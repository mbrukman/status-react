(ns status-im.ui.screens.views
  (:require-macros [status-im.utils.views :refer [defview letsubs] :as views])
  (:require [status-im.utils.universal-links.core :as utils.universal-links]
            [re-frame.core :as re-frame]
            [status-im.utils.platform :as platform]
            [status-im.ui.screens.about-app.views :as about-app]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.bottom-sheet.core :as bottom-sheet]
            [status-im.cljs-react-navigation.reagent :as navigation]
            [reagent.core :as reagent]
            [taoensso.timbre :as log]
            [status-im.ui.screens.mobile-network-settings.view :as mobile-network-settings]
            [status-im.ui.screens.keycard.views :as keycard]
            [status-im.ui.screens.home.sheet.views :as home.sheet]
            [status-im.ui.screens.routing.core :as routing]
            [status-im.ui.screens.signing.views :as signing]
            [status-im.ui.screens.popover.views :as popover]
            [status-im.ui.screens.multiaccounts.recover.views :as recover.views]
            [status-im.utils.dimensions :as dimensions]
            [status-im.ui.screens.wallet.send.views :as wallet]
            status-im.ui.screens.wallet.collectibles.etheremon.views
            status-im.ui.screens.wallet.collectibles.cryptostrikers.views
            status-im.ui.screens.wallet.collectibles.cryptokitties.views
            status-im.ui.screens.wallet.collectibles.superrare.views
            status-im.ui.screens.wallet.collectibles.kudos.views))

(defn bottom-sheet-comp [opts height-atom]
  ;; We compute bottom sheet height dynamically by rendering it
  ;; on an invisible view; then, if height is already available
  ;; (either because it is statically provided or computed),
  ;; we render the sheet itself
  (if (or (not @height-atom) (= 0 @height-atom))
    [react/view {:style {:position :absolute :opacity 0}
                 :on-layout (fn [e]
                              (let [h (-> e .-nativeEvent .-layout .-height)]
                                (reset! height-atom h)))}
     (when (:content opts)
       [(:content opts)])]
    [bottom-sheet/bottom-sheet (assoc opts :content-height @height-atom)]))

(views/defview bottom-sheet []
  (views/letsubs [{:keys [show? view]} [:bottom-sheet]]
    (let [opts (cond-> {:show?     show?
                        :on-cancel #(re-frame/dispatch [:bottom-sheet/hide])}

                 (map? view)
                 (merge view)

                 (= view :mobile-network)
                 (merge mobile-network-settings/settings-sheet)

                 (= view :mobile-network-offline)
                 (merge mobile-network-settings/offline-sheet)

                 (= view :add-new)
                 (merge home.sheet/add-new)

                 (= view :public-chat-actions)
                 (merge home.sheet/public-chat-actions)

                 (= view :keycard.login/more)
                 (merge keycard/more-sheet)

                 (= view :learn-more)
                 (merge about-app/learn-more)

                 (= view :private-chat-actions)
                 (merge home.sheet/private-chat-actions)

                 (= view :group-chat-actions)
                 (merge home.sheet/group-chat-actions)

                 (= view :recover-sheet)
                 (merge (recover.views/bottom-sheet)))
          height-atom (reagent/atom (if (:content-height opts) (:content-height opts) nil))]
      [bottom-sheet-comp opts height-atom])))

(def debug? ^boolean js/goog.DEBUG)

;; Persist navigation state in dev mode
(when debug?
  (defonce state (atom nil))
  (defn- persist-state! [state-obj]
    (js/Promise.
     (fn [resolve _]
       (reset! state state-obj)
       (resolve true))))
  (defn- load-state! []
    (js/Promise.
     (fn [resolve _]
       (resolve @state)))))

(defn main []
  (let [view-id               (re-frame/subscribe [:view-id])
        main-app-navigator    (navigation/create-app-container (routing/get-main-component false))
        twopane-app-navigator (navigation/create-app-container (routing/get-main-component true))
        two-pane?             (reagent/atom (dimensions/fit-two-pane?))]
    (.addEventListener react/dimensions
                       "change"
                       (fn [_]
                         (let [two-pane-enabled? (dimensions/fit-two-pane?)]
                           (re-frame/dispatch [:set-two-pane-ui-enabled two-pane-enabled?])
                           (log/debug ":set-two-pane " two-pane-enabled?)
                           (reset! two-pane? two-pane-enabled?))))
    (reagent/create-class
     {:component-did-mount
      (fn []
        (re-frame/dispatch [:set-two-pane-ui-enabled @two-pane?])
        (log/debug :main-component-did-mount @view-id)
        (utils.universal-links/initialize))

      :component-will-unmount
      utils.universal-links/finalize

      :component-will-update
      (fn []
        (when-not platform/desktop?
          (react/dismiss-keyboard!)))

      :component-did-update
      (fn []
        (log/debug :main-component-did-update @view-id))

      :reagent-render
      (fn []
        [react/safe-area-provider
         [react/view {:flex 1}
          [(if @two-pane? twopane-app-navigator main-app-navigator)
           (merge {:ref               (fn [r]
                                        (navigation/set-navigator-ref r)
                                        ;; TODO: Review this code
                                        (when (and platform/android?
                                                   (not debug?)
                                                   (not (contains? #{:intro :login :progress} @view-id)))
                                          (navigation/navigate-to @view-id nil)))
                   :enableURLHandling false}
                  (when debug?
                    ;; https://github.com/react-navigation/native/blob/d0b24924b2e075fed3bd6586339d34fdd4c2b78e/src/createAppContainer.js#L293
                    ;; urlShouldBe handled by react-navigation otherwise loadNavigationState does not work
                    {:enableURLHandling      true
                     :persistNavigationState persist-state!
                     :loadNavigationState    load-state!}))]
          [wallet/prepare-transaction]
          [wallet/request-transaction]
          [wallet/select-account]
          [signing/signing]
          [bottom-sheet]
          [popover/popover]]])})))

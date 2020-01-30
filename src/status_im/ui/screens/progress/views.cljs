(ns status-im.ui.screens.progress.views
  (:require [status-im.ui.screens.progress.styles :as styles]
            [status-im.ui.components.react :as react]))

;; a simple view with animated progress indicator in its center
(defn progress []
  [react/keyboard-avoiding-view {:style styles/container}
   [react/activity-indicator {:animating true}]])

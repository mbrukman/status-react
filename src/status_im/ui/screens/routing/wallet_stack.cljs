(ns status-im.ui.screens.routing.wallet-stack
  (:require [status-im.utils.config :as config]
            [status-im.ui.screens.currency-settings.views :as currency-settings]
            [status-im.ui.screens.wallet.collectibles.views :as collectibles]
            [status-im.ui.screens.wallet.settings.views :as wallet-settings]
            [status-im.ui.screens.wallet.transactions.views :as wallet-transactions]
            [status-im.ui.screens.wallet.custom-tokens.views :as custom-tokens]
            [status-im.ui.screens.wallet.accounts.views :as wallet.accounts]
            [status-im.ui.screens.wallet.account.views :as wallet.account]
            [status-im.ui.screens.wallet.add-new.views :as add-account]
            [status-im.ui.screens.wallet.account-settings.views :as account-settings]
            [status-im.ui.screens.routing.core :as navigation]))

(defonce stack (navigation/create-stack))

(defn wallet-stack []
  [stack {:initial-route-name :wallet
          :header-mode        :none}
   [{:name      :wallet
     :component wallet.accounts/accounts-overview}
    {:name      :wallet-account
     :component wallet.account/account}
    {:name      :add-new-account
     :component add-account/add-account}
    {:name      :add-watch-account
     :component add-account/add-watch-account}
    {:name      :add-new-account-password
     :component add-account/password}
    {:name      :add-new-account-pin
     :component add-account/pin}
    {:name      :account-added
     :component account-settings/account-added}
    {:name      :account-settings
     :component account-settings/account-settings}
    {:name      :collectibles-list
     :component collectibles/collectibles-list}
    ;; {:name :wallet-onboarding-setup
    ;;          :component }
    {:name      :wallet-transaction-details
     :component wallet-transactions/transaction-details}
    ;; {:name :wallet-settings-hook
    ;;          :component }
    {:name      :wallet-settings-assets
     :component wallet-settings/manage-assets}
    {:name      :wallet-add-custom-token
     :component custom-tokens/add-custom-token}
    {:name      :wallet-custom-token-details
     :component custom-tokens/custom-token-details}

    {:name      :currency-settings
     :component currency-settings/currency-settings}]])

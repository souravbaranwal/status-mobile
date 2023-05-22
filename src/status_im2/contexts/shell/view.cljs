(ns status-im2.contexts.shell.view
  (:require [quo2.core :as quo]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]
            [react-native.core :as rn]
            [status-im2.contexts.shell.utils :as utils]
            [status-im2.navigation.state :as navigation.state]
            [status-im2.contexts.shell.animation :as animation]
            [status-im2.contexts.shell.constants :as shell.constants]
            [status-im2.contexts.shell.shared-values :as shared-values]
            [status-im2.contexts.shell.components.home-stack.view :as home-stack]
            [status-im2.contexts.shell.components.bottom-tabs.view :as bottom-tabs]
            [status-im2.contexts.shell.components.shell-screen.view :as shell-screen]
            [status-im2.contexts.shell.components.floating-screens.view :as floating-screens]))

(defn navigate-back-handler
  []
  (if (and (not @navigation.state/curr-modal)
           (or
            (utils/floating-screen-open? shell.constants/community-screen)
            (utils/floating-screen-open? shell.constants/chat-screen)))
    (do (rf/dispatch [:navigate-back])
        true)
    false))

(defn f-shell-stack
  []
  (let [shared-values (shared-values/calculate-shared-values)]
    (rn/use-effect
     (fn []
       (rn/hw-back-add-listener navigate-back-handler)
       []))
    [:<>
     [shell-screen/view]
     [:f> bottom-tabs/f-bottom-tabs]
     [:f> home-stack/f-home-stack]
     [quo/floating-shell-button
      {:jump-to {:on-press #(animation/close-home-stack true)
                 :label    (i18n/label :t/jump-to)}}
      {:position :absolute
       :bottom   (+ (utils/bottom-tabs-container-height) 7)} ;; bottom offset is 12 = 7 + 5(padding on
                                                             ;; button)
      (:home-stack-opacity shared-values)]
     [floating-screens/view]]))

(defn shell-stack
  []
  [:f> f-shell-stack])

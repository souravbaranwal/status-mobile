(ns status-im2.contexts.shell.animation
  (:require [utils.re-frame :as rf]
            [react-native.reanimated :as reanimated]
            [status-im2.contexts.shell.utils :as utils]
            [status-im2.contexts.shell.state :as state]
            [status-im2.contexts.shell.constants :as shell.constants]))

(defn open-home-stack
  [stack-id animate?]
  (let [home-stack-state-value (utils/calculate-home-stack-state-value stack-id animate?)]
    (reanimated/set-shared-value (:selected-stack-id @state/shared-values-atom) (name stack-id))
    (reanimated/set-shared-value (:home-stack-state @state/shared-values-atom) home-stack-state-value)
    (utils/change-selected-stack-id stack-id true home-stack-state-value)
    (js/setTimeout
     (fn []
       (utils/load-stack stack-id)
       (utils/change-shell-status-bar-style))
     (if animate? shell.constants/shell-animation-time 0))))

(defn change-tab
  [stack-id]
  (reanimated/set-shared-value (:animate-home-stack-left @state/shared-values-atom) false)
  (reanimated/set-shared-value (:selected-stack-id @state/shared-values-atom) (name stack-id))
  (utils/load-stack stack-id)
  (utils/change-selected-stack-id stack-id true))

(defn bottom-tab-on-press
  [stack-id animate?]
  (when (and @state/shared-values-atom (not= stack-id @state/selected-stack-id))
    (if (utils/home-stack-open?)
      (change-tab stack-id)
      (open-home-stack stack-id animate?))
    (utils/update-view-id)))

(defn close-home-stack
  [animate?]
  (let [stack-id               nil
        home-stack-state-value (utils/calculate-home-stack-state-value stack-id animate?)]
    (reanimated/set-shared-value (:animate-home-stack-left @state/shared-values-atom) true)
    (reanimated/set-shared-value (:home-stack-state @state/shared-values-atom) home-stack-state-value)
    (utils/change-selected-stack-id stack-id true home-stack-state-value)
    (utils/change-shell-status-bar-style)
    (utils/update-view-id)))

;; Floating Screen

(defn animate-floating-screen
  [screen-id animation community-chat?]
  (when (utils/floating-screen-animate? screen-id animation)
    (reanimated/set-shared-value
     (get-in @state/shared-values-atom [screen-id :screen-state])
     animation)
    (reset! state/floating-screens-state
      (assoc @state/floating-screens-state screen-id animation))
    (when (and (utils/floating-screen-open? screen-id)
               (not community-chat?))
      (js/setTimeout
       #(open-home-stack
         (if (= screen-id shell.constants/community-screen)
           :communities-stack
           :chats-stack)
         false)
       shell.constants/shell-animation-time))))

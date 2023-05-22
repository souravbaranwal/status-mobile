(ns status-im2.contexts.shell.components.floating-screens.view
  (:require [utils.re-frame :as rf]
            [react-native.core :as rn]
            [react-native.reanimated :as reanimated]
            [status-im2.contexts.shell.state :as state]
            [status-im2.contexts.chat.messages.view :as chat]
            [status-im2.contexts.shell.animation :as animation]
            [status-im2.contexts.shell.constants :as shell.constants]
            [status-im2.contexts.shell.components.floating-screens.style :as style]
            [status-im2.contexts.communities.overview.view :as communities.overview]))

(def screens-map
  {shell.constants/community-screen communities.overview/overview
   shell.constants/chat-screen      chat/chat-render})

(defn f-screen
  [screen-id {:keys [id animation community-chat?]}]
  ;; First render screen, then animate (smoother animation)
  (rn/use-effect
   (fn []
     (animation/animate-floating-screen screen-id animation community-chat?))
   [animation])
  [reanimated/view
   {:style (style/screen (get @state/shared-values-atom screen-id))}
   [(get screens-map screen-id) id]])

;; Currently chat screen and events both depends on current-chat-id,
;; Once we remove use of current-chat-id from view then we can keep last chat loaded, for fast
;; navigation
(defn lazy-screen
  [screen-id]
  (let [screen-param (rf/sub [:shell/floating-screen screen-id])]
    (when screen-param
      [:f> f-screen screen-id screen-param])))

(defn view
  []
  [:<>
   [lazy-screen shell.constants/community-screen]
   [lazy-screen shell.constants/chat-screen]])


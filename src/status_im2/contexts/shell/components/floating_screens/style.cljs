(ns status-im2.contexts.shell.components.floating-screens.style
  (:require [quo2.foundations.colors :as colors]
            [react-native.reanimated :as reanimated]
            [status-im2.contexts.shell.utils :as utils]))

(defn screen
  [{:keys [screen-left screen-top screen-width screen-height screen-z-index screen-margin-top]}]
  (let [{:keys [width height]} (utils/dimensions)]
    (reanimated/apply-animations-to-style
     {:left    screen-left
      :top     screen-top
      :width   screen-width
      :height  screen-height
      :z-index screen-z-index
      ;;:margin-top screen-margin-top
     }
     ;; Maybe also add scale
     {:background-color (colors/theme-colors colors/white colors/neutral-95)
      :overflow         :hidden
      :padding-top      -300})))

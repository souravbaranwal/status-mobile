(ns status-im2.contexts.shell.components.home-stack.style
  (:require [quo2.foundations.colors :as colors]
            [status-im2.contexts.shell.utils :as utils]))

(defn home-stack
  []
  (let [{:keys [width height]} (utils/dimensions)]
    {:border-bottom-left-radius  20
     :border-bottom-right-radius 20
     :background-color           (colors/theme-colors colors/white colors/neutral-95)
     :overflow                   :hidden
     :position                   :absolute
     :width                      width
     :height                     (- height (utils/bottom-tabs-container-height))}))

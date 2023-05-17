(ns status-im2.contexts.onboarding.intro.style
  (:require
    [quo2.foundations.colors :as colors]))

(def page-container
  {:flex            1
   :justify-content :flex-end})

(def text-container
  {:flex      1
   :flex-wrap :wrap})

(def plain-text
  {:font-size   13
   :line-height 18
   :font-weight :normal
   :color       (colors/alpha colors/white 0.7)})

(def highlighted-text
  {:flex  1
   :color colors/white})

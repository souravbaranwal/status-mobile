(ns status-im2.common.mute-drawer.view
  (:require [utils.i18n :as i18n]
            [quo2.core :as quo]
            [react-native.core :as rn]
            [status-im2.constants :as constants]
            [utils.re-frame :as rf]
            [status-im2.common.mute-drawer.style :as style]))

(defn mute-chat
  [id]
  (rf/dispatch [:chat.ui/mute id]))

(defn mute-community
  [id muted?]
  (rf/dispatch [:community/set-muted id (not muted?)]))

(defn hide-sheet-and-dispatch
  [{:keys [chat-id community-id muted? type]}]
  (rf/dispatch [:hide-bottom-sheet])
  (if (= type :community)
    (mute-community community-id muted?)
    (mute-chat chat-id)))

(defn mute-drawer
  [{:keys [chat-id community-id accessibility-label type]}]
  [rn/view {:accessibility-label accessibility-label}
   [quo/text
    {:weight :medium
     :size   :paragraph-2
     :style  (style/header-text)} (i18n/label (if community-id :t/mute-community :t/mute-channel))]
   [quo/menu-item
    {:type                       :transparent
     :title                      (i18n/label :t/mute-for-15-mins)
     :icon-bg-color              :transparent
     :container-padding-vertical 12
     :title-column-style         {:margin-left 2}
     :on-press                   (fn []
                                   (hide-sheet-and-dispatch
                                    {:chat-id      chat-id
                                     :type         type
                                     :community-id community-id
                                     :duration     constants/mute-for-15-mins-type}))}]
   [quo/menu-item
    {:type                       :transparent
     :title                      (i18n/label :t/mute-for-1-hour)
     :icon-bg-color              :transparent
     :container-padding-vertical 12
     :title-column-style         {:margin-left 2}
     :on-press                   (fn []
                                   (hide-sheet-and-dispatch
                                    {:chat-id      chat-id
                                     :type         type
                                     :community-id community-id
                                     :duration     constants/mute-for-1-hour-type}))}]
   [quo/menu-item
    {:type                       :transparent
     :title                      (i18n/label :t/mute-for-8-hours)
     :icon-bg-color              :transparent
     :container-padding-vertical 12
     :title-column-style         {:margin-left 2}
     :on-press                   (fn []
                                   (hide-sheet-and-dispatch
                                    {:chat-id      chat-id
                                     :type         type
                                     :community-id community-id
                                     :duration     constants/mute-for-8-hours-type}))}]
   [quo/menu-item
    {:type                       :transparent
     :title                      (i18n/label :t/mute-for-1-week)
     :icon-bg-color              :transparent
     :container-padding-vertical 12
     :title-column-style         {:margin-left 2}
     :on-press                   (fn []
                                   (hide-sheet-and-dispatch
                                    {:chat-id      chat-id
                                     :type         type
                                     :community-id community-id
                                     :duration     constants/mute-for-1-week}))}]
   [quo/menu-item
    {:type                       :transparent
     :title                      (i18n/label :t/mute-till-unmute)
     :icon-bg-color              :transparent
     :container-padding-vertical 12
     :title-column-style         {:margin-left 2}
     :on-press                   (fn []
                                   (hide-sheet-and-dispatch
                                    {:chat-id      chat-id
                                     :type         type
                                     :community-id community-id
                                     :duration     constants/mute-till-unmuted}))}]])

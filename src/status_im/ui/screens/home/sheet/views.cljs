(ns status-im.ui.screens.home.sheet.views
  (:require [i18n.i18n :as i18n]
            [quo.core :as quo]
            [quo2.core :as quo2]
            [quo2.foundations.colors :as colors]
            [re-frame.core :as rf]
            [status-im.qr-scanner.core :as qr-scanner]
            [status-im.ui.components.invite.views :as invite]
            [status-im.ui.components.react :as rn]
            [status-im.ui.screens.home.sheet.styles :as style]
            [status-im.ui2.screens.chat.components.new-chat.view :as new-chat-aio]
            [status-im.utils.config :as config]
            [status-im2.common.bottom-sheet.view :as bottom-sheet]))

(defn- hide-sheet-and-dispatch
  [event]
  (bottom-sheet/close-bottom-sheet-fn nil)
  (rf/dispatch event))

(defn add-new-view
  []
  [rn/view
   [rn/view style/add-new-view-wrapper
    [quo/text
     {:size   :large
      :weight :bold}
     (i18n/label :t/open-home)]
    [quo/button
     {:type                :icon
      :theme               :icon
      :accessibility-label :universal-qr-scanner
      :on-press            #(rf/dispatch
                             [::qr-scanner/scan-code
                              {:handler ::qr-scanner/on-scan-success}])}
     :main-icons/qr]]
   [quo/list-item
    {:theme               :accent
     :title               (i18n/label :t/start-new-chat)
     :accessibility-label :start-1-1-chat-button
     :icon                :main-icons/one-on-one-chat
     :on-press            #(rf/dispatch [:open-modal :new-chat])}]
   (when config/group-chat-enabled?
     [quo/list-item
      {:theme               :accent
       :title               (i18n/label :t/start-group-chat)
       :accessibility-label :start-group-chat-button
       :icon                :main-icons/group-chat
       :on-press            #(rf/dispatch [:contact.ui/start-group-chat-pressed])}])
   [quo/list-item
    {:theme               :accent
     :title               (i18n/label :t/new-public-group-chat)
     :accessibility-label :join-public-chat-button
     :icon                :main-icons/public-chat
     :on-press            #(rf/dispatch [:open-modal :new-public-chat])}]
   (when @(rf/subscribe [:communities/enabled?])
     [quo/list-item
      {:theme               :accent
       :title               (i18n/label :t/communities-alpha)
       :accessibility-label :communities-button
       :icon                :main-icons/communities
       :on-press            #(rf/dispatch [:navigate-to :communities])}])
   [invite/list-item
    {:accessibility-label :chats-menu-invite-friends-button}]])

(defn new-chat-bottom-sheet
  []
  [rn/view
   [quo2/menu-item
    {:theme                      :main
     :title                      (i18n/label :t/new-chat)
     :icon-bg-color              :transparent
     :type                       :transparent
     :container-padding-vertical 12
     :style-props                {:border-bottom-width 1
                                  :border-color        (colors/theme-colors colors/neutral-10
                                                                            colors/neutral-90)}
     :title-column-style         {:margin-left 2}
     :icon-color                 (colors/theme-colors colors/neutral-50 colors/neutral-40)
     :accessibility-label        :start-a-new-chat
     :icon                       :i/new-message
     :on-press                   #(rf/dispatch [:bottom-sheet/show-sheet :start-a-new-chat])}]
   [quo2/menu-item
    {:theme                        :main
     :title                        (i18n/label :t/connect-with-users)
     :icon-bg-color                :transparent
     :type                         :transparent
     :icon-container-style         {:padding-horizontal 0}
     :container-padding-horizontal {:padding-horizontal 4}
     :style-props                  {:margin-top    18
                                    :margin-bottom 9}
     :container-padding-vertical   12
     :title-column-style           {:margin-left 2}
     :icon-color                   (colors/theme-colors colors/neutral-50 colors/neutral-40)
     :accessibility-label          :connect-with-users
     :subtitle                     (i18n/label :t/enter-a-chat-key)
     :subtitle-color               colors/neutral-50
     :icon                         :i/add-user
     :on-press                     #(rf/dispatch [:open-modal :new-contact])}]])


(def new-chat-bottom-sheet-comp
  {:content new-chat-bottom-sheet})

;; Deprecated
(def add-new
  {:content add-new-view})

(def start-a-new-chat
  {:content new-chat-aio/contact-selection-list})

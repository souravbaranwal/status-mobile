(ns status-im2.contexts.chat.messages.content.view
  (:require [quo.gesture-handler :as gesture-handler]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.platform :as platform]
            [reagent.core :as reagent]
            [status-im.ui2.screens.chat.messages.message :as old-message]
            [status-im2.common.not-implemented :as not-implemented]
            [status-im2.constants :as constants]
            [status-im2.contexts.chat.composer.reply.view :as reply]
            [status-im2.contexts.chat.messages.avatar.view :as avatar]
            [status-im2.contexts.chat.messages.content.album.view :as album]
            [status-im2.contexts.chat.messages.content.audio.view :as audio]
            [status-im2.contexts.chat.messages.content.image.view :as image]
            [status-im2.contexts.chat.messages.content.pin.view :as pin]
            [status-im2.contexts.chat.messages.content.reactions.view :as reactions]
            [status-im2.contexts.chat.messages.content.status.view :as status]
            [status-im2.contexts.chat.messages.content.style :as style]
            [status-im2.contexts.chat.messages.content.system.text.view :as system.text]
            [status-im2.contexts.chat.messages.content.text.view :as content.text]
            [status-im2.contexts.chat.messages.content.unknown.view :as content.unknown]
            [status-im2.contexts.chat.messages.drawers.view :as drawers]
            [utils.address :as address]
            [utils.datetime :as datetime]
            [utils.re-frame :as rf]))

(def delivery-state-showing-time-ms 3000)

(defn avatar-container
  [{:keys [content last-in-group? pinned-by quoted-message from]} is-message-reaction-view?]
  (if (or (and (seq (:response-to content))
               quoted-message)
          last-in-group?
          pinned-by
          is-message-reaction-view?)
    [avatar/avatar from :small]
    [rn/view {:padding-top 2 :width 32}]))

(defn author
  [{:keys [response-to
           compressed-key
           last-in-group?
           pinned-by
           quoted-message
           from
           timestamp]}
   is-message-reaction-view?]
  (when (or (and (seq response-to) quoted-message) last-in-group? pinned-by is-message-reaction-view?)
    (let [[primary-name secondary-name] (rf/sub [:contacts/contact-two-names-by-identity from])
          {:keys [ens-verified added?]} (rf/sub [:contacts/contact-by-address from])]
      [quo/author
       {:primary-name   primary-name
        :secondary-name secondary-name
        :short-chat-key (address/get-shortened-key (or compressed-key from))
        :time-str       (datetime/timestamp->time timestamp)
        :contact?       added?
        :verified?      ens-verified}])))

(defn system-message-content
  [{:keys [content-type quoted-message] :as message-data}]
  (if quoted-message
    [pin/system-message message-data]
    (case content-type

      constants/content-type-system-text           [system.text/text-content message-data]

      constants/content-type-system-pinned-message [system.text/text-content message-data]

      constants/content-type-community             [not-implemented/not-implemented
                                                    [old-message/community message-data]]

      constants/content-type-contact-request       [not-implemented/not-implemented
                                                    [old-message/system-contact-request message-data]])))

(defn on-long-press
  [message-data context]
  (rf/dispatch [:dismiss-keyboard])
  (rf/dispatch [:show-bottom-sheet
                {:content (drawers/reactions-and-actions message-data context)}]))

(defn user-message-content
  []
  (let [show-delivery-state? (reagent/atom false)]
    (fn [{:keys [content-type quoted-message content outgoing outgoing-status] :as message-data}
         context
         keyboard-shown
         is-message-reaction-view?]
      (let [first-image     (first (:album message-data))
            outgoing-status (if (= content-type constants/content-type-album)
                              (:outgoing-status first-image)
                              outgoing-status)
            outgoing        (if (= content-type constants/content-type-album)
                              (:outgoing first-image)
                              outgoing)
            context         (assoc context :on-long-press #(on-long-press message-data context))
            response-to     (:response-to content)
            height          (rf/sub [:dimensions/window-height])]
        [rn/touchable-highlight
         {:accessibility-label (if (and outgoing (= outgoing-status :sending))
                                 :message-sending
                                 :message-sent)
          :underlay-color      (colors/theme-colors colors/neutral-5 colors/neutral-90)
          :style               {:border-radius 16
                                :opacity       (if (and outgoing (= outgoing-status :sending)) 0.5 1)}
          :on-press            (fn []
                                 (if (and platform/ios? @keyboard-shown)
                                   (rn/dismiss-keyboard!)
                                   (when (and outgoing
                                              (not= outgoing-status :sending)
                                              (not @show-delivery-state?))
                                     (reset! show-delivery-state? true)
                                     (js/setTimeout #(reset! show-delivery-state? false)
                                                    delivery-state-showing-time-ms))))
          :on-long-press       #(on-long-press message-data context)}
         [rn/view {:style {:padding-vertical 8}}
          (when (and (seq response-to) quoted-message)
            [reply/quoted-message quoted-message])
          [rn/view
           {:style {:padding-horizontal 12
                    :flex-direction     :row}}
           [avatar-container message-data is-message-reaction-view?]
           (into
            (if is-message-reaction-view?
              [gesture-handler/scroll-view]
              [rn/view])
            [{:style (cond-> {:margin-left 8
                              :flex        1}
                       is-message-reaction-view? (assoc :max-height (* 0.5 height)))}
             [author message-data is-message-reaction-view?]
             (case content-type

               constants/content-type-text [content.text/text-content message-data context]

               constants/content-type-emoji
               [not-implemented/not-implemented [old-message/emoji message-data]]

               constants/content-type-sticker
               [not-implemented/not-implemented [old-message/sticker message-data]]

              constants/content-type-audio
              [audio/audio-message message-data context]

               constants/content-type-image
               [image/image-message 0 message-data context on-long-press]

               constants/content-type-album
               [album/album-message message-data context on-long-press]

               [not-implemented/not-implemented [content.unknown/unknown-content message-data]])
             (when @show-delivery-state?
               [status/status outgoing-status])])]]]))))

(defn message-with-reactions
  [{:keys [pinned-by mentioned in-pinned-view? content-type last-in-group?] :as message-data}
   context]
  (let [show-reaction-authors-sheet? (reagent/atom false)]
    [rn/view
     {:style               (style/message-container in-pinned-view? pinned-by mentioned last-in-group?)
      :accessibility-label :chat-item}
     (when pinned-by
       [pin/pinned-by-view pinned-by])
     (if (#{constants/content-type-system-text constants/content-type-community
            constants/content-type-contact-request
            constants/content-type-system-pinned-message}
          content-type)
       [system-message-content message-data]
       [user-message-content message-data context false])
     [reactions/message-reactions-row message-data [user-message-content message-data context true]
      show-reaction-authors-sheet?]]))

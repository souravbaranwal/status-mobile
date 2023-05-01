(ns status-im2.contexts.chat.messages.content.reactions.view
  (:require [status-im2.constants :as constants]
            [quo2.core :as quo]
            [react-native.core :as rn]
            [utils.re-frame :as rf]
            [status-im2.contexts.chat.messages.drawers.view :as drawers]))

(defn message-reactions-row-comp
  [{:keys [message-id chat-id]} user-message-content show-reaction-author-list?]
  (let [reactions        (rf/sub [:chats/message-reactions message-id chat-id])
        reaction-authors (rf/sub [:chats/reaction-authors-list chat-id])]
    (rn/use-effect (fn []
                     (when (and reaction-authors @show-reaction-author-list?)
                       (rf/dispatch
                        [:show-bottom-sheet
                         {:content                 (fn [] [drawers/reaction-authors
                                                           reaction-authors
                                                           show-reaction-author-list?])
                          :selected-item           (fn []
                                                     user-message-content)
                          :enable-scroll?          false
                          :padding-bottom-override 0}]))
                     (fn []
                       (rf/dispatch [:chat/clear-emoji-reaction-author-details])))
                   [reaction-authors])
    [:<>
     (when (seq reactions)
       [rn/view {:style {:margin-left 52 :margin-bottom 12 :flex-direction :row}}
        (for [{:keys [own emoji-id quantity emoji-reaction-id] :as emoji-reaction} reactions]
          ^{:key (str emoji-reaction)}
          [rn/view {:style {:margin-right 6}}
           [quo/reaction
            {:emoji               (get constants/reactions emoji-id)
             :neutral?            own
             :clicks              quantity
             :on-press            (if own
                                    #(rf/dispatch [:models.reactions/send-emoji-reaction-retraction
                                                   {:message-id        message-id
                                                    :emoji-id          emoji-id
                                                    :emoji-reaction-id emoji-reaction-id}])
                                    #(rf/dispatch [:models.reactions/send-emoji-reaction
                                                   {:message-id message-id
                                                    :emoji-id   emoji-id}]))
             :on-long-press       (fn []
                                    (reset! show-reaction-author-list? true)
                                    (rf/dispatch [:chat.ui/emoji-reactions-by-message-id
                                                  {:message-id message-id
                                                   :chat-id    chat-id}]))
             :accessibility-label (str "emoji-reaction-" emoji-id)}]])
        [quo/add-reaction
         {:on-press (fn []
                      (rf/dispatch [:dismiss-keyboard])
                      (rf/dispatch
                       [:show-bottom-sheet
                        {:content (fn [] [drawers/reactions
                                          {:chat-id chat-id :message-id message-id}])}]))}]])]))

(defn message-reactions-row
  [message-data user-message-content show-reaction-author-list?]
  [:f> message-reactions-row-comp message-data user-message-content show-reaction-author-list?])

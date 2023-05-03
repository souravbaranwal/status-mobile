(ns status-im2.contexts.chat.messages.content.reactions.view
  (:require [status-im2.constants :as constants]
            [quo2.core :as quo]
            [react-native.core :as rn]
            [utils.re-frame :as rf]
            [status-im2.contexts.chat.messages.drawers.view :as drawers]))

(defn- on-press
  [own message-id emoji-id emoji-reaction-id]
  (if own
    (rf/dispatch [:models.reactions/send-emoji-reaction-retraction
                  {:message-id        message-id
                   :emoji-id          emoji-id
                   :emoji-reaction-id emoji-reaction-id}])
    (rf/dispatch [:models.reactions/send-emoji-reaction
                  {:message-id message-id
                   :emoji-id   emoji-id}])))

(defn- on-long-press
  [message-id emoji-id show-reaction-authors-sheet?]
  (reset! show-reaction-authors-sheet? true)
  (rf/dispatch [:chat.ui/emoji-reactions-by-message-id
                {:message-id         message-id
                 :long-pressed-emoji emoji-id}]))

(defn message-reactions-row-comp
  [{:keys [message-id chat-id]} user-message-content show-reaction-authors-sheet?]
  (let [reactions                   (rf/sub [:chats/message-reactions message-id chat-id])
        {:keys [reaction-authors-list
                selected-reaction]} (rf/sub [:chat/reactions-authors])
        _show-sheet                 (when (and reaction-authors-list
                                               @show-reaction-authors-sheet?)
                                      (rf/dispatch [:dismiss-keyboard])
                                      (rf/dispatch
                                       [:show-bottom-sheet
                                        {:on-close
                                         (fn []
                                           (reset! show-reaction-authors-sheet? false)
                                           (rf/dispatch
                                            [:chat/clear-emoji-reaction-author-details]))
                                         :content
                                         (fn [] [drawers/reaction-authors
                                                 reaction-authors-list
                                                 selected-reaction
                                                 (map :emoji-id reactions)])
                                         :selected-item (fn []
                                                          user-message-content)
                                         :padding-bottom-override 0}]))]
    [:<>
     (when (seq reactions)
       [rn/view
        {:style {:margin-left    52
                 :margin-bottom  12
                 :flex-direction :row}}
        (for [{:keys [own emoji-id quantity emoji-reaction-id]
               :as   emoji-reaction} reactions]
          ^{:key emoji-reaction}
          [rn/view {:style {:margin-right 6}}
           [quo/reaction
            {:emoji               (get constants/reactions emoji-id)
             :neutral?            own
             :clicks              quantity
<<<<<<< HEAD
<<<<<<< HEAD
             :on-press            #(on-press own message-id emoji-id emoji-reaction-id)
             :on-long-press       #(on-long-press message-id
                                                  emoji-id
                                                  show-reaction-authors-sheet?)
=======
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
>>>>>>> af14feaf9 (Show message reactions authors sheet)
=======
             :on-press            #(on-press own message-id emoji-id emoji-reaction-id)
             :on-long-press       #(on-long-press chat-id message-id emoji-id show-reaction-author-list?)
>>>>>>> 8a5f7fc30 (Fixes)
             :accessibility-label (str "emoji-reaction-" emoji-id)}]])
        [quo/add-reaction
         {:on-press (fn []
                      (rf/dispatch [:dismiss-keyboard])
                      (rf/dispatch
                       [:show-bottom-sheet
<<<<<<< HEAD
                        {:content       (fn [] [drawers/reactions
                                                {:chat-id    chat-id
                                                 :message-id message-id}])
                         :selected-item (fn []
                                          user-message-content)}]))}]])]))

(defn message-reactions-row
  [message-data user-message-content show-reaction-authors-sheet?]
  [:f> message-reactions-row-comp message-data user-message-content show-reaction-authors-sheet?])
=======
                        {:content (fn [] [drawers/reactions
                                          {:chat-id    chat-id
                                           :message-id message-id}])}]))}]])]))

(defn message-reactions-row
  [message-data user-message-content show-reaction-author-list?]
  [:f> message-reactions-row-comp message-data user-message-content show-reaction-author-list?])
>>>>>>> af14feaf9 (Show message reactions authors sheet)

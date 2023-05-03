(ns status-im.chat.models.reactions
  (:require [re-frame.core :as re-frame]
            [status-im2.constants :as constants]
            [status-im.data-store.reactions :as data-store.reactions]
            [status-im.transport.message.protocol :as message.protocol]
            [utils.re-frame :as rf]
            [taoensso.timbre :as log]
            [clojure.set :as set]
            [utils.transforms :as transforms]))

(defn update-reaction
  [acc retracted chat-id message-id emoji-id emoji-reaction-id reaction]
  ;; NOTE(Ferossgp): For a better performance, better to not keep in db all retracted reactions
  ;; retraction will always come after the reaction so there shouldn't be a conflict
  (if retracted
    (update-in acc [chat-id message-id emoji-id] dissoc emoji-reaction-id)
    (assoc-in acc [chat-id message-id emoji-id emoji-reaction-id] reaction)))

(defn process-reactions
  [chats]
  (fn [reactions new-reactions]
    ;; TODO(Ferossgp): handling own reaction in subscription could be expensive,
    ;; for better performance we can here separate own reaction into 2 maps
    (reduce
     (fn [acc
          {:keys [chat-id message-id emoji-id emoji-reaction-id retracted]
           :as   reaction}]
       (cond-> (update-reaction acc retracted chat-id message-id emoji-id emoji-reaction-id reaction)
         (get-in chats [chat-id :profile-public-key])
         (update-reaction retracted
                          constants/timeline-chat-id
                          message-id
                          emoji-id
                          emoji-reaction-id
                          reaction)))
     reactions
     new-reactions)))

(defn- earlier-than-deleted-at?
  [{:keys [db]} {:keys [chat-id clock-value]}]
  (let [{:keys [deleted-at-clock-value]}
        (get-in db [:chats chat-id])]
    (>= deleted-at-clock-value clock-value)))

(rf/defn receive-signal
  [{:keys [db] :as cofx} reactions]
  (let [reactions (filter (partial earlier-than-deleted-at? cofx) reactions)]
    {:db (update db :reactions (process-reactions (:chats db)) reactions)}))

(rf/defn load-more-reactions
  {:events [:load-more-reactions]}
  [{:keys [db]} cursor chat-id]
  (when-let [session-id (get-in db [:pagination-info chat-id :messages-initialized?])]
    (data-store.reactions/reactions-by-chat-id-rpc
     chat-id
     cursor
     constants/default-number-of-messages
     #(re-frame/dispatch [::reactions-loaded chat-id session-id %])
     #(log/error "failed loading reactions" chat-id %))))

(rf/defn reactions-loaded
  {:events [::reactions-loaded]}
  [{db :db}
   chat-id
   session-id
   reactions]
  (when-not (and (get-in db [:pagination-info chat-id :messages-initialized?])
                 (not= session-id
                       (get-in db [:pagination-info chat-id :messages-initialized?])))
    (let [reactions-w-chat-id (map #(assoc % :chat-id chat-id) reactions)]
      {:db (update db :reactions (process-reactions (:chats db)) reactions-w-chat-id)})))


;; Send reactions


(rf/defn send-emoji-reaction
  {:events [:models.reactions/send-emoji-reaction]}
  [{{:keys [current-chat-id]} :db :as cofx} reaction]
  (message.protocol/send-reaction cofx
                                  (update reaction :chat-id #(or % current-chat-id))))

(rf/defn send-retract-emoji-reaction
  {:events [:models.reactions/send-emoji-reaction-retraction]}
  [{{:keys [current-chat-id]} :db :as cofx} reaction]
  (message.protocol/send-retract-reaction cofx
                                          (update reaction :chat-id #(or % current-chat-id))))

(rf/defn receive-one
  {:events [::receive-one]}
  [{:keys [db]} reaction]
  {:db (update db :reactions (process-reactions (:chats db)) [reaction])})

(defn message-reactions
  [current-public-key reactions]
  (reduce
   (fn [acc [emoji-id reactions]]
     (if (pos? (count reactions))
       (let [own (first (filter (fn [[_ {:keys [from]}]]
                                  (= from current-public-key))
                                reactions))]
         (conj acc
               {:emoji-id          emoji-id
                :own               (boolean (seq own))
                :emoji-reaction-id (:emoji-reaction-id (second own))
                :quantity          (count reactions)}))
       acc))
   []
   reactions))

(defn- <-rpc
  [emoji-reaction]
  (-> emoji-reaction
      (set/rename-keys
       {:emojiId       :emoji-id
        :compressedKey :compressed-key})
      (select-keys [:emoji-id :from :compressed-key])))

(defn- format-response
  [response]
  (->> (transforms/js->clj response)
       (map <-rpc)
       (group-by :emoji-id)
       (into (sorted-map))))

(rf/defn save-emoji-reaction-details
  {:events [:chat/save-emoji-reaction-details]}
  [{:keys [db]} message-reactions long-pressed-emoji]
  {:db (-> db
           (assoc-in [:chats (:current-chat-id db) :message/reaction-authors-list] message-reactions)
           (assoc-in [:chats (:current-chat-id db) :message/reaction-authors-list-selected-reaction]
                     long-pressed-emoji))})

(rf/defn clear-emoji-reaction-details
  {:events [:chat/clear-emoji-reaction-author-details]}
  [{:keys [db]} message-reactions]
  {:db (update-in db [:chats (:current-chat-id db)] dissoc :message/reaction-authors-list)})

(rf/defn emoji-reactions-by-message-id
  {:events [:chat.ui/emoji-reactions-by-message-id]}
  [{:keys [db]} {:keys [message-id chat-id long-pressed-emoji]}]
  {:json-rpc/call [{:method      "wakuext_emojiReactionsByChatIDMessageID"
                    :params      [chat-id message-id]
                    :js-response true
                    :on-error    #(log/error "failed to fetch emoji reaction by message-id: "
                                             {:message-id message-id :error %})
                    :on-success  #(rf/dispatch [:chat/save-emoji-reaction-details
                                                (format-response %) long-pressed-emoji])}]})

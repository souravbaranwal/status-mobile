(ns status-im2.contexts.shell.events
  (:require [utils.re-frame :as rf]
            [re-frame.core :as re-frame]
            [status-im.utils.core :as utils]
            [status-im2.constants :as constants]
            [status-im2.contexts.shell.state :as state]
            [status-im.async-storage.core :as async-storage]
            [status-im2.contexts.shell.utils :as shell.utils]
            [status-im2.navigation.state :as navigation.state]
            [status-im2.contexts.shell.animation :as animation]
            [status-im2.contexts.shell.constants :as shell.constants]
            [status-im.data-store.switcher-cards :as switcher-cards-store]))

;;;; Effects

;; Navigation
(re-frame/reg-fx
 :shell/change-tab-fx
 (fn [stack-id]
   (when (some #(= stack-id %) shell.constants/stacks-ids)
     (animation/bottom-tab-on-press stack-id false))))

(re-frame/reg-fx
 :shell/navigate-back
 (fn [view-id]
   (animation/animate-floating-screen view-id 0 false)))

(re-frame/reg-fx
 :shell/navigate-to-jump-to-fx
 (fn []
   (animation/close-home-stack false)))

(re-frame/reg-fx
 :shell/reset-bottom-tabs
 ;; Todo - Also use this while theme changes for faster reload
 (fn []
   (let [selected-stack-id @state/selected-stack-id]
     (async-storage/set-item! :selected-stack-id nil)
     (reset! state/load-communities-stack? (= selected-stack-id :communities-stack))
     (reset! state/load-chats-stack? (= selected-stack-id :chats-stack))
     (reset! state/load-wallet-stack? (= selected-stack-id :wallet-stack))
     (reset! state/load-browser-stack? (= selected-stack-id :browser-stack)))))

;; Events

(rf/defn switcher-cards-loaded
  {:events [:shell/switcher-cards-loaded]}
  [{:keys [db]} loaded-switcher-cards]
  {:db (assoc db
              :shell/switcher-cards
              (utils/index-by :card-id (switcher-cards-store/<-rpc loaded-switcher-cards)))})

(defn calculate-card-data
  [db now view-id id]
  (case view-id
    :chat
    (let [chat (get-in db [:chats id])]
      (case (:chat-type chat)
        constants/one-to-one-chat-type
        {:card-id       id
         :switcher-card {:type      shell.constants/one-to-one-chat-card
                         :card-id   id
                         :clock     now
                         :screen-id id}}

        constants/private-group-chat-type
        {:card-id       id
         :switcher-card {:type      shell.constants/private-group-chat-card
                         :card-id   id
                         :clock     now
                         :screen-id id}}

        constants/community-chat-type
        {:card-id       (:community-id chat)
         :switcher-card {:type      shell.constants/community-channel-card
                         :card-id   (:community-id chat)
                         :clock     now
                         :screen-id (:chat-id chat)}}

        nil))

    :community-overview
    {:card-id       id
     :switcher-card {:type      shell.constants/community-card
                     :card-id   id
                     :clock     now
                     :screen-id id}}
    nil))

(rf/defn add-switcher-card
  {:events [:shell/add-switcher-card]}
  [{:keys [db now] :as cofx} view-id id]
  (let [card-data     (calculate-card-data db now view-id id)
        switcher-card (:switcher-card card-data)]
    (when card-data
      (rf/merge
       cofx
       {:db (assoc-in
             db
             [:shell/switcher-cards (:card-id card-data)]
             switcher-card)}
       (switcher-cards-store/upsert-switcher-card-rpc switcher-card)))))

(rf/defn close-switcher-card
  {:events [:shell/close-switcher-card]}
  [{:keys [db] :as cofx} card-id]
  (rf/merge
   cofx
   {:db (update db :shell/switcher-cards dissoc card-id)}
   (switcher-cards-store/delete-switcher-card-rpc card-id)))

;; Clean this
(rf/defn navigate-to-jump-to
  {:events [:shell/navigate-to-jump-to]}
  [{:keys [db]}]
  {:db
   (-> (assoc db :view-id :shell)
       (assoc-in [:shell/floating-screens (:view-id db) :animation]
                 shell.constants/close-screen-with-shell-animation))
   :shell/navigate-to-jump-to-fx nil})

(rf/defn change-shell-status-bar-style
  {:events [:change-shell-status-bar-style]}
  [_ style]
  {:merge-options {:id "shell-stack" :options {:statusBar {:style style}}}})

(rf/defn change-shell-nav-bar-color
  {:events [:change-shell-nav-bar-color]}
  [_ color]
  {:merge-options {:id "shell-stack" :options {:navigationBar {:backgroundColor color}}}})

;; Navigation
(rf/defn shell-navigate-to
  {:events [:shell/navigate-to]}
  [{:keys [db]} view-id screen-params animation hidden-screen?]
  (if (shell.utils/shell-navigation? view-id)
    (let [community-chat? (and (= view-id :chat)
                               (= (get-in db
                                          [:chats screen-params :chat-type])
                                  constants/community-chat-type))]
      {:db             (assoc-in db
                        [:shell/floating-screens view-id]
                        {:id              screen-params
                         :community-chat? community-chat?
                         :animation       (or animation
                                              (if (= (:view-id db) :shell)
                                                shell.constants/open-screen-with-shell-animation
                                                shell.constants/open-screen-with-slide-animation))})
       :dispatch-later (cond-> []

                         ;; When opening community chat, open community screen in background
                         community-chat?
                         (conj {:ms       shell.constants/shell-animation-time
                                :dispatch [:shell/navigate-to
                                           shell.constants/community-screen
                                           (get-in db [:chats screen-params :community-id])
                                           shell.constants/open-screen-without-animation true]})

                         ;; Make sure opening of community don't update switcher card
                         (not hidden-screen?)
                         (conj {:ms       (* 2 shell.constants/shell-animation-time)
                                :dispatch [:shell/add-switcher-card view-id screen-params]}))})
    {:navigate-to view-id}))

(rf/defn shell-navigate-back
  {:events [:shell/navigate-back]}
  [{:keys [db]}]
  (let [current-view-id (:view-id db)]
    (if (and (not @navigation.state/curr-modal)
             (shell.utils/shell-navigation? current-view-id))
      {:db (-> (assoc db
                      :view-id
                      (cond
                        (= current-view-id shell.constants/community-screen)
                        :communities-stack
                        (shell.utils/floating-screen-open? shell.constants/community-screen)
                        shell.constants/community-screen
                        :else :chats-stack))
               (assoc-in
                [:shell/floating-screens current-view-id :animation]
                shell.constants/close-screen-with-slide-animation))}
      {:navigate-back nil})))

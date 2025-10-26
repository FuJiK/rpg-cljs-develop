(ns rpg.engine.events
  (:require [rpg.engine.state :as S]
            [rpg.engine.dialog :as D]))

(defn register! [x y opts] (swap! S/state assoc-in [:events [x y]] opts))

(defn run-op! [[tag & args]]
  (case tag
    :say (let [[name text] args] (D/show! name text))
    :give-item (let [[item cnt] args]
                 (swap! S/state update :inventory #(update (or %) item (fnil + 0) (or cnt 1)))
                 (D/show! "SYSTEM" (str "「" (name item) "」を手に入れた！")))
    :chest (let [[id {:keys [item count]}] args]
             (if (S/done? id)
               (D/show! "SYSTEM" "（空っぽだ…）")
               (do
                 (S/mark-done! id)
                 (swap! S/state update :inventory #(update (or %) item (fnil + 0) (or count 1)))
                 (D/show! "SYSTEM" (str "宝箱から「" (name item) "」を手に入れた！")))))
    :warp (let [[x y] args]
            (swap! S/state assoc-in [:player :x] x)
            (swap! S/state assoc-in [:player :y] y)
            (D/show! "SYSTEM" (str "ワープ！ (" x "," y ")")))
    (D/show! "SYSTEM" (str "未対応のコマンド: " tag))))

(defn run-script! [ops] (doseq [op ops] (run-op! op)))

(defn trigger-at! [x y]
  (when-let [{:keys [id once? script]} (get-in @S/state [:events [x y]])]
    (when-not (and once? id (S/done? id))
      (when script (run-script! script))
      (when (and once? id) (S/mark-done! id)))))

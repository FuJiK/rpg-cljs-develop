(ns rpg.engine.battle
  (:require [rpg.engine.state :as S]))

(def menu-items [:fight :skill :item :run])

(def default-player-stats
  {:level 5 :hp 40 :max-hp 40 :attack 12 :defense 4 :skill-power 18})

(defn player-stats []
  (merge default-player-stats (or (:player-stats @S/state) {})))

(defn- damage [atk def]
  (max 1 (+ 1 (- atk def))))

(defn in-battle? [] (= :battle (:mode @S/state)))

(defn queue-after-dialog! [enemy-id return-pos]
  (swap! S/state assoc :pending-battle {:enemy-id enemy-id :return return-pos}))

(defn begin! [{:keys [enemy-id return]}]
  (let [ps (player-stats)
        tmpl (get-in @S/state [:enemies-by-id (or enemy-id :rival)])
        e-max-hp (max 1 (quot (:max-hp ps) 2))]
    (swap! S/state assoc
           :mode :battle
           :battle {:enemy-id (or enemy-id :rival)
                    :enemy-name (:name tmpl "ライバル")
                    :enemy-hp e-max-hp
                    :enemy-max-hp e-max-hp
                    :enemy-attack (or (:attack tmpl) 2)
                    :enemy-defense (or (:defense tmpl) 0)
                    :return return
                    :menu-idx 0
                    :phase :menu
                    :message nil})))

(defn- return-pos []
  (let [{:keys [return]} (:battle @S/state)]
    (or return {:map (or (:current-map @S/state) "route1")
                :x (get-in @S/state [:player :x])
                :y (get-in @S/state [:player :y])})))

(defn- finish-win! []
  (let [{:keys [map x y]} (return-pos)
        ps (player-stats)]
    (swap! S/state assoc
           :mode :field
           :battle nil
           :player-stats (assoc ps :hp (min (:max-hp ps) (:hp ps))))
    (swap! S/state assoc-in [:player :x] x)
    (swap! S/state assoc-in [:player :y] y)
    (when map (swap! S/state assoc :current-map map))
    (swap! S/state assoc :post-battle-scroll true)))

(defn- check-win! []
  (when (and (in-battle?)
             (<= (get-in @S/state [:battle :enemy-hp]) 0))
    (swap! S/state assoc-in [:battle :message] "あなたの勝ち！")
    (swap! S/state assoc-in [:battle :phase] :won)
    (js/setTimeout finish-win! 800)))

(defn- set-msg! [text]
  (swap! S/state assoc-in [:battle :message] text)
  (swap! S/state assoc-in [:battle :phase] :message))

(defn- player-attack! [mult]
  (let [ps (player-stats)
        ed (get-in @S/state [:battle :enemy-defense])
        dmg (damage (int (* mult (:attack ps))) ed)]
    (swap! S/state update-in [:battle :enemy-hp] #(max 0 (- % dmg)))
    (set-msg! (str "あなたの攻撃！" dmg " のダメージ！"))
    (js/setTimeout check-win! 400)))

(defn- enemy-turn! []
  (let [ps (player-stats)
        atk (get-in @S/state [:battle :enemy-attack])
        dmg (damage atk (:defense ps))]
    (swap! S/state update :player-stats update :hp #(max 1 (- % dmg)))
    (set-msg! (str (get-in @S/state [:battle :enemy-name]) "の攻撃！" dmg " のダメージ！"))
    (js/setTimeout
     (fn []
       (when (in-battle?)
         (swap! S/state assoc-in [:battle :phase] :menu)
         (swap! S/state assoc-in [:battle :message] nil)))
     650)))

(defn select-menu! [idx]
  (when (and (in-battle?) (= :menu (get-in @S/state [:battle :phase])))
    (swap! S/state assoc-in [:battle :menu-idx] (mod idx (count menu-items)))))

(defn menu-label [cmd]
  (case cmd
    :fight "たたかう"
    :skill "特技"
    :item "どうぐ"
    :run "にげる"
    "???"))

(defn confirm-menu! []
  (when (and (in-battle?) (= :menu (get-in @S/state [:battle :phase])))
    (let [idx (get-in @S/state [:battle :menu-idx] 0)
          cmd (nth menu-items idx)]
      (case cmd
        :fight (do (swap! S/state assoc-in [:battle :phase] :busy)
                   (player-attack! 1.0)
                   (js/setTimeout enemy-turn! 550))
        :skill (do (swap! S/state assoc-in [:battle :phase] :busy)
                   (player-attack! 1.4)
                   (js/setTimeout enemy-turn! 550))
        :item (if (pos? (get-in @S/state [:inventory :potion] 0))
                (do (swap! S/state update :inventory update :potion dec)
                    (swap! S/state update :player-stats update :hp
                           (fn [hp] (min (:max-hp (player-stats)) (+ hp 25))))
                    (set-msg! "ポーションでHPが回復した！")
                    (js/setTimeout
                     (fn []
                       (when (in-battle?)
                         (swap! S/state assoc-in [:battle :phase] :menu)
                         (swap! S/state assoc-in [:battle :message] nil)))
                     600))
                (set-msg! "使える道具がない…"))
        :run (set-msg! "ライバルに逃げ切れない！")))))

(defn tick! [_dt] nil)
(ns rpg.engine.chase
  (:require [rpg.engine.state :as S]
            [rpg.engine.events :as EV]))

(defn rivals-map? []
  (= "route1" (:current-map @S/state)))

(defn chase-active? []
  (and rivals-map?
       (not (S/done? :rival/encounter))
       (true? (get-in @S/state [:chase :active?]))))

(defn- player-y [] (get-in @S/state [:player :y]))

(defn- encounter-blocked? []
  (or (get-in @S/state [:dialog :open?])
      (:pending-battle @S/state)
      (get-in @S/state [:battle])
      (= :battle (:mode @S/state))))

(defn maybe-arm-chase! []
  (when (and rivals-map?
             (not (S/done? :rival/encounter))
             (<= (player-y) 24)
             (not (chase-active?))
             (nil? (:chase @S/state)))
    (swap! S/state assoc :chase {:active? true
                                 :rival-x 11
                                 :rival-y (+ (player-y) 3)})))

(defn rival-object []
  (when (chase-active?)
    (let [{:keys [rival-x rival-y]} (:chase @S/state)]
      [{:pos [rival-x rival-y] :kind :rival :id :rival/youngster}])))

(defn- trigger-encounter! []
  (when-not (S/done? :rival/encounter)
    (S/mark-done! :rival/encounter)
    (swap! S/state dissoc :chase)
    (EV/run-script!
     [[:say "???（ライバル）" "待て！そこまでだ！"]
      [:say "ライバル" "今のうちに決着をつけるぞ！"]
      [:start-battle :rival]])))

(defn on-player-stepped! []
  (when (S/done? :rival/encounter)
    (swap! S/state dissoc :chase))
  (when-not (S/done? :rival/encounter)
    (maybe-arm-chase!)
    (when (and (chase-active?) (not (encounter-blocked?)))
      (let [py (player-y)
            {:keys [rival-y] :as ch} (:chase @S/state)
            new-ry (max py (dec rival-y))]
        (swap! S/state assoc :chase (assoc ch :rival-y new-ry))
        (when (<= new-ry py)
          (trigger-encounter!))))))
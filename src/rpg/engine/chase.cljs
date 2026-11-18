(ns rpg.engine.chase
  (:require [rpg.engine.state :as S]
            [rpg.engine.events :as EV]))

(defn patrol-map? []
  (= "windpass" (:current-map @S/state)))

(defn chase-active? []
  (and patrol-map?
       (not (S/done? :patrol/encounter))
       (true? (get-in @S/state [:chase :active?]))))

(defn- player-y [] (get-in @S/state [:player :y]))

(defn- encounter-blocked? []
  (or (get-in @S/state [:dialog :open?])
      (:pending-battle @S/state)
      (get-in @S/state [:battle])
      (= :battle (:mode @S/state))))

(defn maybe-arm-chase! []
  (when (and patrol-map?
             (not (S/done? :patrol/encounter))
             (<= (player-y) 24)
             (not (chase-active?))
             (nil? (:chase @S/state)))
    (swap! S/state assoc :chase {:active? true
                                 :patrol-x 11
                                 :patrol-y (+ (player-y) 3)})))

(defn patrol-object []
  (when (chase-active?)
    (let [{:keys [patrol-x patrol-y]} (:chase @S/state)]
      [{:pos [patrol-x patrol-y] :kind :patrol :id :patrol/keeper}])))

(defn- trigger-encounter! []
  (when-not (S/done? :patrol/encounter)
    (S/mark-done! :patrol/encounter)
    (swap! S/state dissoc :chase)
    (EV/run-script!
     [[:say "巡回役" "風見塔へ行くのか？　今は風が強くてな。"]
      [:say "巡回役" "塔まで行くなら、まずここで足さばきを見せてくれ。"]
      [:start-battle :patrol]])))

(defn on-player-stepped! []
  (when (S/done? :patrol/encounter)
    (swap! S/state dissoc :chase))
  (when-not (patrol-map?)
    (swap! S/state dissoc :chase))
  (when (and (patrol-map?)
             (not (S/done? :patrol/encounter)))
    (maybe-arm-chase!)
    (when (and (chase-active?) (not (encounter-blocked?)))
      (let [py (player-y)
            {:keys [patrol-y] :as ch} (:chase @S/state)
            new-ry (max py (dec patrol-y))]
        (swap! S/state assoc :chase (assoc ch :patrol-y new-ry))
        (when (<= new-ry py)
          (trigger-encounter!))))))
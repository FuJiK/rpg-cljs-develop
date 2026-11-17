(ns rpg.engine.chase
  (:require [rpg.engine.state :as S]
            [rpg.engine.events :as EV]))

(defn rivals-map? []
  (= "route1" (:current-map @S/state)))

(defn chase-active? []
  (and rivals-map?
       (not (S/done? :rival/encounter))
       (get-in @S/state [:chase :active?])))

(defn- player-y [] (get-in @S/state [:player :y]))

(defn maybe-arm-chase! []
  (when (and rivals-map?
             (not (S/done? :rival/encounter))
             (<= (player-y) 24)
             (not (get-in @S/state [:chase :armed?])))
    (swap! S/state assoc :chase {:armed? true
                                   :active? true
                                   :rival-x 11
                                   :rival-y (+ (player-y) 3)})))

(defn rival-object []
  (when (chase-active?)
    (let [{:keys [rival-x rival-y]} (:chase @S/state)]
      [{:pos [rival-x rival-y] :kind :rival :id :rival/youngster}])))

(defn on-player-stepped! []
  (maybe-arm-chase!)
  (when chase-active?
    (let [py (player-y)
          {:keys [rival-y] :as ch} (:chase @S/state)
          new-ry (max py (dec rival-y))]
      (swap! S/state assoc :chase (assoc ch :rival-y new-ry))
      (when (<= new-ry py)
        (S/mark-done! :rival/encounter)
        (swap! S/state assoc :chase {:armed? true :active? false})
        (EV/run-script!
         [[:say "???（ライバル）" "待て！そこまでだ！"]
          [:say "ライバル" "今のうちに決着をつけるぞ！"]
          [:start-battle :rival]])))))
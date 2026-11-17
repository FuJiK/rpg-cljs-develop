(ns rpg.engine.transition
  (:require [rpg.engine.state :as S]
            [rpg.engine.battle :as B]))

(defn start-scroll-to-battle! []
  (swap! S/state assoc :transition {:kind :to-battle
                                    :phase :out
                                    :t 0
                                    :dur 480}))

(defn start-scroll-to-field! []
  (swap! S/state assoc :transition {:kind :from-battle
                                    :phase :in
                                    :t 0
                                    :dur 480}))

(defn active? []
  (some? (:transition @S/state)))

(defn progress [offset]
  (min 1.0 (max 0.0 offset)))

(defn tick! [dt-ms]
  (when-let [tr (:transition @S/state)]
    (let [nt (+ (:t tr) (long dt-ms))]
      (if (>= nt (:dur tr))
        (case (:phase tr)
          :out (if (= (:kind tr) :to-battle)
                 (do
                   (B/begin! (:pending-battle @S/state))
                   (swap! S/state assoc :transition nil :pending-battle nil))
                 (swap! S/state assoc :transition (assoc tr :phase :in :t 0)))
          :in (swap! S/state assoc :transition nil))
        (swap! S/state assoc-in [:transition :t] nt)))))

(defn scroll-offset []
  (if-let [{:keys [t dur phase]} (:transition @S/state)]
    (let [p (progress (/ (double t) (double (max 1 dur))))]
      (case phase
        :out (* p (double S/view-h))
        :in (* (- 1.0 p) (double S/view-h))
        0.0))
    0.0))
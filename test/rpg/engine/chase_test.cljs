(ns rpg.engine.chase-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [rpg.test-util :as TU]
            [rpg.engine.state :as S]
            [rpg.engine.chase :as CH]))

(deftest encounter-fires-only-once
  (TU/reset-state!)
  (swap! S/state assoc :current-map "windpass"
         :player {:x 10 :y 20 :dir :up}
         :chase {:active? true :patrol-x 10 :patrol-y 20})
  (CH/on-player-stepped!)
  (is (S/done? :patrol/encounter))
  (is (get-in @S/state [:dialog :open?]))
  (let [q (count (get-in @S/state [:dialog :queue]))]
    (CH/on-player-stepped!)
    (CH/on-player-stepped!)
    (is (= q (count (get-in @S/state [:dialog :queue]))))
    (is (nil? (:chase @S/state)))))
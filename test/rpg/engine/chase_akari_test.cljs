(ns rpg.engine.chase-akari-test
  (:require [cljs.test :refer-macros [deftest is]]
            [rpg.test-util :as TU]
            [rpg.engine.state :as S]
            [rpg.engine.chase :as CH]
            ))

(deftest patrol-chase-does-not-run-off-windpass
  (TU/reset-state!)
  (swap! S/state assoc :current-map "akari-village"
         :player {:x 10 :y 9 :dir :left}
         :chase {:active? true :patrol-x 11 :patrol-y 12})
  (CH/on-player-stepped!)
  (CH/on-player-stepped!)
  (CH/on-player-stepped!)
  (is (nil? (:chase @S/state)))
  (is (not (get-in @S/state [:dialog :open?])))
  (is (not (S/done? :patrol/encounter))))
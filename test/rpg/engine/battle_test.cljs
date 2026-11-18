(ns rpg.engine.battle-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [rpg.test-util :as TU]
            [rpg.engine.state :as S]
            [rpg.engine.battle :as B]))

(deftest enemy-hp-is-half-player-max
  (TU/reset-state!)
  (swap! S/state assoc :enemies-by-id {:patrol {:id :patrol :name "巡回役" :attack 2}})
  (B/begin! {:enemy-id :patrol :return {:map "windpass" :x 10 :y 20}})
  (is (= 20 (get-in @S/state [:battle :enemy-max-hp])))
  (is (= 20 (get-in @S/state [:battle :enemy-hp]))))

(deftest queue-pending-battle
  (TU/reset-state!)
  (B/queue-after-dialog! :patrol {:map "windpass" :x 9 :y 21})
  (is (= {:enemy-id :patrol :return {:map "windpass" :x 9 :y 21}}
         (:pending-battle @S/state))))
(ns rpg.engine.map-test
  (:require [cljs.test :refer [deftest is testing use-fixtures]]
            [rpg.engine.state :as S]
            [rpg.engine.map :as M]))

(use-fixtures :each
  (fn [f]
    (reset! S/state {:player {:x 0 :y 0 :dir :down}
                     :map {}
                     :solid-tiles #{1 2}
                     :events {}
                     :opened #{}
                     :camera {:x 0 :y 0}
                     :keys #{}
                     :dialog {:open? false :queue []}
                     :inventory {}
                     :items-by-id {}
                     :enemies-by-id {}
                     :rng (random-uuid)})
    (f)))

(deftest movement-and-collision
  (testing "free tile movement"
    (swap! S/state assoc :keys #{:right})
    (M/try-move!)
    (is (= 1 (get-in @S/state [:player :x])))
    (is (= 0 (get-in @S/state [:player :y]))))
  (testing "collision with solid tile"
    (M/set-tile! [2 0] 1) ;; 岩
    (swap! S/state assoc :keys #{:right})
    (M/try-move!) ;; 1 -> 2 に進もうとするがブロック
    (M/try-move!)
    (is (= 1 (get-in @S/state [:player :x]))))

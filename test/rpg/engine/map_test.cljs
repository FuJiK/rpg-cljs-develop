(ns rpg.engine.map-test
  (:require [cljs.test :refer [deftest is testing use-fixtures]]
            [rpg.engine.state :as S]
            [rpg.engine.map :as M]
            [rpg.test-util :as TU]))

(use-fixtures :each (fn [f] (TU/reset-state!) (f)))

(deftest ensure-generated-defaults-to-grass
  (is (= 0 (M/get-tile [99 99])))
  (M/ensure-generated! [99 99])
  (is (= 0 (M/get-tile [99 99]))))

(deftest collides-on-solid-tiles
  (M/set-tile! [1 0] 1)
  (is (M/collides? [1 0]))
  (is (not (M/collides? [0 0]))))

(deftest try-start-move-on-free-tile
  (swap! S/state assoc :keys #{:right})
  (M/try-start-move!)
  (is (:active? (:anim @S/state)))
  (is (= [0 0] (:from (:anim @S/state))))
  (is (= [1 0] (:to (:anim @S/state)))))

(deftest try-start-move-blocked-by-solid
  (M/set-tile! [1 0] 1)
  (swap! S/state assoc :keys #{:right})
  (M/try-start-move!)
  (is (not (:active? (:anim @S/state))))
  (is (= 0 (get-in @S/state [:player :x]))))

(deftest next-pos-by-direction
  (is (= [1 0] (M/next-pos {:x 0 :y 0} :right)))
  (is (= [0 1] (M/next-pos {:x 0 :y 0} :down))))

(ns rpg.engine.events-test
  (:require [cljs.test :refer [deftest is use-fixtures]]
            [rpg.engine.state :as S]
            [rpg.engine.events :as EV]
            [rpg.test-util :as TU]))

(use-fixtures :each
  (fn [f]
    (TU/reset-state!)
    (with-redefs [rpg.engine.dialog/show! (fn [& _] nil)]
      (f))))

(deftest chest-once
  (EV/register! 1 0 {:id :chest-001 :once? true
                     :script [[:chest :chest-001 {:item :potion :count 1}]]})
  (EV/trigger-at! 1 0)
  (is (= 1 (get-in @S/state [:inventory :potion])))
  (EV/trigger-at! 1 0)
  (is (= 1 (get-in @S/state [:inventory :potion])))
  (is (contains? (:opened @S/state) :chest-001)))

(deftest give-item
  (EV/run-op! [:give-item :hi-potion 2])
  (is (= 2 (get-in @S/state [:inventory :hi-potion]))))

(deftest warp-moves-player
  (EV/run-op! [:warp 10 10])
  (is (= 10 (get-in @S/state [:player :x])))
  (is (= 10 (get-in @S/state [:player :y]))))

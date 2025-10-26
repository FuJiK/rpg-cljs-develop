(ns rpg.engine.events-test
  (:require [cljs.test :refer [deftest is testing use-fixtures]]
            [rpg.engine.state :as S]
            [rpg.engine.events :as EV]))

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
    (with-redefs [rpg.engine.dialog/show! (fn [& _] nil)]
      (f))))

(deftest chest-once
  (EV/register! 1 0 {:id :chest/001 :once? true
                     :script [[:chest :chest/001 {:item :potion :count 1}]]})
  (EV/trigger-at! 1 0)
  (is (= 1 (get-in @S/state [:inventory :potion])))
  ;; 2回目は増えない
  (EV/trigger-at! 1 0)
  (is (= 1 (get-in @S/state [:inventory :potion])))
  (is (contains? (:opened @S/state) :chest/001)))

(deftest give-item
  (EV/run-op! [:give-item :hi-potion 2])
  (is (= 2 (get-in @S/state [:inventory :hi-potion]))))

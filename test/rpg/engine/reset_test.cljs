(ns rpg.engine.reset-test
  (:require [cljs.test :refer-macros [deftest is]]
            [rpg.test-util :as TU]
            [rpg.engine.state :as S]
            [rpg.engine.reset :as R]))

(deftest clear-progress-wipes-save-related-state
  (TU/reset-state!)
  (set! js/localStorage (TU/mock-local-storage))
  (swap! S/state assoc
         :opened #{:patrol/encounter :chest-001}
         :inventory {:potion 2}
         :player-stats {:hp 12 :max-hp 40}
         :mode :battle)
  (R/clear-progress!)
  (is (empty? (:opened @S/state)))
  (is (empty? (:inventory @S/state)))
  (is (= 40 (get-in @S/state [:player-stats :hp])))
  (is (= :field (:mode @S/state))))
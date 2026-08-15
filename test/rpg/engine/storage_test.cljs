(ns rpg.engine.storage-test
  (:require [cljs.test :refer [deftest is]]
            [rpg.engine.state :as S]
            [rpg.engine.storage :as Store]
            [rpg.test-util :as TU]))

(deftest save-load-roundtrip
  (TU/reset-state!)
  (set! js/localStorage (TU/mock-local-storage))
  (try
    (swap! S/state assoc
           :player {:x 5 :y 7 :dir :up}
           :opened #{:chest-001}
           :inventory {:potion 2}
           :map {[0 0] 3})
    (Store/save-game!)
    (TU/reset-state!)
    (Store/load-game!)
    (is (= {:x 5 :y 7 :dir :up} (:player @S/state)))
    (is (= #{:chest-001} (:opened @S/state)))
    (is (= {:potion 2} (:inventory @S/state)))
    (is (= {[0 0] 3} (:map @S/state)))
    (finally
      (set! js/localStorage js/undefined))))

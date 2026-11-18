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
           :current-map "akari-village")
    (Store/save-game!)
    (TU/reset-state!)
    (Store/load-game!)
    (is (= {:x 5 :y 7 :dir :up} (:player @S/state)))
    (is (= #{:chest-001} (:opened @S/state)))
    (is (= {:potion 2} (:inventory @S/state)))
    (is (= "akari-village" (:current-map @S/state)))
    (finally
      (set! js/localStorage js/undefined))))


(deftest load-legacy-map-and-encounter
  (doseq [[old-map new-map] [["masara" "akari-village"]
                             ["route1" "windpass"]]]
    (TU/reset-state!)
    (set! js/localStorage (TU/mock-local-storage))
    (try
      (let [player {:x 5 :y 7 :dir :up}
            inventory {:potion 2}]
        (.setItem js/localStorage Store/save-key
                  (pr-str {:current-map old-map
                           :player player
                           :inventory inventory
                           :opened #{:rival/encounter :chest-001}}))
        (Store/load-game!)
        (is (= new-map (:current-map @S/state)))
        (is (= player (:player @S/state)))
        (is (= inventory (:inventory @S/state)))
        (is (= #{:patrol/encounter :chest-001} (:opened @S/state))))
      (finally
        (set! js/localStorage js/undefined)))))

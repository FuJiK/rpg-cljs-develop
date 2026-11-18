(ns rpg.data.map-change-test
  (:require [cljs.test :refer [deftest is use-fixtures]]
            ["fs" :as fs]
            ["path" :as path]
            [cljs.reader :as rdr]
            [rpg.data.spec :as Spec]
            [rpg.engine.world :as W]
            [rpg.engine.state :as S]
            [rpg.test-util :as TU]))

(use-fixtures :each {:before TU/reset-state!})

(defn- read-edn [rel]
  (rdr/read-string (.readFileSync fs (path/join (TU/project-root) rel) "utf8")))

(deftest akari-village-and-windpass-validate
  (is (= (read-edn "data/maps/akari-village.edn")
         (Spec/validate! Spec/MapDoc (read-edn "data/maps/akari-village.edn") "akari-village")))
  (is (= (read-edn "data/maps/windpass.edn")
         (Spec/validate! Spec/MapDoc (read-edn "data/maps/windpass.edn") "windpass"))))

(deftest apply-map-switches-state
  (W/apply-map! (read-edn "data/maps/akari-village.edn") "akari-village")
  (is (= "akari-village" (:current-map @S/state)))
  (is (pos? (count (:objects @S/state))))
  (is (= :chest-001 (get-in @S/state [:events [12 10] :id])))
  (W/apply-map! (read-edn "data/maps/windpass.edn") "windpass")
  (is (= "windpass" (:current-map @S/state)))
  (is (nil? (get-in @S/state [:events [12 10] :id])))
  (swap! S/state assoc-in [:player :x] 10)
  (swap! S/state assoc-in [:player :y] 26)
  (is (= 10 (get-in @S/state [:player :x]))))
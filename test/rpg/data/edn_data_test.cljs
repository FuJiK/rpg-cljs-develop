(ns rpg.data.edn-data-test
  (:require [cljs.test :refer [deftest is testing]]
            ["fs" :as fs]
            ["path" :as path]
            [cljs.reader :as rdr]
            [rpg.data.spec :as Spec]
            [rpg.test-util :as TU]))

(defn- read-edn-file [rel-path]
  (rdr/read-string
   (.readFileSync fs (path/join (TU/project-root) rel-path) "utf8")))

(deftest committed-items-edn-validates
  (let [items (read-edn-file "data/items.edn")]
    (is (= items (Spec/validate! Spec/Items items "Items")))
    (is (contains? (into #{} (map :id items)) :potion))))

(deftest committed-enemies-edn-validates
  (let [enemies (read-edn-file "data/enemies.edn")]
    (is (= enemies (Spec/validate! Spec/Enemies enemies "Enemies")))
    (is (contains? (into #{} (map :id enemies)) :slime))))

(deftest committed-overworld-edn-validates
  (let [m (read-edn-file "data/maps/overworld.edn")]
    (is (= m (Spec/validate! Spec/MapDoc m "MapDoc")))
    (testing "sample overworld content"
      (is (pos? (count (:tiles m))))
      (is (pos? (count (:events m))))
      (is (some #(= (:pos %) [2 0]) (:events m))))))

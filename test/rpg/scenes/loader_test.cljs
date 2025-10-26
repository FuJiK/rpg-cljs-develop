(ns rpg.scenes.loader-test
  (:require [cljs.test :refer [deftest is async use-fixtures]]
            [cljs.reader :as rdr]
            [rpg.engine.state :as S]
            [rpg.data.loader :as L]
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

(def sample-items
  "[{:id :potion :name \"P\" :desc \"d\" :effect {:heal 10}}]")

(def sample-enemies
  "[{:id :slime :name \"S\" :hp 5 :attack 1 :exp 1}]")

(def sample-map
  "{:tiles [[0 0 3] [1 0 1]]
    :events [{:pos [0 0] :event {:id :e1 :once? false :script [[:say \"A\" \"B\"]]}}]}")

(deftest load-all-mocked
  (async done
    (with-redefs [js/fetch
                  (fn [path]
                    (js/Promise.resolve
                      (clj->js
                        {:ok true
                         :text (fn []
                                 (js/Promise.resolve
                                   (cond
                                     (re-find #\"/data/items.edn$\" path) sample-items
                                     (re-find #\"/data/enemies.edn$\" path) sample-enemies
                                     (re-find #\"/data/maps/overworld.edn$\" path) sample-map
                                     :else \"\"))))}))]
      (.then (L/load-all!)
             (fn [_]
               (is (= 3 (M/get-tile [0 0])))
               (is (= 1 (M/get-tile [1 0])))
               (is (contains? (:items-by-id @S/state) :potion))
               (is (contains? (:enemies-by-id @S/state) :slime))
               (done)))))

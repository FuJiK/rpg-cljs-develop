(ns rpg.scenes.loader-test
  (:require [cljs.test :refer [deftest is async use-fixtures]]
            [rpg.engine.state :as S]
            [rpg.data.loader :as L]
            [rpg.engine.map :as M]
            [rpg.test-util :as TU]))

(use-fixtures :each {:before TU/reset-state!})

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
                    (let [body (cond
                                 (re-find #"/data/items.edn$" path) sample-items
                                 (re-find #"/data/enemies.edn$" path) sample-enemies
                                 (re-find #"/data/maps/overworld.edn$" path) sample-map
                                 :else "")]
                      (js/Promise.resolve
                       (clj->js {:ok true
                                 :text (fn [] (js/Promise.resolve body))}))))]
      (.then (L/load-all!)
             (fn [_]
               (is (= 3 (M/get-tile [0 0])))
               (is (= 1 (M/get-tile [1 0])))
               (is (contains? (:items-by-id @S/state) :potion))
               (is (contains? (:enemies-by-id @S/state) :slime))
               (done))))))

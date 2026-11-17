(ns rpg.scenes.loader-test
  (:require [cljs.test :refer [deftest is async use-fixtures]]
            [cljs.reader :as rdr]
            [rpg.engine.state :as S]
            [rpg.data.loader :as L]
            [rpg.engine.map :as M]
            [rpg.engine.world :as W]
            [rpg.test-util :as TU]))

(use-fixtures :each {:before TU/reset-state!})

(def sample-items
  "[{:id :potion :name \"P\" :desc \"d\" :effect {:heal 10}}]")

(def sample-enemies
  "[{:id :slime :name \"S\" :hp 5 :attack 1 :exp 1}]")

(def sample-map
  "{:spawn [0 0] :tiles [[0 0 3] [1 0 1]]
    :events [{:pos [0 0] :event {:id :e1 :once? false :script [[:say \"A\" \"B\"]]}}]}")

(deftest load-all-mocked
  (async done
    (with-redefs [js/fetch
                  (fn [path]
                    (let [body (cond
                                 (re-find #"/data/items.edn$" path) sample-items
                                 (re-find #"/data/enemies.edn$" path) sample-enemies
                                 :else "")]
                      (js/Promise.resolve
                       (clj->js {:ok true
                                 :text (fn [] (js/Promise.resolve body))}))))]
      (.then (js/Promise.all #js [(L/load-items!) (L/load-enemies!)])
             (fn [_]
               (W/apply-map! (rdr/read-string sample-map) "masara")
               (is (= 3 (M/get-tile [0 0])))
               (is (= 1 (M/get-tile [1 0])))
               (is (= "masara" (:current-map @S/state)))
               (is (contains? (:items-by-id @S/state) :potion))
               (is (contains? (:enemies-by-id @S/state) :slime))
               (done))))))
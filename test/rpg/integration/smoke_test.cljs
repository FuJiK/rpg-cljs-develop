(ns rpg.integration.smoke-test
  (:require [cljs.test :refer [deftest is async use-fixtures]]
            ["fs" :as fs]
            ["path" :as path]
            [rpg.data.loader :as L]
            [rpg.engine.map :as M]
            [rpg.engine.state :as S]
            [rpg.test-util :as TU]))

(use-fixtures :each {:before TU/reset-state!})

(defn- read-text-file [rel-path]
  (.readFileSync fs (path/join (TU/project-root) rel-path) "utf8"))

(defn- fetch-from-disk [path]
  (js/Promise.resolve
   (clj->js
    {:ok true
     :text (fn []
             (js/Promise.resolve
              (cond
                (= path "/data/items.edn") (read-text-file "data/items.edn")
                (= path "/data/enemies.edn") (read-text-file "data/enemies.edn")
                (= path "/data/maps/overworld.edn") (read-text-file "data/maps/overworld.edn")
                :else (throw (js/Error. (str "unexpected fetch path: " path))))))})))

(deftest load-all-with-committed-edn
  (async done
    (with-redefs [js/fetch fetch-from-disk]
      (.then (L/load-all!)
             (fn [_]
               (is (contains? (:items-by-id @S/state) :potion))
               (is (contains? (:items-by-id @S/state) :hi-potion))
               (is (contains? (:enemies-by-id @S/state) :slime))
               (is (contains? (:enemies-by-id @S/state) :goblin))
               (is (= :event/hello (get-in @S/state [:events [0 0] :id])))
               (is (= :chest-001 (get-in @S/state [:events [2 0] :id])))
               (is (= 3 (M/get-tile [0 3])))
               (done))))))

(deftest static-app-assets-exist
  (let [root (TU/project-root)]
    (is (.existsSync fs (path/join root "index.html")))
    (is (.existsSync fs (path/join root "js/main.js")))
    (is (pos? (.-size (.statSync fs (path/join root "js/main.js")))))
    (is (re-find #"id=\"game\"" (read-text-file "index.html")))
    (is (re-find #"js/main.js" (read-text-file "index.html")))))

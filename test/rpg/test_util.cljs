(ns rpg.test-util
  (:require [rpg.engine.state :as S]))

(defn base-state []
  {:player {:x 0 :y 0 :dir :down}
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
   :anim {:active? false :t 0 :dur 120 :from [0 0] :to [0 0]}
   :step-count 0
   :rng (random-uuid)})

(defn reset-state! []
  (reset! S/state (base-state)))

(defn project-root []
  (str (js/process.cwd)))

(defn mock-local-storage []
  (let [storage (atom {})]
    #js {:getItem (fn [k] (get @storage k))
         :setItem (fn [k v] (swap! storage assoc k v))
         :removeItem (fn [k] (swap! storage dissoc k))}))

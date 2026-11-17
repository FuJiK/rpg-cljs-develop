(ns rpg.test-util
  (:require [rpg.engine.state :as S]))

(defn base-state []
  {:player {:x 0 :y 0 :dir :down}
   :current-map nil
   :map-name ""
   :map {}
   :solid-tiles #{1 2 4}
   :events {}
   :objects []
   :opened #{}
   :camera {:x 0 :y 0}
   :keys #{}
   :dialog {:open? false :queue []}
   :inventory {}
   :items-by-id {}
   :enemies-by-id {}
   :anim {:active? false :t 0 :dur 120 :from [0 0] :to [0 0]}
   :step-count 0
   :mode :field
   :player-stats {:level 5 :hp 40 :max-hp 40 :attack 12 :defense 4 :skill-power 18}
   :chase nil
   :pending-battle nil
   :transition nil
   :battle nil
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

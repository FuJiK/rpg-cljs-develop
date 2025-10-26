(ns rpg.engine.storage
  (:require [cljs.reader :as rdr]
            [rpg.engine.state :as S]))

(def save-key "rpg.cljs.save.v1")

(defn- pick-savable [st]
  (select-keys st [:player :opened :inventory :map]))

(defn save-game! []
  (let [data (pr-str (pick-savable @S/state))]
    (.setItem js/localStorage save-key data)))

(defn load-game! []
  (when-let [txt (.getItem js/localStorage save-key)]
    (try
      (let [data (rdr/read-string txt)]
        (swap! S/state merge data))
      (catch :default _e nil))))

(defn clear-save! [] (.removeItem js/localStorage save-key))

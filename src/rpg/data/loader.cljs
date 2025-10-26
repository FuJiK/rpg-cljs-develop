(ns rpg.data.loader
  (:require [cljs.reader :as rdr]
            [rpg.data.spec :as S]
            [rpg.engine.state :as EState]
            [rpg.engine.map :as M]
            [rpg.engine.events :as EV]))

(defn- fetch-edn! [path]
  (-> (js/fetch path)
      (.then (fn [res]
               (if (.-ok res) (.text res)
                   (throw (ex-info (str "HTTP " (.-status res) " for " path) {})))))
      (.then (fn [txt] (rdr/read-string txt)))))

(defn load-items! []
  (-> (fetch-edn! "/data/items.edn")
      (.then (fn [items]
               (S/validate! S/Items items "Items")
               (swap! EState/state assoc :items-by-id (into {} (map (juxt :id identity) items)))
               items))))

(defn load-enemies! []
  (-> (fetch-edn! "/data/enemies.edn")
      (.then (fn [enemies]
               (S/validate! S/Enemies enemies "Enemies")
               (swap! EState/state assoc :enemies-by-id (into {} (map (juxt :id identity) enemies)))
               enemies))))

(defn apply-map! [m]
  (S/validate! S/MapDoc m "MapDoc")
  (doseq [[x y tid] (:tiles m)] (M/set-tile! [x y] tid))
  (doseq [{:keys [pos event]} (:events m)]
    (let [[x y] pos]
      (S/validate! S/MapEvent event "MapEvent")
      (EV/register! x y event))))

(defn load-map! [name]
  (-> (fetch-edn! (str "/data/maps/" name ".edn"))
      (.then (fn [m] (apply-map! m) m))))

(defn load-all! []
  (-> (js/Promise.all #js [(load-items!) (load-enemies!) (load-map! "overworld")])
      (.then (fn [_] true))))

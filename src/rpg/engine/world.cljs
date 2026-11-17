(ns rpg.engine.world
  (:require [cljs.reader :as rdr]
            [rpg.data.spec :as Spec]
            [rpg.engine.state :as S]
            [rpg.engine.map :as M]
            [rpg.engine.dialog :as D]))

(defn- fetch-edn! [path]
  (-> (js/fetch path)
      (.then (fn [res]
               (if (.-ok res) (.text res)
                   (throw (ex-info (str "HTTP " (.-status res) " for " path) {})))))
      (.then (fn [txt] (rdr/read-string txt)))))

(defn reset-map-layer! []
  (swap! S/state assoc :map {} :events {} :objects []))

(defn apply-map! [m map-id]
  (Spec/validate! Spec/MapDoc m "MapDoc")
  (reset-map-layer!)
  (swap! S/state assoc
         :current-map map-id
         :map-name (or (:name m) "")
         :solid-tiles (if-let [st (:solid-tiles m)]
                        (set st)
                        S/default-solid-tiles)
         :objects (vec (or (:objects m) [])))
  (doseq [[x y tid] (:tiles m)] (M/set-tile! [x y] tid))
  (doseq [{:keys [pos event]} (or (:events m) [])]
    (let [[x y] pos]
      (Spec/validate! Spec/MapEvent event "MapEvent")
      (swap! S/state assoc-in [:events [x y]] event)))
  (when-let [banner (:name m)]
    (D/show! "場所" banner))
  m)

(defn load-map! [name]
  (-> (fetch-edn! (str "/data/maps/" name ".edn"))
      (.then (fn [m]
               (apply-map! m name)
               m))))

(defn change-map! [name x y]
  (-> (load-map! name)
      (.then (fn [m]
               (let [[spawn-x spawn-y] (if (and (some? x) (some? y))
                                          [x y]
                                          (or (:spawn m) [0 0]))]
                 (swap! S/state assoc-in [:player :x] spawn-x)
                 (swap! S/state assoc-in [:player :y] spawn-y))))))
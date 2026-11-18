(ns rpg.data.loader
  (:require [cljs.reader :as rdr]
            [rpg.data.spec :as Spec]
            [rpg.engine.state :as EState]
            [rpg.engine.world :as W]))

(defn- fetch-edn! [path]
  (-> (js/fetch path)
      (.then (fn [res]
               (if (.-ok res) (.text res)
                   (throw (ex-info (str "HTTP " (.-status res) " for " path) {})))))
      (.then (fn [txt] (rdr/read-string txt)))))

(defn load-items! []
  (-> (fetch-edn! "/data/items.edn")
      (.then (fn [items]
               (Spec/validate! Spec/Items items "Items")
               (swap! EState/state assoc :items-by-id (into {} (map (juxt :id identity) items)))
               items))))

(defn load-enemies! []
  (-> (fetch-edn! "/data/enemies.edn")
      (.then (fn [enemies]
               (Spec/validate! Spec/Enemies enemies "Enemies")
               (swap! EState/state assoc :enemies-by-id (into {} (map (juxt :id identity) enemies)))
               enemies))))

(defn load-map! [name] (W/load-map! name))

(defn change-map! [name x y] (W/change-map! name x y))

(defn reset-map-layer! [] (W/reset-map-layer!))

(defn apply-map! [m map-id] (W/apply-map! m map-id))

(defn load-all! []
  (-> (js/Promise.all #js [(load-items!) (load-enemies!)])
      (.then (fn [_]
               (let [st @EState/state
                     map-name (or (:current-map st) "akari-village")
                     saved-pos (when (:current-map st)
                                 (select-keys (:player st) [:x :y]))]
                 (-> (W/change-map! map-name nil nil)
                     (.then (fn [_]
                              (when saved-pos
                                (swap! EState/state update :player merge saved-pos))))))))))
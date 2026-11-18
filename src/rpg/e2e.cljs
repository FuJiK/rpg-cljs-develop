(ns rpg.e2e
  (:require [rpg.engine.state :as S]))

(defn- snapshot []
  (let [{:keys [open? queue]} (:dialog @S/state)]
    #js {:ready true
         :currentMap (or (:current-map @S/state) "")
         :mapName (:map-name @S/state)
         :mode (name (or (:mode @S/state) :field))
         :player (clj->js (:player @S/state))
         :dialog #js {:open (boolean open?)
                      :queueSize (count (or queue []))}
         :opened (clj->js (vec (:opened @S/state)))}))

(defn enabled? []
  (true? (.-__RPG_E2E__ js/window)))

(defn install! []
  (when (enabled?)
    (aset js/window "__rpg"
          #js {:getState snapshot
               :ready false})
    (js/console.info "[rpg.e2e] test hook installed")))

(defn mark-ready! []
  (when (enabled?)
    (when-let [api (.-__rpg js/window)]
      (aset api "ready" true))))
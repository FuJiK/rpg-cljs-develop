(ns rpg.engine.reset
  (:require [rpg.engine.state :as S]
            [rpg.engine.storage :as Store]
            [rpg.engine.world :as W]
            [rpg.engine.input :as I]
            [rpg.engine.render :as R]
            [rpg.engine.map :as M]))

(def default-player-stats
  {:level 5 :hp 40 :max-hp 40 :attack 12 :defense 4 :skill-power 18})

(defn clear-progress! []
  (Store/clear-save!)
  (I/clear-keys!)
  (swap! S/state
         (fn [s]
           (assoc s
                  :opened #{}
                  :inventory {}
                  :player-stats default-player-stats
                  :mode :field
                  :chase nil
                  :pending-battle nil
                  :transition nil
                  :battle nil
                  :post-battle-scroll nil
                  :dialog {:open? false :queue []}
                  :keys #{}
                  :step-count 0
                  :anim {:active? false :t 0 :dur 120 :from [0 0] :to [0 0]}
                  :current-map nil
                  :player {:x 0 :y 0 :dir :down}))))

(defn reset-game! []
  (clear-progress!)
  (-> (W/change-map! "masara" nil nil)
      (.then
       (fn [_]
         (when-let [dlg (when (exists? js/document)
                           (.getElementById js/document "dialog"))]
           (set! (.-style.display dlg) "none"))
         (let [[fx fy] (M/interp-pos)]
           (R/update-camera! fx fy)
           (R/draw!))))))
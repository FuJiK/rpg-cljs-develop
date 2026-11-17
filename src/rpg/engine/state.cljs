(ns rpg.engine.state)

(def tile-size 32)
(def view-w 20)
(def view-h 15)

(def default-solid-tiles #{1 2 4})

(defonce state
  (atom {:player {:x 0 :y 0 :dir :down}
         :current-map nil
         :map-name ""
         :map {}
         :solid-tiles default-solid-tiles
         :events {}
         :objects []
         :opened #{}
         :camera {:x 0 :y 0}
         :keys #{}
         :dialog {:open? false :queue []}
         :inventory {}
         :items-by-id {}
         :enemies-by-id {}
         ;; 進んでる感：移動アニメ状態（ミリ秒）
         :anim {:active? false
                :t 0              ;; 現在時刻
                :dur 120          ;; 所要時間(ms)
                :from [0 0]
                :to   [0 0]}
         :step-count 0            ;; 歩数カウンタ
         :mode :field             ;; :field | :battle
         :player-stats {:level 5 :hp 40 :max-hp 40 :attack 12 :defense 4 :skill-power 18}
         :chase nil
         :pending-battle nil
         :transition nil
         :battle nil
         :post-battle-scroll nil
         :rng (random-uuid)}))

(defn mark-done! [id] (swap! state update :opened conj id))
(defn done? [id] (contains? (:opened @state) id))

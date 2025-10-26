(ns rpg.engine.state)

(def tile-size 32)
(def view-w 20)
(def view-h 15)

(defonce state
  (atom {:player {:x 0 :y 0 :dir :down}
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
         ;; 進んでる感：移動アニメ状態（ミリ秒）
         :anim {:active? false
                :t 0              ;; 現在時刻
                :dur 120          ;; 所要時間(ms)
                :from [0 0]
                :to   [0 0]}
         :step-count 0            ;; 歩数カウンタ
         :rng (random-uuid)}))

(defn mark-done! [id] (swap! state update :opened conj id))
(defn done? [id] (contains? (:opened @state) id))

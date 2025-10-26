(ns rpg.engine.map
  (:require
   [rpg.engine.state :as S]))

(defn get-tile [[x y]] (get-in @S/state [:map [x y]] 0))
(defn set-tile! [[x y] tid] (swap! S/state assoc-in [:map [x y]] tid))
(defn ensure-generated! [[x y]]
  (when-not (contains? (:map @S/state) [x y])
    (set-tile! [x y] 0)))

(defn collides? [[x y]] (contains? (:solid-tiles @S/state) (get-tile [x y])))

(defn next-pos [{:keys [x y]} dir]
  (case dir
    :left  [(dec x) y]
    :right [(inc x) y]
    :up    [x (dec y)]
    :down  [x (inc y)]
    [x y]))

;; キー入力から1歩だけ“移動開始”する（位置の確定はloop側でアニメ完了時に行う）
(defn try-start-move! []
  (let [{:keys [keys anim]} @S/state
        moving? (:active? anim)
        dir (cond
              (contains? keys :left)  :left
              (contains? keys :right) :right
              (contains? keys :up)    :up
              (contains? keys :down)  :down
              :else nil)]
    (when (and (not moving?) dir)
      (swap! S/state update :player assoc :dir dir)
      (let [{:keys [player]} @S/state
            [x y] [(:x player) (:y player)]
            to (case dir
                 :left  [(dec x) y]
                 :right [(inc x) y]
                 :up    [x (dec y)]
                 :down  [x (inc y)])]
        (when-not (contains? (:solid-tiles @S/state) (get-in @S/state [:map to]))
          (swap! S/state assoc :anim {:active? true
                                      :t 0
                                      :dur (get-in @S/state [:anim :dur] 120)
                                      :from [x y]
                                      :to   to}))))))

;; アニメ中の補間位置（小数）を返す
(defn interp-pos []
  (let [{:keys [player anim]} @S/state
        {:keys [active? t dur from to]} anim]
    (if active?
      (let [a (min 1.0 (/ (double t) (double (max 1 dur))))
            [fx fy] from
            [tx ty] to]
        [(+ fx (* (- tx fx) a))
         (+ fy (* (- ty fy) a))])
      [(:x player) (:y player)])))

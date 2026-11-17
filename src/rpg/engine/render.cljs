(ns rpg.engine.render
  (:require [rpg.engine.state :as S]
            [rpg.engine.map :as M]))

(defn tile-color [tid]
  (case tid
    0 "#6cb36c"  ;; 草
    1 "#d8e8d8"  ;; 柵・外枠
    2 "#2b65b1"  ;; 水
    3 "#c8b08a"  ;; 道
    4 "#9e8b72"  ;; 建物
    5 "#5a4a3a"  ;; 屋根／木
    "#444"))

(defn update-camera! [fx fy]
  (let [pad-x (quot S/view-w 2)
        pad-y (quot S/view-h 2)]
    (swap! S/state assoc :camera {:x (int (- fx pad-x))
                                  :y (int (- fy pad-y))})))

(defn- draw-hud! [ctx]
  (let [{:keys [player step-count map-name]} @S/state
        place (when (not-empty map-name) (str " / " map-name))
        txt (str "歩数: " step-count " / 座標: (" (:x player) "," (:y player) ")" place)]
    (set! (.-font ctx) "14px system-ui, sans-serif")
    (set! (.-fillStyle ctx) "rgba(0,0,0,0.6)")
    (.fillRect ctx 12 50 360 26)
    (set! (.-fillStyle ctx) "#EEE")
    (.fillText ctx txt 20 68)))

(defn- draw-object! [ctx vx vy {:keys [kind id]}]
  (let [sx (* vx S/tile-size)
        sy (* vy S/tile-size)
        pad 6
        sz (- S/tile-size pad pad)]
    (case kind
      :chest (let [opened? (and id (S/done? id))]
               (set! (.-fillStyle ctx) (if opened? "#6b5344" "#8d6e63"))
               (.fillRect ctx (+ sx pad) (+ sy pad 6) sz (- sz 6))
               (set! (.-fillStyle ctx) (if opened? "#5d4037" "#ffca28"))
               (.fillRect ctx (+ sx pad) (+ sy pad) sz 8))
      :sign (do
              (set! (.-fillStyle ctx) "#795548")
              (.fillRect ctx (+ sx 14) (+ sy 8) 4 (- sz 4))
              (set! (.-fillStyle ctx) "#efebe9")
              (.fillRect ctx (+ sx 6) (+ sy 6) 20 10))
      :gate (do
              (set! (.-fillStyle ctx) "#81c784")
              (.fillRect ctx (+ sx 4) sy 8 S/tile-size)
              (.fillRect ctx (+ sx (- S/tile-size 12)) sy 8 S/tile-size)
              (set! (.-fillStyle ctx) "#a5d6a7")
              (.fillRect ctx (+ sx 10) (+ sy 4) 12 8))
      :npc (do
             (set! (.-fillStyle ctx) "#5c6bc0")
             (.beginPath ctx)
             (.arc ctx (+ sx (/ S/tile-size 2)) (+ sy (/ S/tile-size 2))
                     (- (/ S/tile-size 2) 4) 0 (* 2 Math/PI))
             (.fill ctx))
      nil)))

(defn- draw-objects! [ctx cam-x cam-y]
  (doseq [{:keys [pos] :as obj} (:objects @S/state)
          :let [[ox oy] pos
                vx (- ox cam-x)
                vy (- oy cam-y)]
          :when (and (<= 0 vx (dec S/view-w))
                     (<= 0 vy (dec S/view-h)))]
    (draw-object! ctx vx vy obj)))

(defn draw! []
  (let [canvas (.getElementById js/document "game")
        ctx (.getContext canvas "2d")
        [fx fy] (M/interp-pos)
        cam (:camera @S/state)
        cam-x (:x cam) cam-y (:y cam)]
    (.clearRect ctx 0 0 (.-width canvas) (.-height canvas))
    (doseq [vy (range S/view-h)
            vx (range S/view-w)]
      (let [tx (+ cam-x vx)
            ty (+ cam-y vy)]
        (M/ensure-generated! [tx ty])
        (let [tid (M/get-tile [tx ty])]
          (set! (.-fillStyle ctx) (tile-color tid))
          (.fillRect ctx (* vx S/tile-size) (* vy S/tile-size) S/tile-size S/tile-size))))
    (draw-objects! ctx cam-x cam-y)
    (let [px (- fx cam-x)
          py (- fy cam-y)
          sx (* px S/tile-size)
          sy (* py S/tile-size)]
      (set! (.-fillStyle ctx) "#ffd54a")
      (.fillRect ctx sx sy S/tile-size S/tile-size)
      (set! (.-strokeStyle ctx) "rgba(255,255,255,0.7)")
      (set! (.-lineWidth ctx) 2)
      (.strokeRect ctx (inc sx) (inc sy) (- S/tile-size 2) (- S/tile-size 2)))
    (draw-hud! ctx)))
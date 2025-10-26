(ns rpg.engine.render
  (:require [rpg.engine.state :as S]
            [rpg.engine.map :as M]))

(defn tile-color [tid]
  (case tid
    0 "#35682d" 1 "#7b7b7b" 2 "#2b65b1" 3 "#a38f65" "#444"))

;; ★ 2引数です（fx fy）
(defn update-camera! [fx fy]
  (let [pad-x (quot S/view-w 2)
        pad-y (quot S/view-h 2)]
    (swap! S/state assoc :camera {:x (int (- fx pad-x))
                                  :y (int (- fy pad-y))})))

(defn- draw-hud! [ctx]
  (let [{:keys [player step-count]} @S/state
        txt (str "歩数: " step-count " / 座標: (" (:x player) "," (:y player) ")")]
    (set! (.-font ctx) "14px system-ui, sans-serif")
    (set! (.-fillStyle ctx) "rgba(0,0,0,0.6)")
    (.fillRect ctx 12 50 280 26)
    (set! (.-fillStyle ctx) "#EEE")
    (.fillText ctx txt 20 68)))

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

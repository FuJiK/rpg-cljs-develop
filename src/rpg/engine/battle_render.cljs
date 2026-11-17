(ns rpg.engine.battle-render
  (:require [rpg.engine.battle :as B]
            [rpg.engine.state :as S]))

(defn- bar! [ctx x y w h frac color]
  (set! (.-fillStyle ctx) "#333")
  (.fillRect ctx x y w h)
  (set! (.-fillStyle ctx) color)
  (.fillRect ctx x y (* w (max 0 (min 1 frac))) h))

(defn draw! [ctx]
  (when (B/in-battle?)
    (let [w (.-width ctx)
          h (.-height ctx)
          ps (B/player-stats)
          {:keys [enemy-name enemy-hp enemy-max-hp menu-idx message phase]} (:battle @S/state)]
      (set! (.-fillStyle ctx) "#1a2744")
      (.fillRect ctx 0 0 w h)
      ;; 敵
      (set! (.-fillStyle ctx) "#e57373")
      (.fillRect ctx 380 80 140 140)
      (set! (.-fillStyle ctx) "#fff")
      (set! (.-font ctx) "bold 16px system-ui, sans-serif")
      (.fillText ctx (str enemy-name) 360 60)
      (bar! ctx 340 40 200 12 (/ enemy-hp enemy-max-hp) "#f44336")
      ;; プレイヤー
      (set! (.-fillStyle ctx) "#ffd54a")
      (.fillRect ctx 80 260 120 120)
      (set! (.-fillStyle ctx) "#fff")
      (.fillText ctx "あなた" 90 240)
      (bar! ctx 60 220 200 12 (/ (:hp ps) (:max-hp ps)) "#4caf50")
      (set! (.-font ctx) "13px system-ui, sans-serif")
      (.fillText ctx (str "Lv" (:level ps) " HP:" (:hp ps) "/" (:max-hp ps)) 60 205)
      ;; メッセージ
      (set! (.-fillStyle ctx) "rgba(0,0,0,0.75)")
      (.fillRect ctx 20 (- h 130) (- w 40) 90)
      (set! (.-fillStyle ctx) "#eee")
      (set! (.-font ctx) "15px system-ui, sans-serif")
      (.fillText ctx (or message "どうする？") 32 (- h 95))
      ;; コマンド（4つ）
      (when (= phase :menu)
        (doseq [[i cmd] (map-indexed vector B/menu-items)
                :let [lx (+ 32 (* (mod i 2) 140))
                      ly (+ (- h 200) (* (quot i 2) 36))
                      selected? (= i menu-idx)]]
          (set! (.-fillStyle ctx) (if selected? "#ffeb3b" "#37474f"))
          (.fillRect ctx lx ly 120 28)
          (set! (.-fillStyle ctx) (if selected? "#000" "#fff"))
          (set! (.-font ctx) "14px system-ui, sans-serif")
          (.fillText ctx (B/menu-label cmd) (+ lx 12) (+ ly 20)))))))
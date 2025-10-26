(ns rpg.engine.input
  (:require [rpg.engine.state :as S]
            [clojure.string :as str]))

;; OS/ブラウザ差で揺れる key / code / keyCode すべてを吸収
(defn ^:private event->kw [e]
  (let [k    (.-key e)                      ;; 例: "ArrowLeft", "a", " "
        code (.-code e)                     ;; 例: "ArrowLeft", "KeyA", "Space"
        kc   (.-keyCode e)                  ;; 例: 37, 38, 39, 40, 32, 13
        lk   (some-> k str/lower-case)]
    (cond
      ;; Arrow / 方向キー
      (or (= lk "arrowleft")  (= lk "left")  (= code "ArrowLeft")  (= kc 37)) :left
      (or (= lk "arrowright") (= lk "right") (= code "ArrowRight") (= kc 39)) :right
      (or (= lk "arrowup")    (= lk "up")    (= code "ArrowUp")    (= kc 38)) :up
      (or (= lk "arrowdown")  (= lk "down")  (= code "ArrowDown")  (= kc 40)) :down
      ;; WASD
      (or (= lk "a") (= code "KeyA")) :left
      (or (= lk "d") (= code "KeyD")) :right
      (or (= lk "w") (= code "KeyW")) :up
      (or (= lk "s") (= code "KeyS")) :down
      ;; 決定（Space / Enter）
      (or (= k " ") (= lk "space") (= lk "spacebar") (= code "Space") (= kc 32) (= kc 13)) :accept
      :else nil)))

(defn on-key [e pressed?]
  (when-let [kw (event->kw e)]
    ;; スクロール等を止める（特に Space / Arrow）
    (.preventDefault e)
    (swap! S/state update :keys (fn [ks] (if pressed? (conj ks kw) (disj ks kw))))))

(defn attach! []
  ;; passive:false で preventDefault を確実に効かせる
  (.addEventListener js/window "keydown" #(on-key % true)  #js {:passive false})
  (.addEventListener js/window "keyup"   #(on-key % false) #js {:passive false}))

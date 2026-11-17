(ns rpg.engine.input
  (:require [rpg.engine.state :as S]
            [rpg.engine.dialog :as D]
            [rpg.engine.battle :as B]
            [rpg.engine.transition :as TR]
            [clojure.string :as str]))

(defonce ^:private ime-composing? (atom false))

(defn clear-keys! []
  (swap! S/state assoc :keys #{}))

(defn- ime-event? [e]
  (or @ime-composing?
      (true? (.-isComposing e))
      (= 229 (.-keyCode e))
      (= "Process" (.-key e))))

(defn- field-movement-allowed? []
  (and (not (get-in @S/state [:dialog :open?]))
       (not (B/in-battle?))
       (not (TR/active?))))

;; OS/ブラウザ差で揺れる key / code / keyCode を吸収（ゲーム操作のみ）
(defn ^:private event->kw [e]
  (let [k    (.-key e)
        code (.-code e)
        kc   (.-keyCode e)
        lk   (some-> k str/lower-case)]
    (cond
      (or (= lk "arrowleft")  (= lk "left")  (= code "ArrowLeft")  (= kc 37)) :left
      (or (= lk "arrowright") (= lk "right") (= code "ArrowRight") (= kc 39)) :right
      (or (= lk "arrowup")    (= lk "up")    (= code "ArrowUp")    (= kc 38)) :up
      (or (= lk "arrowdown")  (= lk "down")  (= code "ArrowDown")  (= kc 40)) :down
      ;; WASD（矢印と同様。IME 変換中は on-key 側で無視）
      (or (= lk "a") (= code "KeyA")) :left
      (or (= lk "d") (= code "KeyD")) :right
      (or (= lk "w") (= code "KeyW")) :up
      (or (= lk "s") (= code "KeyS")) :down
      ;; 決定は Space のみ（Enter は日本語 IME の確定に使うため割り当てない）
      (or (= k " ") (= lk "space") (= lk "spacebar") (= code "Space") (= kc 32)) :accept
      :else nil)))

(defn- consume-game-key? [kw pressed?]
  (when pressed?
    (cond
      (and (= kw :accept) (get-in @S/state [:dialog :open?])) true
      (B/in-battle?) (not= kw nil)
      (and (field-movement-allowed?) #{:left :right :up :down :accept} kw) true
      :else false)))

(defn on-key [e pressed?]
  (when-not (ime-event? e)
    (when-let [kw (event->kw e)]
      (let [consume? (consume-game-key? kw pressed?)]
        (when consume?
          (.preventDefault e))
        (when pressed?
          (cond
            (and (= kw :accept) (get-in @S/state [:dialog :open?]))
            (D/advance!)

            (B/in-battle?)
            (case kw
              :up (B/select-menu! (- (get-in @S/state [:battle :menu-idx] 0) 2))
              :down (B/select-menu! (+ (get-in @S/state [:battle :menu-idx] 0) 2))
              :left (B/select-menu! (dec (get-in @S/state [:battle :menu-idx] 0)))
              :right (B/select-menu! (inc (get-in @S/state [:battle :menu-idx] 0)))
              :accept (B/confirm-menu!)
              nil)))
        (when (field-movement-allowed?)
          (swap! S/state update :keys
                 (fn [ks]
                   (if pressed?
                     (if (= kw :accept) ks (conj ks kw))
                     (disj ks kw)))))))))

(defn attach! []
  (.addEventListener js/window "compositionstart"
                     (fn [_] (reset! ime-composing? true)))
  (.addEventListener js/window "compositionend"
                     (fn [_] (reset! ime-composing? false)))
  (.addEventListener js/window "blur" (fn [_] (clear-keys!)))
  (.addEventListener js/window "keydown" #(on-key % true) #js {:passive false})
  (.addEventListener js/window "keyup" #(on-key % false) #js {:passive false}))
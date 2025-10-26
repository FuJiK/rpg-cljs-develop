(ns rpg.engine.loop
  (:require [rpg.engine.state  :as S]
            [rpg.engine.map    :as M]
            [rpg.engine.events :as EV]
            [rpg.engine.render :as R]))

(defn ^:private tick! [dt-ms]
  (let [st @S/state
        dialog-open? (get-in st [:dialog :open?])
        anim         (:anim st)
        active?      (:active? anim)]

    ;; ダイアログ中は描画だけ
    (when dialog-open?
      (let [[fx fy] (M/interp-pos)]
        (R/update-camera! fx fy)
        (R/draw!)))

    ;; 通常
    (when-not dialog-open?
      (if active?
        ;; アニメ進行
        (let [{:keys [t dur to]} anim
              nt (+ (long t) (long dt-ms))]
          (when (>= nt dur)
            (let [[tx ty] to]
              (swap! S/state
                     (fn [s]
                       (-> s
                           (assoc-in [:player :x] tx)
                           (assoc-in [:player :y] ty)
                           (assoc :anim (assoc (:anim s)
                                               :active? false
                                               :t 0
                                               :from to
                                               :to to))
                           (update :step-count inc)))))
            (let [st2 @S/state
                  x (get-in st2 [:player :x])
                  y (get-in st2 [:player :y])]
              (EV/trigger-at! x y)))
          (when (< nt dur)
            (swap! S/state assoc-in [:anim :t] nt)))
        ;; アニメしていない：移動開始
        (M/try-start-move!))

      ;; カメラ追従＆描画
      (let [[fx fy] (M/interp-pos)]
        (R/update-camera! fx fy)
        (R/draw!)))))

(defn step!
  ([] (step! (js/performance.now)))
  ([last-ts]
   (let [now (js/performance.now)
         dt  (- now last-ts)]
     ;; ここで例外を握りつぶさず Console に出す
     (try
       (tick! dt)
       (catch :default e
         (js/console.error "[loop/step] tick! crashed" e)))
     (js/requestAnimationFrame #(step! now)))))

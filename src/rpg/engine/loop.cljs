(ns rpg.engine.loop
  (:require [rpg.engine.state :as S]
            [rpg.engine.map :as M]
            [rpg.engine.events :as EV]
            [rpg.engine.render :as R]
            [rpg.engine.chase :as CH]
            [rpg.engine.transition :as TR]
            [rpg.engine.battle :as B]))

(defn- maybe-start-battle-transition! []
  (when (and (not (get-in @S/state [:dialog :open?]))
             (:pending-battle @S/state)
             (not (TR/active?))
             (not (B/in-battle?)))
    (TR/start-scroll-to-battle!)))

(defn- maybe-post-battle-scroll! []
  (when (and (:post-battle-scroll @S/state)
             (not (TR/active?))
             (not (B/in-battle?)))
    (swap! S/state dissoc :post-battle-scroll)
    (TR/start-scroll-to-field!)))

(defn ^:private tick-overworld! [dt-ms]
  (let [st @S/state
        dialog-open? (get-in st [:dialog :open?])
        anim (:anim st)
        active? (:active? anim)]
    (when dialog-open?
      (let [[fx fy] (M/interp-pos)]
        (R/update-camera! fx fy)
        (R/draw!)))
    (when-not dialog-open?
      (if active?
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
            (CH/on-player-stepped!)
            (let [st2 @S/state
                  x (get-in st2 [:player :x])
                  y (get-in st2 [:player :y])]
              (EV/trigger-at! x y)))
          (when (< nt dur)
            (swap! S/state assoc-in [:anim :t] nt)))
        (M/try-start-move!))
      (maybe-start-battle-transition!)
      (let [[fx fy] (M/interp-pos)]
        (R/update-camera! fx fy)
        (R/draw!)))))

(defn ^:private tick! [dt-ms]
  (TR/tick! dt-ms)
  (maybe-post-battle-scroll!)
  (cond
    (B/in-battle?)
    (do (B/tick! dt-ms)
        (R/draw!))

    (TR/active?)
    (do
      (maybe-start-battle-transition!)
      (let [[fx fy] (M/interp-pos)]
        (R/update-camera! fx fy)
        (R/draw!)))

    :else
    (tick-overworld! dt-ms)))

(defn step!
  ([] (step! (js/performance.now)))
  ([last-ts]
   (let [now (js/performance.now)
         dt (- now last-ts)]
     (try
       (tick! dt)
       (catch :default e
         (js/console.error "[loop/step] tick! crashed" e)))
     (js/requestAnimationFrame #(step! now)))))
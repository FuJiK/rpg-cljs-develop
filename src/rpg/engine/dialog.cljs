(ns rpg.engine.dialog
  (:require [rpg.engine.state :as S]))

(defn show! [name text]
  (swap! S/state update-in [:dialog :queue] conj {:name name :text text})
  (swap! S/state assoc-in [:dialog :open?] true)
  (let [dlg (.getElementById js/document "dialog")]
    (set! (.-style.display dlg) "block")))

(defn advance! []
  (let [{:keys [queue]} (:dialog @S/state)
        q (vec queue)]
    (if (empty? q)
      (do
        (swap! S/state assoc-in [:dialog :open?] false)
        (let [dlg (.getElementById js/document "dialog")]
          (set! (.-style.display dlg) "none")))
      (let [{:keys [name text]} (first q)
            nm (.getElementById js/document "dlg-name")
            tx (.getElementById js/document "dlg-text")]
        (set! (.-innerText nm) (or name ""))
        (set! (.-innerText tx) (or text ""))
        (swap! S/state assoc-in [:dialog :queue] (subvec q 1))))))

(ns rpg.engine.dialog-test
  (:require [cljs.test :refer [deftest is use-fixtures]]
            [rpg.engine.state :as S]
            [rpg.engine.dialog :as D]
            [rpg.engine.input :as I]
            [rpg.test-util :as TU]))

(defn- mock-dialog-el []
  (let [el #js {:style #js {:display "none"}
                :innerText ""}]
    (set! (.-innerText el) "")
    el))

(defn- mock-document []
  #js {:getElementById (fn [id]
                         (when (contains? #{"dialog" "dlg-name" "dlg-text"} id)
                           (mock-dialog-el)))})

(use-fixtures :each
  (fn [f]
    (TU/reset-state!)
    (set! js/document (mock-document))
    (f)))

(deftest advance-shows-and-closes
  (D/show! "村人" "1行目")
  (D/show! "村人" "2行目")
  (is (get-in @S/state [:dialog :open?]))
  (is (= 2 (count (get-in @S/state [:dialog :queue]))))
  (D/advance!)
  (is (= 1 (count (get-in @S/state [:dialog :queue]))))
  (D/advance!)
  (is (empty? (get-in @S/state [:dialog :queue])))
  (is (get-in @S/state [:dialog :open?]))
  (D/advance!)
  (is (not (get-in @S/state [:dialog :open?]))))

(deftest accept-key-advances-dialog
  (D/show! "SYSTEM" "テスト")
  (let [space-down #js {:key " " :code "Space" :keyCode 32
                        :preventDefault (fn [] nil)}]
    (I/on-key space-down true)
    (is (empty? (get-in @S/state [:dialog :queue])))
    (is (get-in @S/state [:dialog :open?]))
    (I/on-key space-down true)
    (is (not (get-in @S/state [:dialog :open?]))))
  (is (not (contains? (:keys @S/state) :accept))))
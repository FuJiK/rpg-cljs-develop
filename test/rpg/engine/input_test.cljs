(ns rpg.engine.input-test
  (:require [cljs.test :refer-macros [deftest is]]
            [rpg.test-util :as TU]
            [rpg.engine.state :as S]
            [rpg.engine.input :as I]
            [rpg.engine.battle :as B]))

(deftest battle-arrows-do-not-stick-to-field-keys
  (TU/reset-state!)
  (swap! S/state assoc :mode :battle
         :battle {:menu-idx 0 :phase :menu})
  (let [left #js {:key "ArrowLeft" :code "ArrowLeft" :keyCode 37
                  :isComposing false
                  :preventDefault (fn [] nil)}]
    (I/on-key left true)
    (is (not (contains? (:keys @S/state) :left))))
  (swap! S/state assoc :mode :field :battle nil)
  (I/clear-keys!)
  (is (empty? (:keys @S/state))))

(deftest ime-composing-ignored
  (TU/reset-state!)
  (let [e #js {:key "a" :code "KeyA" :keyCode 229
               :isComposing true
               :preventDefault (fn [] nil)}]
    (I/on-key e true)
    (is (empty? (:keys @S/state)))))
(ns rpg.scenes.overworld
  (:require
   [rpg.data.loader :as L]))

(defn seed! [] (L/load-all!))

(ns rpg.data.spec
  (:require [malli.core :as m]
            [malli.error :as me]))

(def Coord [:tuple int? int?])
(def Keyword-id keyword?)

(def Item
  [:map
   [:id Keyword-id]
   [:name string?]
   [:desc string?]
   [:effect [:map
             [:heal {:optional true} int?]
             [:buff {:optional true} [:map-of keyword? any?]]]]])

(def Items [:vector Item])

(def Enemy
  [:map
   [:id Keyword-id]
   [:name string?]
   [:hp pos-int?]
   [:attack pos-int?]
   [:exp pos-int?]
   [:drop {:optional true} [:tuple Keyword-id number?]]])

(def Enemies [:vector Enemy])

(def Op
  [:or
   [:catn [:tag [:= :say]] [:name string?] [:text string?]]
   [:catn [:tag [:= :chest]] [:id Keyword-id] [:payload [:map [:item Keyword-id] [:count pos-int?]]]]
   [:catn [:tag [:= :warp]] [:x int?] [:y int?]]
   [:catn [:tag [:= :give-item]] [:item Keyword-id] [:count pos-int?]]])

(def Script [:vector Op])

(def MapEvent [:map [:id Keyword-id] [:once? boolean?] [:script Script]])

(def TileTriplet [:tuple int? int? int?])

(def MapEventAt [:map [:pos Coord] [:event MapEvent]])

(def MapDoc [:map [:tiles [:vector TileTriplet]] [:events [:vector MapEventAt]]])

(defn validate! [schema value label]
  (when-not (m/validate schema value)
    (throw (ex-info (str "Invalid " label ": " (me/humanize (m/explain schema value))) {:label label :value value})))
  value)

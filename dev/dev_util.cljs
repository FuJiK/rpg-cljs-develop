(ns dev.dev-util
  (:require
   [rpg.data.loader :as L]
   [rpg.engine.dialog :as D]
   [rpg.engine.events :as E]
   [rpg.engine.render :as R]
   [rpg.engine.state :as S]))

;; ========== プレイヤー移動 ==========
(defn tp!
  "プレイヤーを座標 (x, y) に瞬間移動"
  [x y]
  (swap! S/state assoc-in [:player :x] x)
  (swap! S/state assoc-in [:player :y] y)
  (R/update-camera! x y)
  (R/draw!)
  (println (str "[tp!] moved to " [x y])))

;; ========== ダイアログ関連 ==========
(defn say!
  "ダイアログを表示"
  ([text] (D/show! "Dev" text))
  ([name text] (D/show! name text)))

;; ========== イベント発火 ==========
(defn here!
  "現在座標のイベントを強制発火"
  []
  (let [{:keys [x y]} (:player @S/state)]
    (E/trigger-at! x y)))

;; ========== データ再読込 ==========
(defn reload!
  "EDNデータをキャッシュ無視で再読込"
  []
  (-> (L/load-all!)
      (.then (fn [_] (println "[dev] data reloaded") true))))

;; ========== 状態確認 ==========
(defn pos [] (select-keys (:player @S/state) [:x :y]))
(defn keys-held [] (:keys @S/state))
(defn state-keys [] (keys @S/state))

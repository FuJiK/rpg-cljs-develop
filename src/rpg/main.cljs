(ns rpg.main
  (:require
   [rpg.engine.events  :as EV]
   [rpg.engine.input   :as I]
   [rpg.engine.loop    :as L]
   [rpg.engine.render  :as R]
   [rpg.engine.state   :as S]
   [rpg.engine.storage :as Store]
   [rpg.scenes.overworld :as OW]))

(defn init []
  (js/console.info "[rpg.main/init] Initializing RPG game...")
  (I/attach!)
  (Store/load-game!)
  (js/console.debug "[rpg.main/init] Input and storage systems initialized")

  ;; まず一度だけ強制描画（ここで緑フィールド＋黄プレイヤーが出るはず）
  (try
    (let [player (:player @S/state)]
      (when (and player (:x player) (:y player))
        (let [[fx fy] [(:x player) (:y player)]]
          (R/update-camera! fx fy)
          (R/draw!))
        (js/console.log "[init] initial render completed"))
      (when-not player
        (js/console.warn "[init] player state not initialized, skipping initial render")))
    (catch :default e
      (js/console.error "[init] first draw failed" e)))

  ;; データロードが完了してからループ開始＆初期イベント発火
  (-> (OW/seed!)
      (.then (fn [_]
               (js/console.log "[rpg.main/init] data loaded, start loop")
               (L/step!)
               (let [{:keys [x y]} (:player @S/state)]
                 (EV/trigger-at! x y))))
      (.catch (fn [err]
                (js/console.error "[rpg.main/init] data loading failed:" err)
                ;; フォールバック処理: デフォルトマップで開始
                (js/console.log "[rpg.main/init] starting with default state")
                (L/step!)))))
  ;; イベントリスナーの重複登録を防止
  (let [cleanup-fn (fn [_] (Store/save-game!))]
    ;; 既存のリスナーがあれば削除
    (.removeEventListener js/window "beforeunload" cleanup-fn)
    ;; 新しいリスナーを登録
    (.addEventListener js/window "beforeunload" cleanup-fn))

(defn reload [] (js/console.log "[rpg.main/reload] hot-reload"))

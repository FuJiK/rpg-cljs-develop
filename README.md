# RPG-CLJS（ClojureScript 2D RPG 最小土台＋テスト）

このリポジトリは、**ClojureScript だけで動く 2D RPG の最小土台**です。  
- Canvas 2Dで描画（プレイヤー移動／座標イベント／会話／宝箱／ワープ）
- データ駆動（EDNでタイル・イベント定義、Malliでスキーマ検証）
- セーブ／ロード（`localStorage`）
- ユニットテスト（Node）＋ブラウザ結合テスト（shadowのbrowser-test）

詳細なアーキテクチャ説明は [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) を参照してください。

## 動かし方

### 1) 前提
- Node.js（推奨 LTS）

### 2) 依存インストール
```bash
npm i
```

### 3) 開発サーバ（ウォッチ）
```bash
npm run watch
```
`index.html` をローカルHTTPで開いてください（例：VS Code Live Server、または）
```bash
npx http-server -p 8080
# http://localhost:8080 にアクセス
```

### 4) ビルド（本番）
```bash
npm run build
```

### 5) テスト
- 純ロジック（Node）
```bash
npm run test:node
```
- ブラウザ結合（起動してHTMLを開く）
```bash
npm run test:browser:watch
# out/browser-test/index.html をブラウザで開く
```

### 6) REPL駆動開発について
- REPLを起動する際には、以下の手順

```bash
npx shadow-cljs watch app

npx shadow-cljs watch #(これでもOK)
> watch
> shadow-cljs watch app
shadow-cljs - config: /Users/fujiokaken/github/rpg-cljs/shadow-cljs.edn
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.jboss.threads.JBossExecutors (file:/Users/fujiokaken/.m2/repository/org/jboss/threads/jboss-threads/3.5.0.Final/jboss-threads-3.5.0.Final.jar)
WARNING: Please consider reporting this to the maintainers of class org.jboss.threads.JBossExecutors
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
WARNING: A restricted method in java.lang.System has been called
WARNING: java.lang.System::load has been called by com.sun.jna.Native in an unnamed module (file:/Users/fujiokaken/.m2/repository/net/java/dev/jna/jna/5.12.1/jna-5.12.1.jar)
WARNING: Use --enable-native-access=ALL-UNNAMED to avoid a warning for callers in this module
WARNING: Restricted methods will be blocked in a future release unless native access is enabled

[2025-10-24 01:58:28.758 - WARNING] TCP Port 9630 in use.
shadow-cljs - HTTP server available at http://localhost:9631
shadow-cljs - server version: 2.28.23 running at http://localhost:9630
shadow-cljs - nREPL server started on port 9000
shadow-cljs - watching build :app
[:app] Configuring build.
[:app] Compiling ...
[:app] Build completed. (145 files, 0 compiled, 0 warnings, 5.39s)
```
- 続いて、ブラウザを起動

http://localhost:9631

をブラウザで開いておいてください（shadow-cljs.edn の :http-port に合わせる）。

→ ここで初めて「JS runtime（ブラウザ）」が REPL に接続されます。

- 最後に、REPLを起動

```bash
# (ブラウザを起動してない場合)
npx shadow-cljs cljs-repl app
shadow-cljs - config: /Users/fujiokaken/github/rpg-cljs/shadow-cljs.edn 
shadow-cljs - connected to server 
> (js/console.log "hello from REPL") 
No available JS runtime.

```

```bash
# (ブラウザを起動してる場合)
npx shadow-cljs cljs-repl app
shadow-cljs - config: /Users/fujiokaken/github/rpg-cljs/shadow-cljs.edn 
shadow-cljs - connected to server 
> (js/console.log "hello from REPL") 
nil 
cljs.user=>
```


## ディレクトリ構成

```
rpg-cljs/
├─ index.html                 # Canvasありランタイム
├─ package.json
├─ shadow-cljs.edn            # :app + :node-test + :browser-test
├─ data/                      # EDNコンテンツ
│  ├─ items.edn
│  ├─ enemies.edn
│  └─ maps/
│     └─ overworld.edn
├─ src/rpg/
│  ├─ main.cljs               # init/reload
│  ├─ data/
│  │  ├─ spec.cljs            # Malliスキーマ
│  │  └─ loader.cljs          # EDN fetch + validate + apply
│  ├─ engine/
│  │  ├─ state.cljs           # 単一の状態atom
│  │  ├─ input.cljs           # キー入力
│  │  ├─ map.cljs             # 移動・当たり判定
│  │  ├─ dialog.cljs          # ダイアログUI（簡易）
│  │  ├─ events.cljs          # ツクール風DSL（:say :chest :warp :give-item）
│  │  ├─ render.cljs          # Canvas描画
│  │  ├─ loop.cljs            # ゲームループ
│  │  └─ storage.cljs         # localStorage セーブ／ロード
│  └─ scenes/
│     └─ overworld.cljs       # データ一括ロード（items/enemies/overworld）
└─ test/rpg/
   ├─ engine/
   │  ├─ map_test.cljs
   │  └─ events_test.cljs
   └─ scenes/
      └─ loader_test.cljs
```

## 仕様のポイント

- **未定義タイルは自動で草(0)**：最初に全マップを敷く必要なし
- **イベントは座標にぶら下げる**：`events.cljs` の DSL で処理は集中管理
- **EDNデータは起動時にMalli検証**：データ不整合を早期検知
- **セーブは最小限**：`player/opened/inventory/map` を保存（拡張可）

## 今後の拡張アイデア

- 戦闘シーン：`[:battle :slime]` DSL と `engine/battle.cljs` / `scenes/battle.cljs`
- マップ切替・複数エリア：`maps/xxx.edn` を増やし `load-map!` で動的読み込み
- プロパティテスト（test.check）で移動・衝突の性質検査
- サーバ保存（Transit/JSON）／UGC配布／ランキング・実績

---

Happy Hacking! 🎮

# REPL 起動ガイド（shadow-cljs）

このプロジェクトのブラウザ REPL は **shadow-cljs の watch と、同じビルド向け HTTP ページを開いたブラウザ** がセットで必要です。`cljs-repl` だけでは動きません。

## 手順（ターミナル）

### 1. 依存

```bash
npm i
```

Java（JDK 11+）が入っていること。`java -version` で確認。

### 2. watch を走らせたままにする（ターミナル A）

```bash
npm run watch
```

成功時の例:

```
shadow-cljs - nREPL server started on port 9000
shadow-cljs - watching build :app
[:app] Build completed.
```

HTTP の URL はログに出る（通常 `http://localhost:9630`。ポート使用中なら `9631` など）。

### 3. ブラウザで shadow の URL を開く（必須）

**`file://` で index.html を開かない。**  
**`npx http-server` だけ**では REPL 用ランタイムが繋がりません。

ログに出たアドレスを開く（例: `http://localhost:9630` または `http://localhost:9631`）。

ゲーム画面が表示され、タブを開いたままにする。

### 4. CLJS REPL（ターミナル B）

```bash
npm run repl
# 同じ: npx shadow-cljs cljs-repl app
```

```clojure
(js/console.log "hello")
;; => nil  （ブラウザのコンソールに出れば OK）
```

## よくあるエラー

### `No available JS runtime.`

| 原因 | 対処 |
|------|------|
| watch が動いていない | ターミナル A で `npm run watch` |
| ブラウザで shadow の URL を開いていない | 手順 3 |
| 別ポートのページを見ている | watch ログの `HTTP server available at ...` を開き直す |
| watch を再起動したあとタブが古い | ブラウザをリロード（F5） |

### `TCP Port 9630 in use` / `9000 in use`

別の shadow や http-server がポートを使用している。

```bash
# 例: 既存の shadow を止める（ポートは環境により）
npx shadow-cljs stop
```

表示された **別ポート**（例: 9631）の URL をブラウザで開く。

### `cljs-repl` に繋がらない / watch がすぐ終了する

- `npm i` 済みか
- Java があるか
- プロジェクトルート（`shadow-cljs.edn` があるディレクトリ）で実行しているか

## エディタから（Calva / Cursive）

1. ターミナルで `npm run watch` を常時実行
2. ブラウザで shadow の HTTP URL を開く
3. nREPL **ポート 9000**（`shadow-cljs.edn` の `:nrepl {:port 9000}`）に接続
4. ビルド ID は **`app`** を選ぶ

## Node だけ試したい場合（ブラウザ不要）

エンジンの Node テスト用ビルド向け:

```bash
npx shadow-cljs node-repl node-test
```

`rpg.main` の Canvas ゲーム本体は **`:app`（ブラウザ）** 向けです。
# E2E テスト（Playwright）

本番ビルドを HTTP で配信し、ブラウザから実際の操作経路を検証します。

## テストの層

| 層 | コマンド | 役割 |
|----|----------|------|
| Node | `npm run test:ci` | エンジン・EDN・DSL（高速） |
| E2E | `npm run test:e2e` | 起動・移動・会話・リセット・マップ遷移 |
| REPL | `npm run repl` | 開発時の手動確認（[REPL.md](REPL.md)） |

## シナリオ一覧

| ファイル | 内容 |
|----------|------|
| `e2e/smoke.spec.ts` | 灯ノ里（`akari-village`）読み込み、初期座標、`__rpg` フック |
| `e2e/movement.spec.ts` | 矢印で X 座標が変わる |
| `e2e/dialog.spec.ts` | 看板 (6,9) で会話、Space で閉じる |
| `e2e/reset.spec.ts` | データリセット後、進行クリア＆スポーン |
| `e2e/windpass.spec.ts` | 北ゲートから風渡りの街道（`windpass`）へ |

バトル・追跡は歩数が多いため Node テスト（`chase_test`, `battle_test`）に任せています。

## ローカル実行

```bash
npm i
npm run build
npx playwright install chromium
npm run test:e2e
```

`playwright.config.ts` が `serve` でルートをポート 4173 配信します（`file://` は不可）。

## テスト用フック

Playwright は `page.addInitScript` で `window.__RPG_E2E__ = true` を立てます。  
`rpg.e2e` が `window.__rpg.getState()` を公開し、Canvas のピクセルではなく状態を assert します。

本番プレイでは `__RPG_E2E__` を立てなければフックは有効化されません。E2E 時は入場の「場所」バナーを出さず、フレーキーな会話中断を避けます。

## CI

GitHub Actions の `e2e` job（`.github/workflows/ci.yml`）で `build` 後に実行します。
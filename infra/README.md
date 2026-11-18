# AWS 配信

ゲームはリポジトリのルートでビルドし、CDK は `infra/` で独立して管理します。AWS の配信先は東京リージョンの非公開 S3 と CloudFront です。現行ゲームの静的 EDN と localStorage を使用します。

| ワークフロー | 実行条件と役割 |
|---|---|
| Game CI | ゲーム関連の PR で Node テスト、本番ビルド、Chromium 起動確認 |
| CDK CI | インフラ関連の PR と main への push で型チェック、構成テスト、synth |
| Deploy game to AWS | main のゲーム変更または手動実行で Game CI を呼び出し、検証済みの成果物を S3 にアップロード |

npm キャッシュはルートと `infra/` それぞれのロックファイルをキーに使います。ゲームの成果物は同じワークフロー実行内でアーティファクトとして渡します。

## 初回構築

Node.js 22 と、対象の AWS アカウントにログイン済みの AWS CLI を使います。対象アカウントを `aws sts get-caller-identity` で確認してから、リポジトリのルートで以下を実行します。

```sh
cd infra
npm ci
npm test
npm run synth
npx cdk bootstrap aws://<アカウントID>/ap-northeast-1
npx cdk diff RpgHosting
npx cdk deploy RpgHosting
```

アカウントに GitHub Actions 用の OIDC プロバイダーがある場合は、`synth`、`diff`、`deploy` に `-c githubOidcProviderArn=<既存のARN>` を付けて再利用します。CDK の適用はインフラ管理者の権限で行います。

CDK の出力を GitHub の Actions リポジトリ変数に登録します。

| Actions 変数 | CDK 出力 |
|---|---|
| `AWS_GAME_BUCKET` | `GameBucket` |
| `AWS_GAME_URL` | `GameUrl` |
| `AWS_GAME_DEPLOY_ROLE_ARN` | `GameDeployRoleArn` |

3つ揃うとデプロイジョブが有効になります。初回は main の **Deploy game to AWS** を手動実行できます。配信用ロールはこのリポジトリの main から引き受けられ、サイトの対象キーへアップロードできます。

## 配信と復旧

CloudFront のキャッシュは初版では無効で、同一URLに新しいファイルを配信した際の無効化操作を省いています。実運用でキャッシュを有効にする場合は、リリースごとのURLと切り替え方法を設計します。現行のアップロードはファイルごとに反映されます。互換性のある更新を前提とし、復旧時は S3 のバージョニングで同じリリースの各ファイルを復元できます。

GitHub Pages の既存公開は別のワークフローで管理されます。

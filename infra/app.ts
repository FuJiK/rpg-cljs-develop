import { App } from 'aws-cdk-lib';
import { HostingStack } from './lib/hosting-stack';

const app = new App();
new HostingStack(app, 'RpgHosting', {
  env: { account: process.env.CDK_DEFAULT_ACCOUNT, region: 'ap-northeast-1' },
  githubOidcProviderArn: app.node.tryGetContext('githubOidcProviderArn'),
});

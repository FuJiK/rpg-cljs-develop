import { test } from 'node:test';
import { App } from 'aws-cdk-lib';
import { Match, Template } from 'aws-cdk-lib/assertions';
import { HostingStack } from '../lib/hosting-stack';

test('serves the private bucket through CloudFront and trusts only main for publishing', () => {
  const template = Template.fromStack(new HostingStack(new App(), 'Test'));
  template.hasResourceProperties('AWS::S3::Bucket', {
    PublicAccessBlockConfiguration: {
      BlockPublicAcls: true,
      BlockPublicPolicy: true,
      IgnorePublicAcls: true,
      RestrictPublicBuckets: true,
    },
    VersioningConfiguration: { Status: 'Enabled' },
  });
  template.hasResourceProperties('AWS::CloudFront::OriginAccessControl', {
    OriginAccessControlConfig: {
      OriginAccessControlOriginType: 's3',
      SigningBehavior: 'always',
      SigningProtocol: 'sigv4',
    },
  });
  template.hasResourceProperties('AWS::CloudFront::Distribution', {
    DistributionConfig: {
      DefaultRootObject: 'index.html',
      DefaultCacheBehavior: { ViewerProtocolPolicy: 'redirect-to-https' },
    },
  });
  template.hasResourceProperties('AWS::IAM::Role', {
    AssumeRolePolicyDocument: {
      Statement: Match.arrayWith([Match.objectLike({
        Action: 'sts:AssumeRoleWithWebIdentity',
        Condition: { StringEquals: {
          'token.actions.githubusercontent.com:aud': 'sts.amazonaws.com',
          'token.actions.githubusercontent.com:sub':
            'repo:FuJiK/rpg-cljs-develop:ref:refs/heads/main',
        } },
      })]),
    },
  });
});

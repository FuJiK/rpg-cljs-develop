import { CfnOutput, RemovalPolicy, Stack, StackProps } from 'aws-cdk-lib';
import * as cloudfront from 'aws-cdk-lib/aws-cloudfront';
import { S3BucketOrigin } from 'aws-cdk-lib/aws-cloudfront-origins';
import * as iam from 'aws-cdk-lib/aws-iam';
import * as s3 from 'aws-cdk-lib/aws-s3';
import { Construct } from 'constructs';

interface HostingStackProps extends StackProps {
  githubOidcProviderArn?: string;
}

export class HostingStack extends Stack {
  constructor(scope: Construct, id: string, props: HostingStackProps = {}) {
    super(scope, id, props);

    const site = new s3.Bucket(this, 'Site', {
      blockPublicAccess: s3.BlockPublicAccess.BLOCK_ALL,
      encryption: s3.BucketEncryption.S3_MANAGED,
      enforceSSL: true,
      removalPolicy: RemovalPolicy.RETAIN,
      versioned: true,
    });

    const distribution = new cloudfront.Distribution(this, 'Distribution', {
      defaultRootObject: 'index.html',
      defaultBehavior: {
        origin: S3BucketOrigin.withOriginAccessControl(site),
        viewerProtocolPolicy: cloudfront.ViewerProtocolPolicy.REDIRECT_TO_HTTPS,
        cachePolicy: cloudfront.CachePolicy.CACHING_DISABLED,
      },
    });

    const issuer = 'token.actions.githubusercontent.com';
    const oidcArn = props.githubOidcProviderArn ??
      new iam.OidcProviderNative(this, 'GitHubOidc', {
        url: 'https://' + issuer,
        clientIds: ['sts.amazonaws.com'],
      }).oidcProviderArn;
    const publisher = new iam.Role(this, 'Publisher', {
      assumedBy: new iam.WebIdentityPrincipal(oidcArn, {
        StringEquals: {
          [issuer + ':aud']: 'sts.amazonaws.com',
          [issuer + ':sub']: 'repo:FuJiK/rpg-cljs-develop:ref:refs/heads/main',
        },
      }),
    });

    publisher.addToPolicy(new iam.PolicyStatement({
      actions: ['s3:PutObject', 's3:AbortMultipartUpload'],
      resources: [
        site.arnForObjects('index.html'),
        site.arnForObjects('js/*'),
        site.arnForObjects('data/*'),
      ],
    }));

    new CfnOutput(this, 'GameBucket', { value: site.bucketName });
    new CfnOutput(this, 'GameUrl', { value: 'https://' + distribution.distributionDomainName });
    new CfnOutput(this, 'GameDeployRoleArn', { value: publisher.roleArn });
  }
}

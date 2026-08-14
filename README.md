# my-aws-project

Multi-module Gradle/Spring Boot project that reports the AWS region and
availability zone it's running in, via the EC2 Instance Metadata Service
(IMDSv2).

## Modules

- **`shared-lib`** — Plain Java library (no Spring). Holds
  `com.example.shared.AppInfo`, a small placeholder utility so `web-app` has
  something to depend on and future modules have a shared home for common
  code.
- **`web-app`** — Spring Boot 3.3 web application (Java 17). Exposes a
  `GET /region` endpoint that queries the EC2 metadata service for the
  current availability zone and derives the region from it. Depends on
  `shared-lib`.

## Build locally

```bash
./gradlew build
```

## Run locally

```bash
./gradlew :web-app:bootRun
```

Then, in another terminal:

```bash
curl http://localhost:8080/region
```

> **Note:** `/region` only returns real AWS data when the app is actually
> running on an EC2 instance, since it talks to the instance metadata
> endpoint at `169.254.169.254`. Running it locally (or anywhere off EC2)
> will hit a connection timeout, which the controller catches and reports as
> a friendly fallback message instead of failing the request.

## CI/CD

`.github/workflows/build.yml` builds `web-app`'s executable jar on every push
to `main` and uploads it to S3. Before using it, edit the placeholders:

- `env.AWS_REGION` — target AWS region (defaults to `us-east-1`)
- `env.ARTIFACT_BUCKET` — set to your actual S3 bucket name

It also expects `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY` to be
configured as GitHub Actions secrets on the repository.

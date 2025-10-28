# Maintain a trust frontend

This service is responsible for navigating the user to maintain various aspects of their trust registration. It acts as the 'hub' which allows the user to navigate to other areas of the service, such as: trustees, beneficiaries etc.

To run locally using the micro-service provided by the service manager:

```bash
sm2 --start MAINTAIN_TRUST_ALL
```

Or

```bash
sm2 --start REGISTER_TRUST_ALL
```

If you want to run your local copy, then stop the frontend ran by the service manager and run your local code by using the following (port number is 9788 but is defaulted to that in build.sbt).

`sbt run`

Use the following command to run your local copy with the test-only routes:

```bash
sbt run -Dplay.http.router=testOnlyDoNotUseInAppConf.Routes
```

## Testing the service

This service uses [sbt-scoverage](https://github.com/scoverage/sbt-scoverage) to
provide test coverage reports.

Use the following commands to run the tests with coverage and generate a report.

Run this script before raising a PR to ensure your code changes pass the Jenkins pipeline. This runs all the unit tests and checks for dependency updates:

```bash
./run_all_tests.sh
```

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").

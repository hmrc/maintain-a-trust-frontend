# Maintain a trust frontend

This service is responsible for navigating the user to maintain various aspects of their trust registration. It acts as the 'hub' which allows the user to navigate to other areas of the service, such as: trustees, beneficiaries etc.

To run locally using the micro-service provided by the service manager:

***sm2 --start TRUSTS_ALL -r***

If you want to run your local copy, then stop the frontend ran by the service manager and run your local code by using the following (port number is 9788 but is defaulted to that in build.sbt).

`sbt run`

## Testing the service

This service uses [sbt-scoverage](https://github.com/scoverage/sbt-scoverage) to
provide test coverage reports.

Use the following commands to run the tests with coverage and generate a report.

Run unit and integration tests:
```
sbt clean coverage test it:test coverageReport
```

Unit tests only:
```
sbt clean coverage test coverageReport
```

Integration tests only:
```
sbt clean coverage it:test coverageReport
```

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").

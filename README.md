# Scripts for Jenkins CI

This repository is some sort of "desperate measure" as we use [Jenkins CI](https://jenkins.io) quite a lot but sometimes plugins don't provide the functionality needed. Most of what we need in addition is written in [Groovy](http://groovy-lang.org/) to talk to other services, some of those are open sourced here. It may not always be truly elegant, but does the job while running stable in daily operations.

## SonarQube Quality-Gate Check

**Source:** [here](sonar-quality-gates/quality-gates.groovy)

We're using a single, generic [SonarQube](https://www.sonarqube.org/) Job for most projects providing some good insight into Code Quality in terms of Static Analysis.

Neither of the SonarQube Quality Gates Plugins for Jenkins provided provides a Build Step which can handle dynamic project keys (e.g. by using Jenkins Variables), so we wrote this little Groovy script to fail the build in case SonarQube reports a failed Quality-Gate.

This script is depending/tested (and running in production here) on:

* Jenkins CI >= 2.92
* Plugin: [Groovy](https://plugins.jenkins.io/groovy)
* Plugin: [Environment Injector](https://plugins.jenkins.io/envinject)

### How To

Your Build environemnt should be prepared using the Checkbox "Prepare SonarQube Scanner environment" beforehand.

Add a SonarQube Standalone Scanner to your Build-Jobs. SonarQube stores job data in `${WORKSPACE}/.scannerwork/report-task.txt`, which is in standard properties format. So it can be injected into the Environment using the "Environment Injector" Plugin.

After injecting the Variables add a "Execute System Groovy Script" using the Script-Content from mentioned source above.

This Script will poll the SonarQube analysis status and fail the build in case the Quality Gate returns either WARNING of FAILED using the parameters provided by SonarQube during analysis.


## License

This Vagrant box is licensed under the permissive [MIT license](http://opensource.org/licenses/MIT) - have fun with it!

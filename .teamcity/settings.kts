import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.PullRequests
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.perfmon
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.pullRequests
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.ScriptBuildStep
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2019_2.projectFeatures.dockerRegistry
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2020.1"

project {

    vcsRoot(WpCalypso)

    buildType(RunAllUnitTests)
    buildType(prepare_calypso_live)
    buildType(run_woo_e2e_tests)
    buildType(run_ie11_e2e_tests)
    buildType(run_safari_tests)

    template(run_e2e)

    params {
        param("env.NODE_OPTIONS", "--max-old-space-size=32000")
        text("env.E2E_WORKERS", "7", label = "Magellan parallel workers", description = "Number of parallel workers in Magellan (e2e tests)", allowEmpty = true)
        text("env.JEST_MAX_WORKERS", "16", label = "Jest max workers", description = "How many tests run in parallel", allowEmpty = true)
        password("env.CONFIG_KEY", "credentialsJSON:16d15e36-f0f2-4182-8477-8d8072d0b5ec", label = "Config key", description = "Key used to decrypt config")
        text("env.CHILD_CONCURRENCY", "15", label = "Yarn child concurrency", description = "How many packages yarn builds in parallel", allowEmpty = true)
        text("docker_image", "automattic/wp-calypso-ci:1.0.9", label = "Docker image", description = "Docker image to use for the run", allowEmpty = true)
        param("teamcity.git.fetchAllHeads", "true")
    }

    features {
        dockerRegistry {
            id = "PROJECT_EXT_6"
            name = "Docker Registry"
            url = "https://registry.a8c.com"
        }
    }
}

object RunAllUnitTests: BuildType({
    id = "RunAllUnitTests"
    name = "Run all unit tests"
    description = "Runs code hygiene and unit tests"

    artifactRules = """
        test_results => test_results
        artifacts => artifacts
    """.trimIndent()

    vcs {
        root(WpCalypso)

        cleanCheckout = true
    }

    steps {
        script {
            name = "Prepare environment"
            scriptContent = """
                set -e
                export HOME="/calypso"
                export NODE_ENV="test"
                export CHROMEDRIVER_SKIP_DOWNLOAD=true
                export PUPPETEER_SKIP_DOWNLOAD=true
                export npm_config_cache=${'$'}(yarn cache dir)

                # Update node
                . "${'$'}NVM_DIR/nvm.sh" --install
                nvm use

                # Install modules
                yarn install
            """.trimIndent()
            dockerImagePlatform = ScriptBuildStep.ImagePlatform.Linux
            dockerImage = "%docker_image%"
            dockerRunParameters = "-u %env.UID%"
        }
        script {
            name = "Code hygiene"
            scriptContent = """
                set -e
                set -x
                export HOME="/calypso"
                export NODE_ENV="test"

                # Update node
                . "${'$'}NVM_DIR/nvm.sh"

                # Prevent uncommited changes
                DIRTY_FILES=${'$'}(git status --porcelain 2>/dev/null)
                if [ ! -z "${'$'}DIRTY_FILES" ]; then
                	echo "Repository contains uncommitted changes: "
                	echo "${'$'}DIRTY_FILES"
                	echo "You need to checkout the branch, run 'yarn' and commit those files."
                	exit 1
                fi

                # Code style
                FILES_TO_LINT=${'$'}(git diff --name-only --diff-filter=d refs/remotes/origin/master...HEAD | grep -E '^(client/|server/|packages/)' | grep -E '\.[jt]sx?${'$'}' || exit 0)
                echo ${'$'}FILES_TO_LINT
                if [ ! -z "${'$'}FILES_TO_LINT" ]; then
                	yarn run eslint --format junit --output-file "./test_results/eslint/results.xml" ${'$'}FILES_TO_LINT
                fi

                # Run type checks
                yarn run tsc --project client/landing/gutenboarding
            """.trimIndent()
            dockerImagePlatform = ScriptBuildStep.ImagePlatform.Linux
            dockerImage = "%docker_image%"
            dockerRunParameters = "-u %env.UID%"
        }
        script {
            name = "Run unit tests"
            scriptContent = """
                set -e
                export JEST_JUNIT_OUTPUT_NAME="results.xml"
                export HOME="/calypso"
                export NODE_ENV="test"

                # Update node
                . "${'$'}NVM_DIR/nvm.sh"

                # Run client tests
                JEST_JUNIT_OUTPUT_DIR="./test_results/client" yarn test-client --maxWorkers=${'$'}JEST_MAX_WORKERS --ci --reporters=default --reporters=jest-junit --silent

                # Run packages tests
                JEST_JUNIT_OUTPUT_DIR="./test_results/packages" yarn test-packages --maxWorkers=${'$'}JEST_MAX_WORKERS --ci --reporters=default --reporters=jest-junit --silent

                # Run server tests
                JEST_JUNIT_OUTPUT_DIR="./test_results/server" yarn test-server --maxWorkers=${'$'}JEST_MAX_WORKERS --ci --reporters=default --reporters=jest-junit --silent

                # Run Editing Toolkit tests
                cd apps/editing-toolkit
                JEST_JUNIT_OUTPUT_DIR="../../test_results/editing-toolkit" yarn test:js --reporters=default --reporters=jest-junit  --maxWorkers=${'$'}JEST_MAX_WORKERS
            """.trimIndent()
            dockerImagePlatform = ScriptBuildStep.ImagePlatform.Linux
            dockerImage = "%docker_image%"
            dockerRunParameters = "-u %env.UID%"
        }
        script {
            name = "Build artifacts"
            scriptContent = """
                set -e
                export HOME="/calypso"
                export NODE_ENV="test"

                # Update node
                . "${'$'}NVM_DIR/nvm.sh"

                # Build o2-blocks
                (cd apps/o2-blocks/ && yarn build --output-path="../../artifacts/o2-blocks")

                # Build wpcom-block-editor
                (cd apps/wpcom-block-editor/ && yarn build --output-path="../../artifacts/wpcom-block-editor")

                # Build notifications
                (cd apps/notifications/ && yarn build --output-path="../../artifacts/notifications")
            """.trimIndent()
            dockerImagePlatform = ScriptBuildStep.ImagePlatform.Linux
            dockerImage = "%docker_image%"
            dockerRunParameters = "-u %env.UID%"
        }
    }

    triggers {
        vcs {
        }
    }

    features {
        feature {
            type = "xml-report-plugin"
            param("xmlReportParsing.reportType", "junit")
            param("xmlReportParsing.reportDirs", "test_results/**/*.xml")
        }
        perfmon {
        }
        pullRequests {
            vcsRootExtId = "${WpCalypso.id}"
            provider = github {
                authType = token {
                    token = "credentialsJSON:c834538f-90ff-45f5-bbc4-64406f06a28d"
                }
                filterAuthorRole = PullRequests.GitHubRoleFilter.MEMBER
            }
        }
    }
})

object prepare_calypso_live : BuildType({
    name = "Prepare calypso live"

    vcs {
        root(WpCalypso)
    }

    steps {
        script {
            name = "Prime calypso live"
            scriptContent = """
                set -x
                set -e
                export sha=${'$'}(git rev-parse HEAD)
                curl "https://calypso.live/?hash=${'$'}{sha}"
                ./test/e2e/scripts/wait-for-running-branch.sh
            """.trimIndent()
            dockerImagePlatform = ScriptBuildStep.ImagePlatform.Linux
            dockerImage = "%docker_image%"
            dockerRunParameters = "-u %env.UID%"
        }
    }
})

object run_ie11_e2e_tests : BuildType({
    templates(run_e2e)
    name = "Run e2e tests - Canary IE11"

    artifactRules = "artifacts"

    steps {
        script {
            name = "Run tests"
            id = "RUNNER_363"
            scriptContent = """
                set -e

                export NODE_CONFIG_ENV=test
                export BRANCHNAME=master
                export HIGHLIGHT_ELEMENT=true
                export sha=${'$'}(git rev-parse HEAD)
                export HOME="/calypso"
                export npm_config_cache=${'$'}(yarn cache dir)
                export TARGET="IE11"
                export JETPACKHOST=''
                export ARTIFACTS="${'$'}{PWD}/artifacts"

                # Update node
                . "${'$'}NVM_DIR/nvm.sh" --install
                nvm use

                # Install modules
                yarn install

                # Run tests
                cd  ./test/e2e
                yarn run decryptconfig
                ./run.sh -a ${'$'}E2E_WORKERS -R -z -S ${'$'}sha

                # Save artifacts
                mkdir -p "${'$'}ARTIFACTS"
                cp -r ./reports "${'$'}{ARTIFACTS}/e2ereports"
                cp -r ./screenshots "${'$'}{ARTIFACTS}/screenshots"
            """.trimIndent()
            dockerImagePlatform = ScriptBuildStep.ImagePlatform.Linux
            dockerImage = "%docker_image%"
            dockerRunParameters = "-u %env.UID%"
        }
    }

    features {
        feature {
            id = "BUILD_EXT_362"
            type = "xml-report-plugin"
            param("xmlReportParsing.reportType", "junit")
            param("xmlReportParsing.reportDirs", "artifacts/**/*.xml")
            param("xmlReportParsing.verboseOutput", "true")
        }
    }

    dependencies {
        snapshot(prepare_calypso_live) {
        }
    }
})

object run_safari_tests : BuildType({
    templates(run_e2e)
    name = "Run e2e tests - Canary Safari"

    artifactRules = "artifacts"

    steps {
        script {
            name = "Run tests"
            id = "RUNNER_363"
            scriptContent = """
                set -e

                export NODE_CONFIG_ENV=test
                export BRANCHNAME=master
                export HIGHLIGHT_ELEMENT=true
                export sha=${'$'}(git rev-parse HEAD)
                export HOME="/calypso"
                export npm_config_cache=${'$'}(yarn cache dir)
                export TARGET=''
                export JETPACKHOST=''
                export ARTIFACTS="${'$'}{PWD}/artifacts"

                # Update node
                . "${'$'}NVM_DIR/nvm.sh" --install
                nvm use

                # Install modules
                yarn install

                # Run tests
                cd  ./test/e2e
                yarn run decryptconfig
                ./run.sh -a ${'$'}E2E_WORKERS -R -y -S ${'$'}sha

                # Save artifacts
                mkdir -p "${'$'}ARTIFACTS"
                cp -r ./reports "${'$'}{ARTIFACTS}/e2ereports"
                cp -r ./screenshots "${'$'}{ARTIFACTS}/screenshots"
            """.trimIndent()
            dockerImagePlatform = ScriptBuildStep.ImagePlatform.Linux
            dockerImage = "%docker_image%"
            dockerRunParameters = "-u %env.UID%"
        }
    }

    features {
        feature {
            id = "BUILD_EXT_362"
            type = "xml-report-plugin"
            param("xmlReportParsing.reportType", "junit")
            param("xmlReportParsing.reportDirs", "artifacts/**/*.xml")
            param("xmlReportParsing.verboseOutput", "true")
        }
    }

    dependencies {
        snapshot(prepare_calypso_live) {
        }
    }
})

object run_woo_e2e_tests : BuildType({
    templates(run_e2e)
    name = "Run e2e tests - Canary Woo"

    artifactRules = "artifacts"

    steps {
        script {
            name = "Run tests"
            id = "RUNNER_363"
            scriptContent = """
                set -e

                export NODE_CONFIG_ENV=test
                export BRANCHNAME=master
                export HIGHLIGHT_ELEMENT=true
                export sha=${'$'}(git rev-parse HEAD)
                export npm_config_cache=${'$'}(yarn cache dir)
                export ARTIFACTS="${'$'}{PWD}/artifacts"
                export TARGET="WOO"
                export JETPACKHOST=''
                export TEST_VIDEO=true

                # Update node
                . "${'$'}NVM_DIR/nvm.sh" --install
                nvm use

                # Install modules
                yarn install

                # Run tests
                cd  ./test/e2e
                yarn run decryptconfig
                ./run.sh -a ${'$'}E2E_WORKERS -R -W -S ${'$'}sha

                # Save artifacts
                mkdir -p "${'$'}ARTIFACTS"
                cp -r ./reports "${'$'}{ARTIFACTS}/e2ereports"
                cp -r ./screenshots "${'$'}{ARTIFACTS}/screenshots"
            """.trimIndent()
            dockerImagePlatform = ScriptBuildStep.ImagePlatform.Linux
            dockerImage = "%docker_image%"
            dockerRunParameters = "-u %env.UID%"
        }
    }

    features {
        feature {
            id = "BUILD_EXT_362"
            type = "xml-report-plugin"
            param("xmlReportParsing.reportType", "junit")
            param("xmlReportParsing.reportDirs", "artifacts/**/*.xml")
            param("xmlReportParsing.verboseOutput", "true")
        }
    }

    dependencies {
        snapshot(prepare_calypso_live) {
        }
    }
})

object run_e2e : Template({
    name = "Run e2e tests"

    vcs {
        root(WpCalypso)

        cleanCheckout = true
    }

    features {
        feature {
            id = "BUILD_EXT_362"
            type = "xml-report-plugin"
            param("xmlReportParsing.reportType", "junit")
            param("xmlReportParsing.reportDirs", "test_results/**/*.xml")
        }
        perfmon {
            id = "perfmon"
        }
    }
})

object WpCalypso : GitVcsRoot({
    name = "wp-calypso"
    url = "git@github.com:Automattic/wp-calypso.git"
    pushUrl = "git@github.com:Automattic/wp-calypso.git"
    branch = "refs/heads/try/teamcity"
    authMethod = uploadedKey {
        uploadedKey = "Sergio TeamCity"
    }
})

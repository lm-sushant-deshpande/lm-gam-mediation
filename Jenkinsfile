pipeline {
    agent any

    parameters {
        choice(name: 'LIBRARY', choices: ['LMControlCoreSDK', 'GamMediationAdapter', 'LemmaVideoSDK'], description: 'Select library to publish')
        string(name: 'GIT_BRANCH', defaultValue: '', description: 'Git branch to checkout')
        string(name: 'VERSION', defaultValue: '', description: 'Version of the release')
        choice(name: 'ENVIRONMENT', choices: ['QA', 'PROD'], description: 'Environment to deploy (QA or PROD)')
    }

    environment {
        DOCKER_IMAGE = 'lm-gam-mediation:latest'
        AAR_FILE_NAME = "${env.LIBRARY_NAME}-${params.VERSION}.aar"
        AAR_FILE_PATH = "${WORKSPACE}/${env.AAR_FILE_NAME}"
    }

    stages {

        stage('Clean Workspace') {
            steps {
                echo 'Cleaning Existing Directory...'
                cleanWs()
                sh '''
                    echo "Current Directory: $(pwd)"
                    ls -l
                '''
            }
        }

        stage('Parameter Validation') {
            steps {
                script {
                    if (!params.LIBRARY?.trim()) {
                        error "Parameter LIBRARY is required"
                    }
                    if (!params.GIT_BRANCH?.trim()) {
                        error "Parameter GIT_BRANCH is required."
                    }
                    if (!params.VERSION?.trim()) {
                        error "Parameter VERSION is required."
                    }
                    if (!params.ENVIRONMENT?.trim()) {
                        error "Parameter ENVIRONMENT is required."
                    }

                    def repoMap = [
                        'LMControlCoreSDK': 'git@github.com:lemmamedia/lm-controlcore-android-sdk.git',
                        'GamMediationAdapter': 'git@github.com:lm-sushant-deshpande/lm-gam-mediation.git',
                        'LemmaVideoSDK': 'git@github.com:lm-sushant-deshpande/lm-gam-mediation.git'
                    ]
                    def libraryMap = [
                        'LMControlCoreSDK': 'LMControlCoreSDK',
                        'GamMediationAdapter': 'gam_mediation_adapter',
                        'LemmaVideoSDK': 'lemmavideosdk'
                    ]
                    def repoDirectory = [
                        'LMControlCoreSDK': 'LMControlCoreSDKApp',
                        'GamMediationAdapter': 'GAM_SampleApp',
                        'LemmaVideoSDK': 'GAM_SampleApp'
                    ]

                    env.GIT_HUB_REPO = repoMap[params.LIBRARY] ?: error("Invalid library selected")
                    env.LIBRARY_NAME = libraryMap[params.LIBRARY] ?: error("Invalid library selected")
                    env.REPO_DIR = repoDirectory[params.LIBRARY] ?: error("Invalid library selected")
                }
            }
        }

        stage('Checkout') {
            steps {
                script {
                    checkout([$class: 'GitSCM',
                        branches: [[name: "${params.GIT_BRANCH}"]],
                        userRemoteConfigs: [[
                            url: "${env.GIT_HUB_REPO}",
                            credentialsId: 'p-git-access'
                        ]]
                    ])
                }
            }
        }

        stage('Build & Publish AAR') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'nexus-uploader-usr', usernameVariable: 'NEXUS_USERNAME', passwordVariable: 'NEXUS_PASSWORD')]) {

                        dir("${env.REPO_DIR}") {
                            docker.image("${env.DOCKER_IMAGE}").inside {
                                echo "Building AAR with Docker..."

                                sh '''
                                    set -e
                                    echo "Running in Docker container..."
                                    echo "Current Directory: $(pwd)"
                                    ls -l

                                    echo "Library Name: ${LIBRARY_NAME}"
                                    echo "Gradle Wrapper (gradlew) exists, proceeding with build..."

                                    ./gradlew clean :${LIBRARY_NAME}:assembleRelease \
                                        -Pversion=${VERSION}
                                '''

                                echo "Publishing AAR to Nexus..."
                                def aarMavenUrl = params.ENVIRONMENT == 'PROD'
                                ? 'https://nexus.lemmatechnologies.com/repository/maven-releases'
                                : 'https://nexus.lemmatechnologies.com/repository/maven-releases'

                                echo "Using Version: ${VERSION}"

                                withEnv(["AAR_MAVEN_URL=${aarMavenUrl}"]) {
                                    sh '''
                                        set -e
                                        echo "Executing Gradle publish command with URL: ${AAR_MAVEN_URL}"
                                        ./gradlew :${LIBRARY_NAME}:publish \
                                            -PnexusUsername=${NEXUS_USERNAME} \
                                            -PnexusPassword=${NEXUS_PASSWORD} \
                                            -Pversion=${VERSION} \
                                            -PmavenUrl=${AAR_MAVEN_URL}
                                    '''
                                }
                            }
                        }
                    }
                }
            }
        }

    }
}

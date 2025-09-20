pipeline {
    agent { label 'docker' }

    environment {
        REGISTRY_IMAGE       = 'registry.example.com/smartcane-api'
        DOCKER_CREDENTIALS_ID = 'docker-registry-credentials'
        DOCKERFILE_PATH      = 'Dockerfile'
        DEPLOY_JOB           = ''
    }

    options {
        timestamps()
        skipDefaultCheckout(true)
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    env.GIT_SHORT_COMMIT = sh(returnStdout: true, script: 'git rev-parse --short HEAD').trim()
                }
            }
        }

        stage('Build & Test') {
            agent {
                docker {
                    image 'eclipse-temurin:21-jdk'
                    args '-v $WORKSPACE/.gradle:/home/gradle/.gradle'
                }
            }
            steps {
                sh './gradlew clean test'
                sh './gradlew bootJar'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'build/test-results/**/*.xml'
                    archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true, allowEmptyArchive: true
                }
            }
        }

        stage('Docker Build') {
            steps {
                script {
                    env.IMAGE_TAG = env.GIT_SHORT_COMMIT ?: env.BUILD_NUMBER
                }
                sh "docker build -f ${env.DOCKERFILE_PATH} -t ${env.REGISTRY_IMAGE}:${env.IMAGE_TAG} ."
            }
        }

        stage('Docker Push') {
            when {
                expression { return env.DOCKER_CREDENTIALS_ID?.trim() }
            }
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: env.DOCKER_CREDENTIALS_ID,
                        usernameVariable: 'REGISTRY_USERNAME',
                        passwordVariable: 'REGISTRY_PASSWORD'
                    )
                ]) {
                    script {
                        def registryHost = env.REGISTRY_IMAGE.split('/')[0]
                        sh "echo ${REGISTRY_PASSWORD} | docker login ${registryHost} --username ${REGISTRY_USERNAME} --password-stdin"
                        sh "docker push ${env.REGISTRY_IMAGE}:${env.IMAGE_TAG}"
                        sh 'docker logout || true'
                    }
                }
            }
        }

        stage('Trigger Deployment') {
            when {
                expression { return env.DEPLOY_JOB?.trim() }
            }
            steps {
                build job: env.DEPLOY_JOB, parameters: [string(name: 'IMAGE_TAG', value: env.IMAGE_TAG)]
            }
        }
    }
}

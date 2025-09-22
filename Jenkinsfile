pipeline {
  agent any
  environment {
    REGISTRY   = "ghcr.io"
    OWNER      = "musclebeaver"
    APP        = "smartcane-api"
    IMAGE_BASE = "${REGISTRY}/${OWNER}/${APP}"
    GHCR_PAT   = credentials('smartcane-ghcr')
  }
  options { timestamps(); disableConcurrentBuilds() }

  stages {
    stage('Checkout') {
      steps { checkout scm }
    }

    stage('Build JAR') {
      steps {
        sh '''
          chmod +x ./gradlew || true
          ./gradlew clean build -x test
        '''
      }
    }

    stage('Build & Push Image') {
      steps {
        script {
          def branch  = env.BRANCH_NAME ?: 'local'
          def channel = (branch == 'main') ? 'prod' : (branch == 'dev' ? 'dev' : branch.replaceAll('[^a-zA-Z0-9_.-]','-'))

          // 루트 Gradle 산출물 경로
          def jar = sh(script: "ls build/libs/*.jar | head -n 1", returnStdout: true).trim()

          sh """
            cp "${jar}" app.jar
            cat > Dockerfile <<'EOF'
            FROM eclipse-temurin:21-jre-alpine
            COPY app.jar /app/app.jar
            WORKDIR /app
            EXPOSE 8081
            ENTRYPOINT ["java","-jar","/app/app.jar"]
            EOF

            echo \$GHCR_PAT | docker login ${REGISTRY} -u ${OWNER} --password-stdin
            docker build -t ${IMAGE_BASE}:${channel}-${BUILD_NUMBER} -t ${IMAGE_BASE}:${channel} .
            docker push  ${IMAGE_BASE}:${channel}-${BUILD_NUMBER}
            docker push  ${IMAGE_BASE}:${channel}
          """

          env.DEPLOY_DIR = (branch == 'main') ? '/app/smartcane/was' : '/app/smartcane/was-dev'
        }
      }
    }

    stage('Deploy to WAS (this host)') {
      steps {
        sh '''
          cd ${DEPLOY_DIR}
          echo $GHCR_PAT | docker login ghcr.io -u '"${OWNER}"' --password-stdin
          docker compose pull
          docker compose up -d
          docker image prune -f
        '''
      }
    }
  }

  post {
    success { echo "배포 성공 🎉 ${env.BRANCH_NAME}" }
    failure { echo "배포 실패 ❌ 콘솔 로그 확인" }
  }
}

pipeline {
  agent any   // ← 여기! docker 에이전트 대신 any 사용

  environment {
    REGISTRY   = "ghcr.io"
    OWNER      = "musclebeaver"              // GitHub 계정/조직명
    APP        = "smartcane-api"
    IMAGE_BASE = "${REGISTRY}/${OWNER}/${APP}"
    GHCR_PAT   = credentials('smartcane-ghcr')   // Jenkins Credentials ID
  }

  options { timestamps(); ansiColor('xterm'); disableConcurrentBuilds() }

  stages {
    stage('Checkout') { steps { checkout scm } }

    stage('Build JAR') {
      steps {
        dir('backend') {
          sh './gradlew clean build -x test'
        }
      }
    }

    stage('Build & Push Image') {
      steps {
        script {
          def branch   = env.BRANCH_NAME ?: 'local'
          def channel  = (branch == 'main') ? 'prod' : (branch == 'dev' ? 'dev' : branch.replaceAll('[^a-zA-Z0-9_.-]','-'))
          def jar      = sh(script: "ls backend/build/libs/*.jar | head -n 1", returnStdout: true).trim()

          sh """
            cp ${jar} backend/app.jar
            cat > backend/Dockerfile <<'EOF'
            FROM eclipse-temurin:21-jre-alpine
            COPY app.jar /app/app.jar
            WORKDIR /app
            EXPOSE 8081
            ENTRYPOINT ["java","-jar","/app/app.jar"]
            EOF

            echo \$GHCR_PAT | docker login ${REGISTRY} -u ${OWNER} --password-stdin
            docker build -t ${IMAGE_BASE}:${channel}-${BUILD_NUMBER} -t ${IMAGE_BASE}:${channel} backend
            docker push  ${IMAGE_BASE}:${channel}-${BUILD_NUMBER}
            docker push  ${IMAGE_BASE}:${channel}
          """

          // 배포 디렉터리 브랜치별 분기 (WAS 동일 호스트)
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
    success {
      script {
        if (env.BRANCH_NAME == 'main') {
          echo "배포 성공 🎉 운영: http://<WAS_IP>:8081"
        } else if (env.BRANCH_NAME == 'dev') {
          echo "배포 성공 🎉 개발: http://<WAS_IP>:8082"
        } else {
          echo "배포 성공 🎉 브랜치 ${env.BRANCH_NAME}"
        }
      }
    }
    failure { echo "배포 실패 ❌ 콘솔 로그 확인" }
  }
}

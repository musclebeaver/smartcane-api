pipeline {
  agent any
  environment {
    REGISTRY   = "ghcr.io"
    OWNER      = "musclebeaver"                 // GitHub 계정/조직
    APP        = "smartcane-api"
    IMAGE_BASE = "${REGISTRY}/${OWNER}/${APP}"
    GHCR_PAT   = credentials('smartcane-ghcr')
  }
  options { timestamps(); disableConcurrentBuilds() }

  stages {
    stage('Checkout') { steps { checkout scm } }

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
          def channel = (branch == 'main') ? 'prod' :
                        (branch == 'dev')  ? 'dev'  :
                        branch.replaceAll('[^a-zA-Z0-9_.-]','-')
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

            # GHCR 로그인 (정상 치환)
            echo "$GHCR_PAT" | docker login ${REGISTRY} -u ${OWNER} --password-stdin

            # 반드시 prod/dev 고정 태그와, 빌드별 태그 둘 다 push
            docker build -t ${IMAGE_BASE}:${channel}-${BUILD_NUMBER} -t ${IMAGE_BASE}:${channel} .
            docker push  ${IMAGE_BASE}:${channel}-${BUILD_NUMBER}
            docker push  ${IMAGE_BASE}:${channel}
          """

          env.DEPLOY_DIR = (branch == 'main') ? '/app/smartcane/was' : '/app/smartcane/was-dev'
          env.DEPLOY_TAG = channel   // prod 또는 dev
        }
      }
    }

    stage('Deploy to WAS (this host)') {
      steps {
        sh '''
          cd ${DEPLOY_DIR}
          # 로그인 username 변수 제대로 치환
          echo "$GHCR_PAT" | docker login ghcr.io -u ${OWNER} --password-stdin

          docker compose pull
          docker compose up -d
          docker image prune -f
        '''
      }
    }
  }

  post {
    success { echo "배포 성공 🎉 ${env.BRANCH_NAME} (${env.DEPLOY_TAG})" }
    failure { echo "배포 실패 ❌ 콘솔 로그 확인" }
  }
}

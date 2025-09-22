pipeline {
  agent any
  environment {
    REGISTRY   = "ghcr.io"
    OWNER      = "musclebeaver"                 // 깃허브 계정/조직
    APP        = "smartcane-api"
    IMAGE_BASE = "${REGISTRY}/${OWNER}/${APP}"
    GHCR_PAT   = credentials('smartcane-ghcr')   // write:packages + read:packages
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
          def channel = (branch == 'main') ? 'prod'
                       : (branch == 'dev')  ? 'dev'
                       : branch.replaceAll('[^a-zA-Z0-9_.-]','-')

          // 산출물 선택
          def jar = sh(script: "ls build/libs/*.jar | head -n 1", returnStdout: true).trim()
          echo "JAR selected: ${jar}"

          // 하나의 sh 블록에서 Dockerfile 생성 + 로그인 + build + push 까지 연속 실행
          sh """
            set -euo pipefail

            cp "${jar}" app.jar

            cat > Dockerfile <<'EOF'
            FROM eclipse-temurin:21-jre-alpine
            COPY app.jar /app/app.jar
            WORKDIR /app
            EXPOSE 8081
            ENTRYPOINT ["java","-jar","/app/app.jar"]
            EOF

            # GHCR 로그인 (Jenkins credentials를 쉘에서 확장해야 하므로 \$를 이스케이프)
            echo "\$GHCR_PAT" | docker login ${REGISTRY} -u ${OWNER} --password-stdin

            # 이미지 빌드 & 푸시 (채널 고정 태그 + 빌드번호 태그)
            docker build -t ${IMAGE_BASE}:${channel}-${BUILD_NUMBER} -t ${IMAGE_BASE}:${channel} .
            docker push  ${IMAGE_BASE}:${channel}-${BUILD_NUMBER}
            docker push  ${IMAGE_BASE}:${channel}
          """

          env.DEPLOY_DIR = (branch == 'main') ? '/app/smartcane/was' : '/app/smartcane/was-dev'
          env.DEPLOY_TAG = channel
        }
      }
    }

    stage('Deploy to WAS (this host)') {
      steps {
        sh '''
          cd ${DEPLOY_DIR}
          echo "$GHCR_PAT" | docker login ghcr.io -u ${OWNER} --password-stdin
          docker compose pull
          docker compose up -d
          docker image prune -f
        '''
      }
    }
  }

  post {
    success { echo "배포 성공 🎉 branch=${env.BRANCH_NAME}, tag=${env.DEPLOY_TAG}" }
    failure { echo "배포 실패 ❌ 콘솔 로그 확인" }
  }
}

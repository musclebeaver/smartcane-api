pipeline {
  agent any
  environment {
    REGISTRY   = "ghcr.io"
    OWNER      = "musclebeaver"               // GitHub 계정/조직
    APP        = "smartcane-api"
    IMAGE_BASE = "${REGISTRY}/${OWNER}/${APP}"
    GHCR_PAT   = credentials('smartcane-ghcr') // write:packages + read:packages
  }
  options { timestamps(); disableConcurrentBuilds() }

  stages {
    stage('Checkout') {
      steps { checkout scm }
    }

    stage('Build & Push Image (with repo Dockerfile)') {
      steps {
        script {
          def branch  = env.BRANCH_NAME ?: 'local'
          def channel = (branch == 'main') ? 'prod'
                       : (branch == 'dev')  ? 'dev'
                       : branch.replaceAll('[^a-zA-Z0-9_.-]','-')

          // GHCR 로그인 (쉘에서 변수 확장하도록 \$ 이스케이프 주의)
          sh '''
            set -euo pipefail
            echo "$GHCR_PAT" | docker login ghcr.io -u ''' + "${OWNER}" + ''' --password-stdin
          '''

          // 레포에 있는 Dockerfile 그대로 사용 (멀티스테이지가 JAR 빌드까지 수행)
          sh """
            set -euo pipefail
            docker build -f Dockerfile \\
              -t ${IMAGE_BASE}:${channel}-${BUILD_NUMBER} \\
              -t ${IMAGE_BASE}:${channel} \\
              .
            docker push ${IMAGE_BASE}:${channel}-${BUILD_NUMBER}
            docker push ${IMAGE_BASE}:${channel}
          """

          env.DEPLOY_DIR = (branch == 'main') ? '/app/smartcane/was' : '/app/smartcane/was-dev'
          env.DEPLOY_TAG = channel
        }
      }
    }

    stage('Deploy to WAS (this host)') {
      steps {
        sh '''
          set -euo pipefail
          cd ${DEPLOY_DIR}
          echo "$GHCR_PAT" | docker login ghcr.io -u ''' + "${OWNER}" + ''' --password-stdin
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

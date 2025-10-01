pipeline {
  agent any
  environment {
    REGISTRY   = "ghcr.io"
    OWNER      = "musclebeaver"                  // GitHub 계정/조직
    APP        = "smartcane-api"
    IMAGE_BASE = "${REGISTRY}/${OWNER}/${APP}"
    GHCR_PAT   = credentials('smartcane-ghcr')   // GHCR PAT (write:packages, read:packages)
  }

  options {
    timestamps()
    disableConcurrentBuilds()
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build & Push Image') {
      steps {
        script {
          def branch  = env.BRANCH_NAME ?: 'local'
          def channel = (branch == 'main') ? 'prod'
                       : (branch == 'dev')  ? 'dev'
                       : branch.replaceAll('[^a-zA-Z0-9_.-]','-')

          sh '''
            set -euo pipefail
            echo "$GHCR_PAT" | docker login ghcr.io -u ''' + "${OWNER}" + ''' --password-stdin
          '''

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

          # 최신 이미지 pull
          docker compose pull smartcane-api

          # 컨테이너 교체 (force recreate + orphan 제거)
          docker compose up -d --force-recreate --remove-orphans smartcane-api

          # 오래된 dangling 이미지 정리
          docker image prune -f

          # 상태 확인
          docker compose ps
        '''
      }
    }
  }

  post {
    success { echo "✅ 배포 성공: branch=${env.BRANCH_NAME}, tag=${env.DEPLOY_TAG}" }
    failure { echo "❌ 배포 실패: 콘솔 로그 확인 필요" }
  }
}

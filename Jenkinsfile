pipeline {
  agent any

  environment {
    REGISTRY   = "ghcr.io"
    OWNER      = "musclebeaver"         // GitHub 사용자명
    IMAGE_NAME = "${REGISTRY}/${OWNER}/smartcane-api"
    GHCR_USER  = "musclebeaver"
    GHCR_PAT   = credentials('smartcane-ghcr')   // Jenkins에 등록한 GHCR 토큰
  }

  options {
    timestamps()
    ansiColor('xterm')
    disableConcurrentBuilds()
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build JAR') {
      steps {
        dir('backend') {
          sh './gradlew clean build -x test'
        }
      }
    }

    stage('Build & Push Docker Image') {
      steps {
        script {
          def jarFile = sh(script: "ls backend/build/libs/*.jar | head -n 1", returnStdout: true).trim()
          sh """
            cp ${jarFile} backend/app.jar

            cat > backend/Dockerfile <<'EOF'
            FROM eclipse-temurin:21-jre-alpine
            COPY app.jar /app/app.jar
            WORKDIR /app
            EXPOSE 8081
            ENTRYPOINT ["java", "-jar", "app.jar"]
            EOF

            echo $GHCR_PAT | docker login $REGISTRY -u $GHCR_USER --password-stdin
            docker build -t $IMAGE_NAME:${BUILD_NUMBER} -t $IMAGE_NAME:latest backend
            docker push $IMAGE_NAME:${BUILD_NUMBER}
            docker push $IMAGE_NAME:latest
          """
        }
      }
    }

    stage('Deploy to WAS') {
      steps {
        sh '''
          cd /app/smartcane/was
          echo $GHCR_PAT | docker login $REGISTRY -u $GHCR_USER --password-stdin
          docker compose pull
          docker compose up -d
          docker image prune -f
        '''
      }
    }
  }

  post {
    success {
      echo "배포 성공 🎉 http://<WAS_IP>:8081 에서 확인하세요"
    }
    failure {
      echo "배포 실패 ❌"
    }
  }
}

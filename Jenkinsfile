pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'chmod +x ./gradlew'
                sh './gradlew clean build -x test'
            }
        }
    }

    post {
        success {
            echo 'CI build succeeded.'
        }
        failure {
            echo 'CI build failed.'
        }
    }
}

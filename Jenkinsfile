pipeline {
    agent any

    environment {
        WAS_HOSTS = "${params.INVEST_CORE_WAS_HOSTS}"
        WAS_USER = "${params.INVEST_CORE_WAS_USER}"
        APP_DIR = "${params.INVEST_CORE_APP_DIR}"
        SSH_CREDENTIAL_ID = "${params.INVEST_CORE_SSH_CREDENTIAL_ID}"
        JAR_NAME = "won-invest-core-${BUILD_NUMBER}.jar"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Test & Build') {
            steps {
                sh 'chmod +x ./gradlew'
                sh 'SPRING_PROFILES_ACTIVE=test ./gradlew clean test bootJar'
                sh 'cp build/libs/*.jar ${JAR_NAME}'
            }
        }

        stage('Transfer & Deploy') {
            when {
                expression {
                    env.BRANCH_NAME == 'main' || env.GIT_BRANCH == 'origin/main' || env.GIT_BRANCH == 'main'
                }
            }
            steps {
                sshagent(credentials: [env.SSH_CREDENTIAL_ID]) {
                    script {
                        for (host in env.WAS_HOSTS.split()) {
                            sh """
                            scp "${JAR_NAME}" "${WAS_USER}@${host}:/tmp/"
                            ssh "${WAS_USER}@${host}" "
                              sudo mv /tmp/${JAR_NAME} ${APP_DIR}/releases/ &&
                              sudo chown deploy:deploy ${APP_DIR}/releases/${JAR_NAME} &&
                              cd ${APP_DIR} &&
                              sudo -u deploy ln -sfn releases/${JAR_NAME} app.jar &&
                              sudo systemctl restart won-invest-core &&
                              for i in 1 2 3 4 5 6; do
                                curl -fsS --connect-timeout 2 --max-time 5 http://localhost:8084/actuator/health && exit 0
                                sleep 5
                              done
                              sudo systemctl status won-invest-core --no-pager
                              exit 1
                            "
                            """
                        }
                    }
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline succeeded.'
        }
        failure {
            echo 'Pipeline failed.'
        }
    }
}

pipeline {
    agent any
    tools {
        maven 'Mvn' // Указываем имя, которое ты задал для установки Maven
    }

    environment {
        APP_NETWORK = 'app-network'  // Название сети, указанное в docker-compose.yml
        APP_NAME = 'myapp'
    }

    stages {
        stage('Clone repository') {
            steps {
                // Клонируем проект из GitHub
                git branch: 'main', url: 'https://github.com/wtor22/QTbonus.git'
            }
        }

        stage('Build application') {
            steps {
                sh 'mvn clean package -DskipTests'
                sh 'ls -l target'
            }
        }

        stage('Build Docker image') {
            steps {
                script {
                    // Сборка Docker-образа на основе Dockerfile
                    try {
                        sh "docker build -t ${env.APP_NAME}:latest ."
                    } catch (Exception e) {
                        echo "Docker build failed: ${e}"
                        currentBuild.result = 'FAILURE'
                        error("Build failed") // Завершаем сборку с ошибкой
                    }
                }
            }
        }

        stage('Deploy application') {
            steps {
                script {
                    // Останавливаем и удаляем старый контейнер, если он существует
                    sh "docker stop ${env.APP_NAME} || true"
                    sh "docker rm ${env.APP_NAME} || true"

                    // Запускаем новый контейнер с указанием сети и проверкой успешности
                    sh """
                        docker run -d -p 8081:8081 --network ${env.APP_NETWORK} \
                        --name ${env.APP_NAME} ${env.APP_NAME}:latest || { echo "Failed to run container"; exit 1; }
                    """

                    // Выводим информацию о контейнерах и образах для проверки
                    sh 'docker ps -a'
                    sh 'docker images'
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}

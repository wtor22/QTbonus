pipeline {
    agent any
    tools {
        maven 'Mvn' // Указываем имя, которое ты задал для установки Maven
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
                sh 'mvn clean package'
                sh 'ls -l target'
            }
        }

        stage('Build Docker image') {
            steps {
                script {
                    // Сборка Docker-образа на основе Dockerfile
                    try {
                        sh 'docker build -t myapp:latest .'
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
                    // Останавливаем старый контейнер (если он есть) и запускаем новый
                    sh 'docker stop myapp || true'
                    sh 'docker rm myapp || true'
                    sh 'docker run -d -p 8081:8081 --name myapp myapp:latest || { echo "Failed to run container"; exit 1; }'
                    sh 'docker ps -a' // Добавляем вывод всех контейнеров
                    sh 'docker images' // Добавляем вывод всех образов
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

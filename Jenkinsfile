pipeline {
    agent any

    stages {
        stage('Clone repository') {
            steps {
                // Клонируем проект из GitHub
                git branch: 'main', url: 'https://github.com/wtor22/QTbonus.git'
            }
        }

        stage('Build application') {
            steps {
                script {
                    // Используем Maven для сборки проекта
                    sh 'mvn clean package'
                    sh 'ls -l target' // Выводим содержимое директории target
                }
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
                    sh 'docker run -d -p 8081:8081 --name myapp myapp:latest'
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

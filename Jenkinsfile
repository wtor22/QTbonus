pipeline {
    agent any

    stages {
        stage('Clone repository') {
            steps {
                // Клонируем проект из GitHub
                git 'https://github.com/yourusername/yourrepository.git'
            }
        }

        stage('Build application') {
            steps {
                // Сборка Maven проекта
                sh 'mvn clean package'
            }
        }

        stage('Build Docker image') {
            steps {
                script {
                    // Сборка Docker-образа на основе Dockerfile
                    sh 'docker build -t myapp:latest .'
                }
            }
        }

        stage('Deploy application') {
            steps {
                script {
                    // Останавливаем старый контейнер (если он есть) и запускаем новый
                    sh 'docker stop myapp || true'
                    sh 'docker rm myapp || true'
                    sh 'docker run -d -p 8081:8080 --name myapp myapp:latest'
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

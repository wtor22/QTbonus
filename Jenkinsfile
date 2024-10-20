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
                }
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

pipeline {
    agent any

    tools {
        maven 'Default' 
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo 'Building Spring Boot application...'
                // Skip tests in the build stage to handle them separately
                sh 'mvn clean compile -DskipTests'
            }
        }

        stage('Unit Test') {
            steps {
                echo 'Running unit tests...'
                // Run only the tests and generate reports
                sh 'mvn test'
            }
            post {
                always {
                    // Record JUnit test results for the Jenkins UI
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Package') {
            steps {
                echo 'Packaging application...'
                sh 'mvn package -DskipTests'
            }
        }

        stage('Archive Artifacts') {
            steps {
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }
    }

    post {
        always {
            echo 'Build finished.'
        }
        success {
            echo 'Build Succeeded!'
        }
        failure {
            echo 'Build Failed. Check logs.'
        }
    }
}

pipeline {
    agent any

    tools {
        // This must match the name of the Maven installation 
        // in Jenkins -> Manage Jenkins -> Global Tool Configuration
        maven 'Default' 
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Unit Test') {
            steps {
                echo 'Building Spring Boot application...'
                sh 'mvn clean package'
            }
        }

        stage('Archive Artifacts') {
            steps {
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }
        
        // We will add the Dev/Prod deployment stages once 
        // we verify the build works!
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

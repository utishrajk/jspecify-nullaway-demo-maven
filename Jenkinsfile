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

        stage('Determine Version') {
            steps {
                script {
                    // Read the base version from pom.xml (e.g., 1.0.0)
                    def pom = readMavenPom file: 'pom.xml'
                    def baseVersion = pom.version.replace("-SNAPSHOT", "")
                    
                    // Create a unique version: BaseVersion.BuildNumber (e.g., 1.0.0.15)
                    env.DYNAMIC_VERSION = "${baseVersion}.${env.BUILD_NUMBER}"
                    echo "Calculated Dynamic Version: ${env.DYNAMIC_VERSION}"
                }
            }
        }

        stage('Set Version') {
            steps {
                sh "mvn versions:set -DnewVersion=${env.DYNAMIC_VERSION}"
            }
        }

        stage('Build & Test') {
            steps {
                sh 'mvn clean compile test'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Package') {
            steps {
                sh 'mvn package -DskipTests'
            }
        }

        stage('Publish to Nexus') {
            steps {
                script {
                    def jarName = "jspecify-nullway-demo-${env.DYNAMIC_VERSION}.jar"
                    def nexusUrl = "http://nexus-server:8081/repository/maven-snapshots/"
                    withCredentials([usernamePassword(credentialsId: 'nexus-creds', usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PWD')]) {
                        sh "curl -v -u ${NEXUS_USER}:${NEXUS_PWD} --upload-file target/${jarName} ${nexusUrl}com/example/jspecify-nullway-demo/${env.DYNAMIC_VERSION}/${jarName}"
                    }
                }
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
    }
}

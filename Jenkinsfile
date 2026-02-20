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
                    def pom = readMavenPom file: 'pom.xml'
                    def baseVersion = pom.version.replace("-SNAPSHOT", "")
                    env.DYNAMIC_VERSION = "${baseVersion}.${env.BUILD_NUMBER}"
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
                    def nexusUrl = "http://nexus-server:8081/repository/maven-releases/"
                    withCredentials([usernamePassword(credentialsId: 'nexus-creds', usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PWD')]) {
                        sh "curl -v -u ${NEXUS_USER}:${NEXUS_PWD} --upload-file target/${jarName} ${nexusUrl}com/example/jspecify-nullway-demo/${env.DYNAMIC_VERSION}/${jarName}"
                    }
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    sh "docker build -t jspecify-demo:latest ."
                }
            }
        }

        stage('Deploy to Dev Cluster') {
            steps {
                script {
                    echo "Loading image to Kind cluster..."
                    sh "kind load docker-image jspecify-demo:latest --name dev-cluster"
                    
                    echo "Applying Kubernetes manifests..."
                    sh "kubectl apply -f deployment.yaml"
                    
                    echo "Waiting for rollout to complete..."
                    sh "kubectl rollout status deployment/jspecify-demo"
                }
            }
        }

        stage('Verify Pods') {
            steps {
                script {
                    sh "kubectl get pods -l app=jspecify-demo"
                    def podStatus = sh(script: "kubectl get pods -l app=jspecify-demo -o jsonpath='{.items[*].status.phase}'", returnStdout: true).trim()
                    echo "Pod Statuses: ${podStatus}"
                    
                    if (podStatus.contains("Running")) {
                        echo "SUCCESS: App is up and running in pods!"
                    } else {
                        error "FAIL: Pods are not in Running state."
                    }
                }
            }
        }
    }
}

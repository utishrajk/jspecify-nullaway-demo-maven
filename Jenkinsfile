pipeline {
    agent any

    options {
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

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
                    sh "docker build -t jspecify-demo:${env.DYNAMIC_VERSION} -t jspecify-demo:latest ."
                }
            }
        }

        stage('Deploy to Dev Cluster') {
            steps {
                script {
                    sh "kubectl config use-context dev-cluster"
                    sh "kubectl create namespace dev --dry-run=client -o yaml | kubectl apply -f -"
                    
                    echo "Loading image to Minikube Dev Cluster via Docker Pipe..."
                    // Using direct docker pipe to bypass Minikube SSH issues
                    sh "docker save jspecify-demo:${env.DYNAMIC_VERSION} | docker exec -i dev-cluster docker load"
                    
                    sh "sed -i 's/VERSION_PLACEHOLDER/${env.DYNAMIC_VERSION}/g' deployment.yaml"
                    sh "kubectl apply -f deployment.yaml -n dev"
                    sh "kubectl rollout restart deployment/jspecify-demo -n dev"
                    sh "kubectl rollout status deployment/jspecify-demo -n dev"
                }
            }
        }

        stage('Integration Test (Dev)') {
            steps {
                script {
                    echo "Starting port-forward for Dev..."
                    def portBusy = sh(script: "netstat -tuln | grep :8082 || true", returnStdout: true).trim()
                    if (!portBusy) {
                        sh "kubectl port-forward -n dev service/jspecify-demo-service 8082:8082 --address 0.0.0.0 > dev_pf.log 2>&1 &"
                        sleep 10
                    }
                    sh 'curl -s -o /dev/null -w "%{http_code}" http://127.0.0.1:8082/hello'
                }
            }
        }

        stage('Manual Approval') {
            steps {
                input message: "Promote version ${env.DYNAMIC_VERSION} to Production?", ok: "Deploy to Prod"
            }
        }

        stage('Deploy to Production') {
            steps {
                script {
                    echo "Deploying to Minikube Production Cluster via Docker Pipe..."
                    sh "kubectl config use-context prod-cluster"
                    sh "kubectl create namespace prod --dry-run=client -o yaml | kubectl apply -f -"
                    
                    sh "docker save jspecify-demo:${env.DYNAMIC_VERSION} | docker exec -i prod-cluster docker load"
                    
                    sh "kubectl apply -f deployment.yaml -n prod"
                    sh "kubectl rollout restart deployment/jspecify-demo -n prod"
                    sh "kubectl rollout status deployment/jspecify-demo -n prod"
                }
            }
        }

        stage('Verify Production') {
            steps {
                script {
                    sh "kubectl get pods -n prod -l app=jspecify-demo"
                    def podStatus = sh(script: "kubectl get pods -n prod -l app=jspecify-demo -o jsonpath='{.items[*].status.phase}'", returnStdout: true).trim()
                    if (podStatus.contains("Running")) {
                        echo "SUCCESS: Production is LIVE!"
                    } else {
                        error "FAIL: Production pods not running."
                    }
                }
            }
        }
    }
}

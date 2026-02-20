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
                    sh "docker build -t jspecify-demo:${env.DYNAMIC_VERSION} -t jspecify-demo:latest ."
                }
            }
        }

        stage('Deploy to Dev Cluster') {
            steps {
                script {
                    sh "kubectl config use-context kind-dev-cluster"
                    sh "kind load docker-image jspecify-demo:${env.DYNAMIC_VERSION} --name dev-cluster"
                    
                    echo "Injecting version ${env.DYNAMIC_VERSION} into deployment.yaml"
                    sh "sed -i 's/VERSION_PLACEHOLDER/${env.DYNAMIC_VERSION}/g' deployment.yaml"
                    
                    sh "kubectl apply -f deployment.yaml"
                    sh "kubectl rollout restart deployment/jspecify-demo"
                    sh "kubectl rollout status deployment/jspecify-demo"
                }
            }
        }

        stage('Integration Test (Dev)') {
            steps {
                script {
                    echo "Ensuring port-forward is active..."
                    def portBusy = sh(script: "netstat -tuln | grep :8082 || true", returnStdout: true).trim()
                    
                    if (!portBusy) {
                        sh "kubectl port-forward service/jspecify-demo-service 8082:8082 --address 0.0.0.0 > pf.log 2>&1 &"
                        sleep 10
                    }
                    
                    sh """
                        response=\$(curl -s -o /dev/null -w "%{http_code}" http://127.0.0.1:8082/hello)
                        if [ "\$response" -eq 200 ]; then
                            echo "Integration Test Passed"
                        else
                            exit 1
                        fi
                    """
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
                    echo "Deploying to Production Cluster..."
                    sh "kubectl config use-context kind-prod-cluster"
                    sh "kind load docker-image jspecify-demo:${env.DYNAMIC_VERSION} --name prod-cluster"
                    
                    // The sed replace was already done in the Dev stage, so deployment.yaml is ready
                    sh "kubectl apply -f deployment.yaml"
                    sh "kubectl rollout restart deployment/jspecify-demo"
                    sh "kubectl rollout status deployment/jspecify-demo"
                }
            }
        }

        stage('Verify Production') {
            steps {
                script {
                    sh "kubectl get pods -l app=jspecify-demo"
                    def podStatus = sh(script: "kubectl get pods -l app=jspecify-demo -o jsonpath='{.items[*].status.phase}'", returnStdout: true).trim()
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

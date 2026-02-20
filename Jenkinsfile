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
                    // We use the host's kind since we mapped the docker socket
                    sh "kind load docker-image jspecify-demo:latest --name dev-cluster"
                    
                    echo "Applying Kubernetes manifests..."
                    sh "kubectl apply -f deployment.yaml"
                    
                    echo "Forcing pod restart to pick up new image..."
                    sh "kubectl rollout restart deployment/jspecify-demo"
                    
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

        stage('Integration Test') {
            steps {
                script {
                    echo "Running integration tests against the cluster..."
                    // Wait a few seconds for the app to initialize inside the container
                    sleep 10
                    
                    // We use the service name or pod IP to test connectivity internally
                    // Since we already have a port-forward on 8082 in the background, we can test that
                    sh """
                        response=\$(curl -s -o /dev/null -w "%{http_code}" http://0.0.0.0:8082/hello)
                        echo "Response code: \$response"
                        if [ "\$response" -eq 200 ]; then
                            echo "Integration Test Passed: /hello returned 200 OK"
                        else
                            echo "Integration Test Failed: Received \$response"
                            exit 1
                        fi
                    """
                }
            }
        }
    }
}

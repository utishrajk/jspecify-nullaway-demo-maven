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
                    
                    echo "Forcing pod restart to pick up new image..."
                    sh "kubectl rollout restart deployment/jspecify-demo"
                    
                    echo "Waiting for rollout to complete..."
                    sh "kubectl rollout status deployment/jspecify-demo"
                }
            }
        }

        stage('Integration Test (Dev)') {
            steps {
                script {
                    echo "Starting/Verifying port-forward..."
                    // We start it but don't kill it so it stays persistent for the user
                    sh "kubectl port-forward service/jspecify-demo-service 8082:8082 --address 0.0.0.0 > pf.log 2>&1 &"
                    sleep 5
                    
                    echo "Running integration tests against http://0.0.0.0:8082/hello..."
                    sh """
                        response=\$(curl -s -o /dev/null -w "%{http_code}" http://0.0.0.0:8082/hello)
                        echo "Response code: \$response"
                        if [ "\$response" -eq 200 ]; then
                            echo "Integration Test Passed"
                        else
                            echo "Integration Test Failed"
                            exit 1
                        fi
                    """
                }
            }
        }

        stage('Manual Approval') {
            steps {
                input message: "Does the Dev deployment look good? Promote to Production?", ok: "Deploy to Prod"
            }
        }

        stage('Deploy to Production') {
            steps {
                echo "Deploying version ${env.DYNAMIC_VERSION} to Production Cluster..."
                // Logic for second cluster (prod-cluster) goes here
                sh "echo 'Production deployment logic would run here'"
            }
        }
    }
}

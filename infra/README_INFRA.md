# Infrastructure Backup & Restore Guide

## System Architecture

```mermaid
flowchart TB
    subgraph UserSpace ["User Access Layer"]
        User([User Browser])
        Hosts["/etc/hosts Aliases<br/>(nexus.hello.com, etc.)"]
    end

    subgraph Host ["Local Laptop (Docker Engine)"]
        direction TB
        Proxy[Nginx Reverse Proxy<br/>'devops-proxy' @ Port 80]
        
        subgraph DevOpsTools ["Core Services (devops-net bridge)"]
            Jenkins[Jenkins Server<br/>Port 8080]
            Nexus[Nexus Repo<br/>Port 8081]
            Splunk[Splunk Enterprise<br/>Port 8000/8088]
            Portainer[Portainer UI<br/>Port 9000]
            Socat[Log Proxy<br/>Socat :8888]
        end
    end

    subgraph K8sDev ["Minikube Dev Cluster (192.168.49.2)"]
        direction LR
        DevApp[jspecify-demo-app<br/>:8083]
        DevSvc[NodePort Service<br/>:30265]
        DevFB[Fluent Bit<br/>Log Forwarder]
    end

    subgraph K8sProd ["Minikube Prod Cluster (192.168.58.2)"]
        direction LR
        ProdApp[jspecify-demo-app<br/>:8083]
        ProdSvc[NodePort Service<br/>:31581]
        ProdFB[Fluent Bit<br/>Log Forwarder]
    end

    %% Routing Flow
    User --> Hosts
    Hosts --> Proxy
    Proxy -- "nexus.hello.com" --> Nexus
    Proxy -- "jenkins.hello.com" --> Jenkins
    Proxy -- "splunk.hello.com" --> Splunk
    Proxy -- "portainer.hello.com" --> Portainer
    Proxy -- "dev.hello.com" --> DevSvc
    Proxy -- "prod.hello.com" --> ProdSvc

    %% Service -> Target Flow
    DevSvc --> DevApp
    ProdSvc --> ProdApp

    %% Log Forwarding Flow
    DevApp -- "Container Logs" --> DevFB
    ProdApp -- "Container Logs" --> ProdFB
    DevFB -- "HEC (HTTPS :8088)<br/>via 192.168.49.1" --> Splunk
    ProdFB -- "HEC (HTTPS :8088)<br/>via host.minikube.internal" --> Splunk

    %% Jenkins Automation
    Jenkins -- "Build & Push" --> Nexus
    Jenkins -- "kubectl apply" --> K8sDev
    Jenkins -- "kubectl apply" --> K8sProd

    style User fill:#f9f,stroke:#333,stroke-width:2px
    style Proxy fill:#69f,stroke:#333,stroke-width:2px
    style Splunk fill:#f66,stroke:#333,stroke-width:2px
    style Jenkins fill:#fa0,stroke:#333,stroke-width:2px
```

This directory contains a complete backup of the DevOps infrastructure state as of the "first stable state" commit.

## Structure

*   `docker-compose.yaml`: Defines the host-level services (Jenkins, Nexus, Splunk, Portainer, Nginx Proxy). Use `docker-compose up -d` to restore.
*   `nginx/`: Nginx reverse proxy configuration.
*   `fluent-bit/`: Fluent Bit manifests deployed to the **Dev** cluster.
*   `k8s/`:
    *   `dev/`: Deployments and Services running in the `dev` namespace of `dev-cluster`.
    *   `prod/`: Deployments and Services running in the `prod` namespace of `prod-cluster`.
    *   `prod/fluent-bit/`: Fluent Bit manifests running in the `kube-system` namespace of `prod-cluster`.
*   `splunk/`:
    *   `inputs.conf`: The HEC configuration from the Splunk server.
    *   `token.txt`: The specific HEC token used by Fluent Bit.
*   `setup_hosts.sh`: Script to add local DNS aliases to `/etc/hosts`.

## Service Network Directory

| Service | Friendly URL | Real Internal URL | Description |
| :--- | :--- | :--- | :--- |
| **Jenkins** | [http://jenkins.hello.com](http://jenkins.hello.com) | `http://127.0.0.1:8080` | Main CI/CD Dashboard |
| **Nexus** | [http://nexus.hello.com](http://nexus.hello.com) | `http://127.0.0.1:8081` | Artifact Repository (Releases) |
| **Splunk UI** | [http://splunk.hello.com](http://splunk.hello.com) | `http://127.0.0.1:8000` | Log Analysis & Visualization |
| **Splunk HEC** | -- | `https://127.0.0.1:8088` | Log Collector (HEC) |
| **Portainer** | [http://portainer.hello.com](http://portainer.hello.com) | `http://127.0.0.1:9000` | Docker Container Management |
| **Dev App** | [http://dev.hello.com](http://dev.hello.com) | `http://192.168.49.2:30265` | JSpecify App in Dev Cluster |
| **Prod App** | [http://prod.hello.com](http://prod.hello.com) | `http://192.168.58.2:31581` | JSpecify App in Prod Cluster |
| **Log Proxy** | -- | `http://127.0.0.1:8888` | Socat tunnel to Splunk HEC |

> **Note:** The "Friendly URLs" are managed by the Nginx Reverse Proxy (`devops-proxy`) running on port 80 of your host machine.

---

## Build & Deployment Lifecycle

### 1. Application Build (Maven)
- **JDK Match:** The project is configured for **Java 21**.
- **Command:** `mvn clean package`
- **Output:** A fat JAR located at `target/jspecify-nullway-demo-*.jar`.

### 2. Image Creation (Docker)
- **Base Image:** `eclipse-temurin:21-jre-alpine` (Matches the application's Java 21 requirement).
- **Process:** The `Dockerfile` copies the compiled JAR into the `/app` directory inside the container as `app.jar`.
- **Command:** `docker build -t jspecify-demo:latest .`

### 3. Minikube Image Loading (The "Docker Pipe")
Because Minikube runs in its own Docker-in-Docker (or KIC) container, it cannot see host images directly. We use a high-speed pipe to transfer the image:
```bash
docker save jspecify-demo:latest | docker exec -i dev-cluster docker load
```
This bypasses the need for a private registry or slow SSH transfers.

### 4. Pod Creation (Kubernetes)
- **Manifest:** `deployment.yaml` (Source) or `infra/k8s/` (Backup).
- **Pull Policy:** Set to `imagePullPolicy: Never`. This forces Kubernetes to use the image we manually loaded into the node rather than trying to pull it from a remote registry.
- **Instantiation:** 
  ```bash
  kubectl apply -f deployment.yaml -n dev
  ```

---

## CI/CD Pipeline Sequence (Jenkinsfile)

The Jenkins pipeline automates the entire lifecycle from commit to production. Here is the step-by-step sequence:

### 1. Dynamic Versioning & Incrementing
- **Mechanism:** The pipeline reads the base version from `pom.xml` (e.g., `1.0`).
- **Incrementing:** It appends the Jenkins `${BUILD_NUMBER}` to create a unique version: `${baseVersion}.${env.BUILD_NUMBER}` (e.g., `1.0.45`).
- **Enforcement:** `mvn versions:set -DnewVersion=${env.DYNAMIC_VERSION}` is called to update the project before any build happens.

### 2. Build & Quality Gate (JUnit)
- **Compilation:** `mvn clean compile` ensures code is valid.
- **Unit Testing:** `mvn test` runs the JUnit 5 suite.
- **Reporting:** Jenkins captures these results using `junit '**/target/surefire-reports/*.xml'`, providing a visual breakdown of pass/fail tests in the UI.

### 3. Artifact Storage (Nexus)
- **Repository:** The built JAR is published to the `maven-releases` repository in Nexus.
- **URL:** `http://nexus-server:8081/repository/maven-releases/com/example/jspecify-nullway-demo/`
- **Method:** `curl` with `usernamePassword` credentials uploads the specifically versioned JAR (e.g., `jspecify-nullway-demo-1.0.45.jar`).

### 4. Docker Image Construction
- **Tagging:** Two tags are created: `jspecify-demo:${DYNAMIC_VERSION}` and `jspecify-demo:latest`.
- **JDK:** Built against **Java 21** as specified in the `Dockerfile` and `pom.xml`.

### 5. Deployment to Development (Minikube Dev)
- **Targeting:** `kubectl config use-context dev-cluster`.
- **Namespace:** Deployed into the `dev` namespace.
- **Scaling:** The `deployment.yaml` specifies **2 replicas** (pods).
- **Image Injection:** The image is "piped" directly into the cluster's internal Docker runtime:
  `docker save ... | docker exec -i dev-cluster docker load`
- **Instantiation:** `sed` replaces `VERSION_PLACEHOLDER` in the manifest before `kubectl apply`.

### 6. Integration Testing (Dev)
- **Process:**
  1. Pipeline checks for port conflicts on port `8082`.
  2. If clear, it starts a background `kubectl port-forward` to expose the Dev service.
  3. It executes a `curl` against `http://127.0.0.1:8082/hello`.
  4. If the HTTP response is not `200`, the pipeline fails, preventing promotion to Prod.

### 7. Promotion & Production (Minikube Prod)
- **Manual Gate:** A `Manual Approval` stage requires a user to click "Deploy to Prod" in Jenkins.
- **Targeting:** `kubectl config use-context prod-cluster`.
- **Namespace:** Deployed into the `prod` namespace.
- **Final Verification:** The pipeline queries the pod status using `jsonpath='{.items[*].status.phase}'`. It only succeeds if the status is exactly `Running`.

---

## Restoration Steps

1.  **Host Services:**
    ```bash
    cd infra
    docker-compose up -d
    ./setup_hosts.sh
    ```

2.  **Splunk Configuration:**
    *   After Splunk starts, copy `splunk/inputs.conf` to the container:
        ```bash
        docker cp splunk/inputs.conf splunk-server:/opt/splunk/etc/apps/splunk_httpinput/local/
        docker restart splunk-server
        ```

3.  **Kubernetes Clusters (Minikube):**
    *   Ensure clusters are running (`minikube start -p dev-cluster`, `minikube start -p prod-cluster`).
    *   Apply Dev manifests:
        ```bash
        kubectl config use-context dev-cluster
        kubectl apply -f k8s/dev/
        kubectl apply -f fluent-bit/ # Apply Fluent Bit to Dev
        ```
    *   Apply Prod manifests:
        ```bash
        kubectl config use-context prod-cluster
        kubectl apply -f k8s/prod/
        kubectl apply -f k8s/prod/fluent-bit/
        ```

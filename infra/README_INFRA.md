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

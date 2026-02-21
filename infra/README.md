# DevOps Infrastructure

This directory contains the configuration and scripts to set up the local DevOps environment.

## Components

1.  **Nginx Reverse Proxy**: Maps local hostnames (e.g., `jenkins.hello.com`) to internal service ports.
    - Configuration: `nginx/nginx.conf`
    - Run command: `docker run -d --name devops-proxy --network host -v $(pwd)/nginx/nginx.conf:/etc/nginx/nginx.conf:ro nginx:alpine`

2.  **Fluent Bit (Log Forwarding)**: Collects logs from Minikube clusters and sends them to Splunk.
    - Configuration: `fluent-bit/*.yaml`
    - Setup: `kubectl apply -f fluent-bit/`

3.  **Host Aliases**: Script to update `/etc/hosts`.
    - Script: `setup_hosts.sh`

## Accessing Services

- **Nexus**: [http://nexus.hello.com](http://nexus.hello.com)
- **Jenkins**: [http://jenkins.hello.com](http://jenkins.hello.com)
- **Splunk**: [http://splunk.hello.com](http://splunk.hello.com)
- **Portainer**: [http://portainer.hello.com](http://portainer.hello.com)
- **Minikube Dev**: [http://dev.hello.com](http://dev.hello.com)
- **Minikube Prod**: [http://prod.hello.com](http://prod.hello.com)

#!/bin/bash
# Script to add local DevOps aliases to /etc/hosts

ALIASES=(
    "127.0.0.1 nexus.hello.com"
    "127.0.0.1 jenkins.hello.com"
    "127.0.0.1 dev.hello.com"
    "127.0.0.1 prod.hello.com"
    "127.0.0.1 splunk.hello.com"
    "127.0.0.1 portainer.hello.com"
)

echo "Adding aliases to /etc/hosts..."
for alias in "${ALIASES[@]}"; do
    if ! grep -q "$alias" /etc/hosts; then
        echo "Adding $alias"
        echo "$alias" | sudo tee -a /etc/hosts
    else
        echo "$alias already exists"
    fi
done

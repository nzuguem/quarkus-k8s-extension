#!/bin/bash
openssl genrsa -out ca.key 2048
openssl req -x509 -new -nodes -days 365 -key ca.key -out ca.crt -subj "/CN=prod.svc.url"
kubectl create secret tls tuto-quarkus-k8s-tls-secret --key ca.key --cert ca.crt -n tuto-quarkus-k8s
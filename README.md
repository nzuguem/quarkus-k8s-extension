# [Kubernetes Extension][quarkus-k8s-extension], Késako ?
Extension that supports automatic generation of manifests for Kubernetes, OpenShift, Knative platforms. It is based on the **_[Dekorate][dekorate]_** lib, and configurations (in the form of quarkus properties) supplied by users. \
_In this tutorial we use the Kuberneters platform by default._

Kubernetes manifests are generated at application build (`./gradlew build`) time in `build/kuberneters`

Manifest output directory can be changed :
```properties
quarkus.kubernetes.output-directory=build/k8s
```

You can then apply the generated manifests:
```bash
# Cluster setup for this tutorial
kubectl apply -f k8s/

kubectl apply -f build/k8s/kubernetes.yml
```

> The [Container Image extension][quarkus-container-image-extension] lets you automatically build and push Docker images when deploying them on a target platform.

# Manifests Kubernetes
## Container Image
- The generated docker image of Pod, will take the form : `<DOCKER_HOST_USER>/<APPLICATION_NAME>:<APPLICATION_VERSION>`
- These properties can be overloaded
```properties
quarkus.container-image.group=quarkus #optional, default to the system username
quarkus.container-image.name=demo-app #optional, defaults to the application name
quarkus.container-image.tag=1.0       #optional, defaults to the application version
```
⚠️ _You'll need a quarkus extension for Container Image. We used Jib_

## GitOps compatibility
All k8s objects generated by Quakus contain the following annotations : 
```yaml
metadata:
  annotations:
    app.quarkus.io/build-timestamp: 2023-08-18 - 07:25:56 +0000
```

The values of these annotations are too changeable, due to the fact that applications are too often built (on the dev workstation or on CI). This is problematic with GitOps tools, as these changes will lead to new deployments.

The ideal solution would be to do without it and be compatible with GitOps tools :
```properties
quarkus.kubernetes.idempotent=true
```

## Deployment kind
By default, the generated Pod is controlled by the k8s `Deployment` object. But the following objects can also be used:
- `StatefulSet`
  - `quarkus.kubernetes.deployment-kind=StatefulSet` 
- `Job`
  - [`quarkus.kubernetes.deployment-kind=Job`][job-configuration]
- `CronJob`
  - [`quarkus.kubernetes.deployment-kind=CronJob`][cron-job-configuration]

## Namespace
No namespace is specified in the manifests, so resources will be created in the default namespace. We can specify our own namespaces : 
```properties
quarkus.kubernetes.namespace=tuto-quarkus-k8s
```

## Labels
[Certain labels are recommended by k8s][recommended-labels-k8s], and we can define them as follows: 
```properties
quarkus.kubernetes.part-of=tutorials
quarkus.kubernetes.name=quarkus-k8s   # Generated by default
quarkus.kubernetes.version=1.0
```

We can define our own labels, respecting the format \
We can enclose the key in double quotation marks for greater precision. It will remain mandatory if the key contains the "." character.

Example :
```properties
# Format : quarkus.kubernetes.labels.<KEY>=<VALUE>
quarkus.kubernetes.labels."app.quarkus.io/author"=Kevin.Nzuguem
```

## Annotations
Specifying annotations is very similar to specifying labels.

Example :
```properties
# Format : quarkus.kubernetes.annotations.<KEY>=<VALUE>
quarkus.kubernetes.annotations."app.quarkus.io/position"=1
```

## Environment variables
Three ways to inject environment variables :
- **_env - value_** \
  The value of the environment variable is set directly in the manifest
  ```properties
  # Format : quarkus.kubernetes.env.vars.<ENV_KEY>=<ENV_VALUE>
  quarkus.kubernetes.env.vars.greeting-name=RESTEasy Reactive EnvValue
  ```
  ```yaml
  # Result
  env:
  - name: GREETING_NAME
    value: RESTEasy Reactive EnvValue
  ```

- **_env - valueFrom_** \
  The value of the environment variable is extracted either from a `Secret`, a `ConfigMap` or a Pod field
  ```properties
  # ConfigMap
  # Format : quarkus.kubernetes.env.mapping.<ENV_KEY>.from-configmap=<NAME OF CONFIGMAP>
  quarkus.kubernetes.env.mapping.greeting-name-cm.from-configmap=tuto-quarkus-k8s-greeting-cm
  # Format : quarkus.kubernetes.env.mapping.<ENV_KEY>.with-key=<KEY DATA ON CONFIGMAP>
  quarkus.kubernetes.env.mapping.greeting-name-cm.with-key=GREETING_NAME_CM
  
  # Secret
  # Format : quarkus.kubernetes.env.mapping.<ENV_KEY>.from-secret=<NAME OF SECRET>
  quarkus.kubernetes.env.mapping.greeting-name-secret.from-secret=tuto-quarkus-k8s-greeting-secret
  # Format : quarkus.kubernetes.env.mapping.<ENV_KEY>.with-key=<KEY DATA ON SECRET>
  quarkus.kubernetes.env.mapping.greeting-name-secret.with-key=GREETING_NAME_SECRET
  
  # Field
  quarkus.kubernetes.env.fields.instance-name=metadata.name
  ```
  ```yaml
  # Result
  env:
  - name: GREETING_NAME_SECRET
    valueFrom:
      secretKeyRef:
        key: GREETING_NAME_SECRET
        name: tuto-quarkus-k8s-greeting-secret
  - name: GREETING_NAME_CM
    valueFrom:
      configMapKeyRef:
        key: GREETING_NAME_CM
        name: tuto-quarkus-k8s-greeting-cm
  - name: GREETING_NAME
    value: RESTEasy Reactive EnvValue
  - name: INSTANCE_NAME
    valueFrom:
      fieldRef:
        fieldPath: metadata.name
  ```
  
- **_envFrom_** \
  Environment variables (**_key and value_**) are extracted from either `ConfigMap` or `Secret` 
  ```properties
  # ConfigMap
  # Format: quarkus.kubernetes.env.configmaps=<LIST OF CONFIGMAPS. Separate by ",">
  quarkus.kubernetes.env.configmaps=tuto-quarkus-k8s-greeting-cm
  
  # Secret
  # Format: quarkus.kubernetes.env.secrets=<LIST OF SECRETS. Separate by ",">
  quarkus.kubernetes.env.secrets=tuto-quarkus-k8s-greeting-secret
  ```
  ```yaml
  # Result
  envFrom:
  - secretRef:
      name: tuto-quarkus-k8s-greeting-secret
  - configMapRef:
      name: tuto-quarkus-k8s-greeting-cm
  ```

<!-- All resources links -->
[job-configuration]:  https://quarkus.io/guides/deploying-to-kubernetes#quarkus-kubernetes-kubernetes-config_quarkus.kubernetes.job.parallelism
[cron-job-configuration]: https://quarkus.io/guides/deploying-to-kubernetes#quarkus-kubernetes-kubernetes-config_quarkus.kubernetes.cron-job.parallelism
[recommended-labels-k8s]: https://kubernetes.io/docs/concepts/overview/working-with-objects/common-labels
[quarkus-k8s-extension]: https://quarkus.io/guides/deploying-to-kubernetes
[dekorate]: https://dekorate.io/
[quarkus-container-image-extension]: https://quarkus.io/guides/container-image
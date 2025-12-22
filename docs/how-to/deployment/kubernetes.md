---
description: >-
  Deploy moclojer to Kubernetes - Deployments, Services, ConfigMaps, and best practices
  for production-ready K8s deployments with health checks and scaling.
---

# Kubernetes Deployment

Deploy moclojer to Kubernetes for production-grade mock APIs with scaling and high availability.

## 📋 What You'll Build

- ✅ Kubernetes Deployment
- ✅ Service (ClusterIP/LoadBalancer)
- ✅ ConfigMap for configuration
- ✅ Health checks (liveness/readiness)
- ✅ Resource limits
- ✅ Horizontal Pod Autoscaling

## 🚀 Quick Start

### 1. ConfigMap for Configuration

```yaml
# configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: moclojer-config
  namespace: default
data:
  moclojer.yml: |
    - endpoint:
        method: GET
        path: /health
        response:
          status: 200
          body: >
            {"status": "ok", "service": "moclojer"}
    
    - endpoint:
        method: GET
        path: /api/users
        response:
          status: 200
          body: >
            {"users": [{"id": 1, "name": "Alice"}]}
```

### 2. Deployment

```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: moclojer
  namespace: default
  labels:
    app: moclojer
spec:
  replicas: 2
  selector:
    matchLabels:
      app: moclojer
  template:
    metadata:
      labels:
        app: moclojer
    spec:
      containers:
      - name: moclojer
        image: ghcr.io/moclojer/moclojer:latest
        ports:
        - containerPort: 8000
          name: http
        env:
        - name: PORT
          value: "8000"
        - name: CONFIG
          value: "/config/moclojer.yml"
        volumeMounts:
        - name: config
          mountPath: /config
          readOnly: true
        resources:
          requests:
            memory: "128Mi"
            cpu: "100m"
          limits:
            memory: "512Mi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /health
            port: 8000
          initialDelaySeconds: 10
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /health
            port: 8000
          initialDelaySeconds: 5
          periodSeconds: 10
      volumes:
      - name: config
        configMap:
          name: moclojer-config
```

### 3. Service

```yaml
# service.yaml
apiVersion: v1
kind: Service
metadata:
  name: moclojer
  namespace: default
spec:
  type: ClusterIP
  selector:
    app: moclojer
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8000
    name: http
```

## 📦 Deploy

```bash
# Apply all resources
kubectl apply -f configmap.yaml
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml

# Check status
kubectl get pods -l app=moclojer
kubectl get svc moclojer

# Test
kubectl port-forward svc/moclojer 8000:80
curl http://localhost:8000/health
```

## 🔧 Advanced Configurations

### Horizontal Pod Autoscaler

```yaml
# hpa.yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: moclojer
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: moclojer
  minReplicas: 2
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
```

### Ingress (NGINX)

```yaml
# ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: moclojer
  annotations:
    kubernetes.io/ingress.class: nginx
spec:
  rules:
  - host: mock-api.example.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: moclojer
            port:
              number: 80
```

## ✅ Best Practices

**Do ✅:**
- Use ConfigMaps for configuration
- Set resource limits
- Configure health checks
- Use specific image tags (not `latest`)
- Enable horizontal scaling

**Don't ❌:**
- Don't hardcode configs in images
- Don't skip health checks
- Don't use excessive resources

## 📚 See Also

- **[Docker Deployment](docker.md)**
- **[Cloud Run](cloud-run.md)**
- **[Integration Testing](../testing/integration-testing.md)**

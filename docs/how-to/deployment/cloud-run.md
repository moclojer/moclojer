---
description: >-
  Deploy moclojer to Google Cloud Run - serverless, autoscaling mock API deployment
  with YAML configuration from Cloud Storage or build-time inclusion.
---

# Cloud Run Deployment

Deploy moclojer to Google Cloud Run for serverless, autoscaling mock APIs.

## 🚀 Quick Deploy

```bash
# Build and deploy
gcloud run deploy moclojer \
  --image ghcr.io/moclojer/moclojer:latest \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --set-env-vars CONFIG=/app/moclojer.yml
```

## 📦 With Custom Configuration

### 1. Create Dockerfile

```dockerfile
FROM ghcr.io/moclojer/moclojer:latest
COPY moclojer.yml /app/moclojer.yml
```

### 2. Build and Push

```bash
docker build -t gcr.io/YOUR_PROJECT/moclojer .
docker push gcr.io/YOUR_PROJECT/moclojer
```

### 3. Deploy

```bash
gcloud run deploy moclojer \
  --image gcr.io/YOUR_PROJECT/moclojer \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --port 8000
```

## ✅ Best Practices

- Use Cloud Build for CI/CD
- Store configs in Cloud Storage
- Enable Cloud Logging
- Set appropriate memory/CPU limits

## 📚 See Also

- **[Docker Deployment](docker.md)**
- **[Kubernetes](kubernetes.md)**

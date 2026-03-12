# Docker Deployment

Docker is the easiest way to get pastely running quickly. This guide will show you how to run pastely in a Docker container.

---

## **1. Pull the Docker Image**

pastely is available on Docker Hub:

```bash
docker pull interaapps/pastely
```

---

## **2. Run pastely with Docker**

You can start pastely using a single `docker run` command:

```bash
docker run -p 8080:80 \
  --env HTTP_SERVER_PORT=80 \
  --env HTTP_SERVER_CORS="*" \
  ...
  interaapps/pastely
```

> You can find all environment variables in [Configuration](../configuration.md).

---

## **3. Verify Deployment**

1. Open your browser and navigate to `http://localhost:8080` (or your configured `SERVER_NAME`).
2. You should see the pastely home page.
3. Try creating a paste to confirm the database connection works.

---

## **4. Next Steps**

* Configure OAuth login: [Configuration](../oauth.md)
* Consider using Docker-Compose for more advanced deployments: [Docker-Compose](docker-compose.md)
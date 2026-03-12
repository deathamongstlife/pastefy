# Docker-Compose Deployment

Using Docker-Compose is a convenient way to run pastely together with its dependencies like MySQL, Redis, or MinIO in a single configuration.

---

## **1. Copy the docker-compose**

```yaml
version: '3.3'

services:
  db:
    image: mariadb:10.11
    volumes:
      - dbvol:/var/lib/mysql

    environment:
      MYSQL_ROOT_PASSWORD: pastely
      MYSQL_DATABASE: pastely
      MYSQL_USER: pastely
      MYSQL_PASSWORD: pastely

  pastely:
    depends_on:
      - db
    image: interaapps/pastely:latest
    ports:
      - "9999:80"

    environment:
      HTTP_SERVER_PORT: 80
      HTTP_SERVER_CORS: "*"
      DATABASE_DRIVER: mysql
      DATABASE_NAME: pastely
      DATABASE_USER: pastely
      DATABASE_PASSWORD: pastely
      DATABASE_HOST: db
      DATABASE_PORT: 3306
      SERVER_NAME: "http://localhost:9999"
      # There is INTERAAPPS, GOOGLE, GITHUB, DISCORD, TWITCH
      OAUTH2_PROVIDER_CLIENT_ID:
      OAUTH2_PROVIDER_CLIENT_SECRET:

volumes:
  dbvol:
```

---

## **2. Using the Provided `docker-compose.yml`**

```bash
docker-compose up -d
```

This will start pastely along with the required services (database, Redis, etc.) in detached mode.

---

## **3. Customize Environment Variables**

Before starting, you might want to configure some environment variables:

```yaml
services:
  pastely:
    image: interaapps/pastely
    ports:
      - "8080:80"
    environment:
      HTTP_SERVER_PORT: 80
      HTTP_SERVER_CORS: "*"
      ...
```

* Change `SERVER_NAME` to your domain if self-hosting publicly.
* For OAuth login, add the respective environment variables under `environment`.

---

## **4. Starting & Stopping pastely**

```bash
# Start all services
docker-compose up -d

# Stop all services
docker-compose down

# View logs
docker-compose logs -f
```

> Tip: Use `docker-compose restart pastely` to restart only the pastely service after changing environment variables.

---

## **5. Next Steps**

* Verify that pastely is running at `http://localhost:8080`.
* Configure OAuth logins if needed: [Configuration](../oauth.md)
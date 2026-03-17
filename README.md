# AREM Docker + AWS Lab

## 1) Project summary

This repository contains a Java 17 Maven project for an AREM lab that combines:

- a **custom HTTP micro-framework** (no Spring),
- **Docker local operations** (single/multi-container),
- **Docker Hub publishing scripts**, and
- **AWS EC2 deployment with Terraform**.

### Assignment requested vs implemented

| Assignment requested | Implemented in this repository |
| --- | --- |
| Build a custom lightweight Java framework (no Spring), with concurrent request handling, graceful shutdown, and a hello endpoint. | Implemented with `MiniWebFramework`, `MiniHttpServer`, `HttpRequest`, `HttpResponse`, route registration, `/hello`, and graceful shutdown via `/shutdown` and JVM shutdown hook. |
| Add Terraform for AWS EC2 + security group + variables + Docker startup in `user_data`. | Implemented under `infra/terraform/` with provider/version constraints, variables, security group, EC2 bootstrap, Docker pull, and `docker run`. |
| Dockerize app and support local operations (including compose and multi-container run). | Implemented with `Dockerfile`, `docker-compose.yml`, and scripts: `docker-build.ps1`, `docker-run-three.ps1`, `docker-verify.ps1`. |
| Add Docker Hub publish automation. | Implemented with scripts: `dockerhub-login.ps1`, `dockerhub-tag.ps1`, `dockerhub-push.ps1`. |
| Validate end-to-end where possible. | Maven build/tests and local runtime smoke test were re-validated in this environment; Docker/Terraform/AWS could not be executed here due missing CLIs/credentials (see verification section). |

---

## 2) Architecture and class design (custom framework)

Main source package: `src/main/java/edu/eci/arem`

### Class responsibilities

- `app/FrameworkApplication`
  - Application entry point.
  - Reads `PORT` and `THREAD_POOL_SIZE` from environment.
  - Registers routes (`/hello`, `/shutdown`).
  - Installs JVM shutdown hook.
  - Starts the HTTP server.

- `framework/MiniWebFramework`
  - Route registry (`GET` only).
  - Uses `ConcurrentHashMap<String, RouteHandler>` for thread-safe route access.
  - Resolves route by path and delegates to handler.
  - Returns framework-level responses for `405`, `404`, and `500`.

- `framework/RouteHandler`
  - Functional interface (`HttpResponse handle(HttpRequest request)`).
  - Allows lambda-based route definitions.

- `framework/MiniHttpServer`
  - Low-level socket server (`ServerSocket`).
  - Accept loop + fixed worker pool (`Executors.newFixedThreadPool`).
  - Parses incoming request, calls framework, writes HTTP response bytes.
  - Provides `stopGracefully()`.

- `framework/http/HttpRequest`
  - Parses request line and query string.
  - Normalizes path and exposes query params.

- `framework/http/HttpResponse`
  - Builds plain-text HTTP responses.
  - Serializes status line, headers, and body to bytes.

### Concurrency model

- **Acceptor thread**: `MiniHttpServer.start()` accepts TCP connections.
- **Worker pool**: each accepted client socket is submitted to a fixed-size executor.
- **Thread-safe routing**: route storage uses `ConcurrentHashMap`.
- **Configurable workers**: `THREAD_POOL_SIZE` environment variable (defaults to `max(2, availableProcessors)` in `FrameworkApplication`).

### Graceful shutdown behavior

- `/shutdown` route starts a separate thread that calls `server.stopGracefully()`.
  - This avoids blocking request handling while still returning an HTTP response.
- JVM shutdown hook also calls `stopGracefully()`.
- `stopGracefully()`:
  1. atomically flips running flag,
  2. closes `ServerSocket` to unblock `accept()`,
  3. shuts down executor,
  4. waits up to 30 seconds, then forces shutdown if needed.

---

## 3) Local build and run (without Docker)

From project root:

```powershell
cd C:\Users\angel\Desktop\arem-docker-aws-lab
mvn --no-transfer-progress clean package
```

Run app:

```powershell
$env:PORT = "6000"
$env:THREAD_POOL_SIZE = "4"
java -jar .\target\arem-docker-aws-lab-1.0-SNAPSHOT.jar
```

Quick checks (from another terminal):

```powershell
Invoke-WebRequest "http://localhost:6000/hello?name=AREM" -UseBasicParsing
Invoke-WebRequest "http://localhost:6000/shutdown" -UseBasicParsing
```

---

## 4) Docker build/run and docker-compose usage

### Build image

```powershell
.\scripts\docker-build.ps1
```

Default image tag: `arem-docker-aws-lab:local`

Equivalent manual command:

```powershell
docker build --build-arg APP_PORT=6000 -t arem-docker-aws-lab:local .
```

### Run 3 app containers locally (different host ports)

```powershell
.\scripts\docker-run-three.ps1
```

Defaults:
- container port: `6000`
- host ports: `6101`, `6102`, `6103`

Verify:

```powershell
.\scripts\docker-verify.ps1
```

### docker-compose (app + mongo)

```powershell
docker compose up --build -d
docker compose ps
docker compose logs app
docker compose down -v
```

Note: `docker-compose.yml` starts both `app` and `mongo`. The current Java app is stateless and does not include Mongo client integration.

---

## 5) Docker Hub publish flow (scripts)

From project root:

1. Build local image:
   ```powershell
   .\scripts\docker-build.ps1
   ```
2. (Optional) set repository once:
   ```powershell
   $env:DOCKERHUB_REPOSITORY = "youruser/arem-docker-aws-lab"
   ```
3. Interactive login:
   ```powershell
   .\scripts\dockerhub-login.ps1 -Username "youruser"
   ```
4. Tag image:
   ```powershell
   .\scripts\dockerhub-tag.ps1 -SourceImage "arem-docker-aws-lab:local" -Repository "youruser/arem-docker-aws-lab" -Tag "latest"
   ```
5. Push image:
   ```powershell
   .\scripts\dockerhub-push.ps1 -SourceImage "arem-docker-aws-lab:local" -Repository "youruser/arem-docker-aws-lab" -Tag "latest"
   ```

Shortcut (tag + optional login + push):

```powershell
.\scripts\dockerhub-push.ps1 -Repository "youruser/arem-docker-aws-lab" -Tag "latest" -LoginFirst
```

---

## 6) AWS Terraform deployment (EC2 + Docker)

Terraform files are in `infra\terraform`.

### Prerequisites

- Terraform CLI (`>= 1.5.0`)
- AWS credentials configured in your shell/profile
- Existing EC2 key pair in target region
- Docker image already pushed to Docker Hub

### Configure variables

```powershell
cd .\infra\terraform
Copy-Item terraform.tfvars.example terraform.tfvars
```

Edit `terraform.tfvars`:

- `aws_region`
- `instance_type`
- `key_pair_name`
- `allowed_cidrs`
- `host_port`
- `container_port`
- `docker_image_name`
- `docker_image_tag`
- `instance_name`

Important for this app image: use `container_port = 6000` unless you rebuild the image with a different internal app port.

### Deploy

```powershell
terraform init
terraform fmt -recursive
terraform validate
terraform plan
terraform apply
```

Useful outputs:
- `instance_public_ip`
- `instance_public_dns`
- `service_url`

Cleanup:

```powershell
terraform destroy
```

---

## 7) Verification / evidence (strictly from this environment)

Environment: `C:\Users\angel\Desktop\arem-docker-aws-lab`

### Verified here

1. **Maven tests passed**
   - Command: `mvn --no-transfer-progress test`
   - Result: `Tests run: 3, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS`.

2. **Packaging succeeded**
   - Command: `mvn --no-transfer-progress clean package`
   - Result: `BUILD SUCCESS`.
   - Artifact confirmed: `target\arem-docker-aws-lab-1.0-SNAPSHOT.jar`.
   - Dependency directory confirmed: `target\dependency\`.

3. **Runtime smoke test succeeded (non-Docker)**
   - Launched jar with `PORT=6000`, `THREAD_POOL_SIZE=4`.
   - `GET /hello?name=README` → `200`, body: `Hello, README!`
   - `GET /shutdown` → `200`, body: `Server is shutting down.`
   - Process exited after shutdown (`process_exited=true`).

4. **Key file presence validated**
   - `Dockerfile`, `docker-compose.yml`, Docker scripts, and Terraform files were confirmed to exist.

### Not verifiable here (missing tools/credentials)

- Docker commands could not be executed here (`docker_not_found`).
  - Therefore image build/run, multi-container execution, compose runtime, and Docker Hub push were **not** re-verified in this session.
- Terraform commands could not be executed here (`terraform_not_found`).
  - Therefore `terraform plan/apply/destroy` and EC2 provisioning were **not** re-verified in this session.
- AWS runtime access and public endpoint checks require AWS credentials/account resources not available in this session.

---

## 8) Deliverables checklist

- [x] Repository with source code, Docker artifacts, and Terraform IaC.
- [x] Top-level `README.md` with architecture, operations, deployment, and evidence sections.
- [ ] Demo video (to be recorded/exported by student).

### Suggested video guidance steps

1. Show repository structure and explain framework classes.
2. Run `mvn clean package` and show passing tests.
3. Run jar locally and call `/hello`; then call `/shutdown`.
4. Build Docker image and run validation scripts (`docker-build`, `docker-run-three`, `docker-verify`).
5. Show Docker Hub login/tag/push flow using scripts.
6. Show Terraform variable setup, `plan`, `apply`, and resulting `service_url`.
7. Show cleanup (`docker compose down -v`, `terraform destroy`).

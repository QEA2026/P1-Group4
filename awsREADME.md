## AWS EC2 Project Access & Startup

### 1. Generate an SSH Key Pair

Each teammate should create their **own SSH key pair** on their computer.

> If you already have an SSH key pair you want to use, you can skip this step and send your existing `.pub` key to the EC2 administrator.

#### Windows PowerShell

Generate a key:

```powershell
ssh-keygen -t ed25519
```

Press **Enter** through the prompts to use the default location.

Display your public key:

```powershell
type $env:USERPROFILE\.ssh\id_ed25519.pub
```

#### macOS / Linux

Generate a key:

```bash
ssh-keygen -t ed25519
```

Press **Enter** through the prompts to use the default location.

Display your public key:

```bash
cat ~/.ssh/id_ed25519.pub
```

This creates:

```text
id_ed25519       # Private key — KEEP THIS PRIVATE
id_ed25519.pub   # Public key — send this to the EC2 administrator
```

Send the **entire contents of `id_ed25519.pub`** to the person managing the EC2 instance.

> **Never share your private `id_ed25519` key.**

---

### 2. SSH Access to the EC2 Instance

After the EC2 administrator adds your public key to the instance, connect using the command for your operating system.

#### Windows PowerShell

```powershell
ssh -i "$env:USERPROFILE\.ssh\id_ed25519" ec2-user@YOUR_EC2_PUBLIC_IP
```

#### macOS / Linux

```bash
ssh -i ~/.ssh/id_ed25519 ec2-user@YOUR_EC2_PUBLIC_IP
```

Replace `YOUR_EC2_PUBLIC_IP` with the current public IPv4 address of the EC2 instance.

Example:

```bash
ssh -i ~/.ssh/id_ed25519 ec2-user@18.219.141.185
```

> **Note:** The EC2 public IP may change when the instance is stopped and started unless an Elastic IP has been assigned. Always verify the current public IP in AWS.

---

### 3. Navigate to the Project

After connecting to EC2:

```bash
cd ~/P1-Group4
```

Verify the project files:

```bash
ls
```

You should see files/directories such as:

```text
README.md
docker-compose.yml
employee-app
manager-app
database
```

---

### 4. Start the Application

Start the Docker containers:

```bash
docker compose up -d
```

If the application code has changed and the Docker images need to be rebuilt:

```bash
docker compose up -d --build
```

The application uses:

* **Employee App:** Port `5001`
* **Manager API:** Port `8080`
* **PostgreSQL:** AWS RDS

The Docker PostgreSQL container is used for the local/containerized demonstration environment. The EC2 deployment connects the applications to the AWS RDS PostgreSQL database.

---

### 5. Verify the Containers

Run:

```bash
docker compose ps
```

You should see:

```text
employee-app
manager-app
expense-postgres
```

All containers should show a status of `Up`, with the PostgreSQL container showing `healthy`.

---

### 6. View Application Logs

Employee application:

```bash
docker compose logs --tail=50 employee-app
```

Manager application:

```bash
docker compose logs --tail=50 manager-app
```

Follow logs in real time:

```bash
docker compose logs -f employee-app
```

or:

```bash
docker compose logs -f manager-app
```

Press `Ctrl+C` to stop following the logs.

---

### 7. Stop the Containers

When finished working, stop and remove the containers:

```bash
docker compose down
```

This removes the application containers and Docker network but **does not delete the Docker images or the AWS RDS database**.

The containers can be recreated later with:

```bash
docker compose up -d
```

---

### 8. Git Workflow

Before making changes:

```bash
git pull origin main
```

After making changes:

```bash
git status
git add .
git commit -m "Describe your changes"
git push origin main
```

> Make sure `.env` files and private credentials are never committed to GitHub.

---

### Quick Startup

For teammates who have already completed SSH setup:

#### Windows PowerShell

```powershell
ssh -i "$env:USERPROFILE\.ssh\id_ed25519" ec2-user@YOUR_EC2_PUBLIC_IP
```

#### macOS / Linux

```bash
ssh -i ~/.ssh/id_ed25519 ec2-user@YOUR_EC2_PUBLIC_IP
```

Then, on the EC2 instance:

```bash
cd ~/P1-Group4
docker compose up -d
docker compose ps
```

The project should now be running on the EC2 instance.

# Run Jenkins Pipeline on AWS

## 1. Connect to the EC2 Instance

Open PowerShell:

```powershell
ssh -i "$env:USERPROFILE\.ssh\id_ed25519" ec2-user@<EC2-PUBLIC-IP>
```

## 2. Check the Containers

```bash
cd ~/P1-Group4
docker compose ps
```

If the containers are not running:

```bash
docker compose up -d
```

## 3. Check Jenkins

```bash
sudo systemctl status jenkins --no-pager
```

Jenkins should show:

```text
Active: active (running)
```

## 4. Get the Jenkins Password

On the EC2 instance, run:

```bash
sudo cat /var/lib/jenkins/secrets/initialAdminPassword
```

Copy the password that is displayed.

> If Jenkins has already been fully configured and your own Jenkins account was created, use your Jenkins username and password instead. The `initialAdminPassword` is mainly for the initial Jenkins setup/unlock screen.

## 5. Connect to Jenkins

Open a **second PowerShell window on your local computer**:

```powershell
ssh -L 9090:localhost:8081 -i "$env:USERPROFILE\.ssh\id_ed25519" ec2-user@<EC2-PUBLIC-IP>
```

Keep this PowerShell window open.

## 6. Open Jenkins

Open:

```text
http://localhost:9090
```

Enter your Jenkins login/password.

## 7. Run the Pipeline

Open:

```text
P1-Group4-Pipeline-AWS
```

Click:

```text
Build Now
```

Then open **Console Output** to watch the pipeline.

A successful run ends with:

```text
Finished: SUCCESS
```

> **Note:** Do not manually start the frontend on port `5500`. Jenkins starts it for the tests and automatically stops it afterward.

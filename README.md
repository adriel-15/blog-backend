# blog backend

short description of the project...

## 🚀 mysql database initialization & update

This project includes automation scripts to **initialize** or **update** the MySQL database used by the application. 
You **do not need to run `docker-compose` manually**.
Just follow the instructions below based on your operating system.

### 🧩 What the scripts do

- Check if the MySQL Docker container is running or exists.
- Remove the existing container and volume if necessary (clean start).
- Start the MySQL container using `docker-compose`.

### 🧩 when to run the script

- before running the project for the first time.
- whenever you pull new changes that include database modifications

---

## 🐧 linux/macOS users

Use the provided **bash script**:

```bash
./start-mysql.sh
```

### **Requirements:**

- Bash shell
- Docker and Docker Compose installed and running

---

## 🖥️ windows users

Use the provided **PowerShell script**:

```powershell
.\start-mysql.ps1
```

### **Requirements:**

- PowerShell (v5.1+ recommended)
- Docker Desktop installed and running

---

## 📝 notes

- These scripts automatically manage the lifecycle of the MySQL container defined in your `docker-compose.yml`
- They assume:
    - The container is named `mysql-container`
    - The MySQL root password is `test123`
    - Port `3307` is used internally (adjust if needed)



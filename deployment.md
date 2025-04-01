# Deployment Guide

This document describes how to deploy the Java Spring Boot application from scratch to a production-ready environment. It covers platform prerequisites, installation steps, service configuration, and optional continuous deployment setup.

## 1. Overview

The application:
- Is a Java Spring Boot web application.
- Uses PostgreSQL as its database.
- Is served behind an Nginx reverse proxy.
- Can be continuously deployed via GitHub Actions (or deployed manually).

Below is a step-by-step guide for installing and configuring all required components on a Linux VM.

---

## 2. Prerequisites

1. **Linux VM**  
   - Any modern Linux distribution (e.g., Ubuntu 20.04/22.04, CentOS/RHEL 8 or later).  
   - Root or sudo-level access to install packages and configure services.

2. **Required Packages**  
   - **Java 21**: OpenJDK 21 (or a compatible JDK).  
   - **PostgreSQL 14**: Database server.  
   - **Nginx**: Web server to act as a reverse proxy.  
   - **Systemd**: For managing the Spring Boot service (commonly available by default on most modern Linux distributions).

3. **Network Access**  
   - The server should have access to the internet for installing packages.
   - Port **80** (HTTP) open to serve traffic via Nginx.
   - Port **22** open for SSH (needed by GitHub Actions if you choose to use CI/CD).

---

## 3. Step-by-Step Deployment

### 3.1 Install Java 21

Depending on your Linux distribution, you can install OpenJDK 21 as follows. (Below is an example for Ubuntu-based systems.)

```bash
sudo apt update
sudo apt install openjdk-21-jdk -y
```

Verify the installation:

```bash
java --version
```

It should print the version number 21.

### 3.2 Install and Configure PostgreSQL 14

1. **Install PostgreSQL 14**  
For Ubuntu-based systems:

```bash
sudo apt install postgresql-14 -y
```

2. **Set up the PostgreSQL superuser**
PostgreSQL installs a default superuser named postgres. We need to ensure it has the password password (for demonstration only; use a strong password in production).

```bash
# Switch to postgres user
sudo -i -u postgres

# Open the PostgreSQL prompt
psql

# Change the password
ALTER USER postgres WITH PASSWORD 'password';

# Exit the PostgreSQL prompt
\q

# Return to your normal user
exit
```

3. **Create the database**

```bash
sudo -i -u postgres psql
CREATE DATABASE abroad;
\q
exit
```

### 3.3 Install Nginx

Install and start Nginx

```bash
sudo apt install nginx -y
sudo systemctl enable nginx
sudo systemctl start nginx
```

### 3.4 Configure Nginx as a Reverse Proxy

We will forward all HTTP traffic from the public IP on port 80 to our internal Spring Boot application on port 8080.

1. **Edit the default Nginx site (or create a new config):** 

```bash
sudo nano /etc/nginx/sites-available/default
```

2. **Update the Server block**

```bash
server {
    listen 80 default_server;
    listen [::]:80 default_server;

    server_name _;  # or use your domain name if you have one

    location / {
        proxy_pass http://127.0.0.1:8080/;
        proxy_http_version 1.1;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header Host $host;
    }
}
```

3. **Test and reload the Nginx configuration**

```bash
sudo nginx -t
sudo systemctl reload nginx
```

#### 3.5 Create a Systemd Service for the Application

To run the application in the background and have it automatically start on boot, create a systemd service file:

1. **Create the service file**

```bash
sudo nano /etc/systemd/system/458abroad.service
```

2. **Use the following configuration**

```properties
[Unit]
Description=ECE 458 Abroad Application Beta Service
After=syslog.target

[Service]
User=root
WorkingDirectory=/opt/458abroad
EnvironmentFile=/opt/458abroad/service.conf
ExecStart=/usr/bin/java -jar /opt/458abroad/abroad-0.0.1-SNAPSHOT.jar \
  --fillDb=${FILL_DB} --resetDb=${RESET_DB} \
  --spring.profiles.active=beta \
  --spring.config.additional-location=file:application.properties \
  --server.port=8081
SuccessExitStatus=143
Restart=on-failure                                                                                                                                          
[Install]
WantedBy=multi-user.target
```

Create a configuration file for the service in the main application directory:

```properties
FILL_DB=true
RESET_DB=true
```

3. **Enable and start the service**

```bash
sudo systemctl daemon-reload
sudo systemctl enable abroad
sudo systemctl start abroad
```

4. **Check Status**

```bash
sudo systemctl status abroad
```

## 3.6 Configure Shibboleth for SSO Authentication

The following steps describe how to install and configure the Shibboleth Service Provider (SP) to handle SSO authentication.

### 3.6.1 Install Shibboleth SP

For Ubuntu-based systems, install the Shibboleth SP packages:

```bash
sudo apt update
sudo apt install shibboleth-sp2-common shibboleth-sp2-utils -y
```

### 3.6.2 Configure Shibboleth SP

Edit the main Shibboleth configuration file (typically located at /etc/shibboleth/shibboleth2.xml) to include your Identity Provider (IdP) settings. For example:

```xml
<SSO entityID="https://your-idp.example.com/idp/shibboleth">
    SAML2 SAML1
</SSO>
```

Ensure that your IdP metadata, certificate, and key paths are correctly configured in this file.

### 3.6.3 Set Up an Apache Virtual Host on Port 8080

Create an Apache virtual host configuration to handle SSO-related traffic. For example, create a new file at /etc/apache2/sites-available/shibboleth.conf with the following content:

```xml
<VirtualHost *:8080>
    ServerName sso.yourdomain.com

    # DocumentRoot can be a placeholder if Apache is used solely for routing SSO requests
    DocumentRoot /var/www/html

    # Enable the Shibboleth handler for SSO endpoints
    <Location /Shibboleth.sso>
        SetHandler shib
    </Location>

    # Optional: Additional logging for troubleshooting
    ErrorLog ${APACHE_LOG_DIR}/sso_error.log
    CustomLog ${APACHE_LOG_DIR}/sso_access.log combined
</VirtualHost>
```

Enable the site and reload Apache:

```bash
sudo a2ensite shibboleth
sudo systemctl reload apache2
```

### 3.6.4 Reroute SSO Authentication Traffic

Update your network or reverse proxy configuration (e.g., in Nginx) to forward SSO-related traffic to the Apache server on port 8080. For example, add the following location block to your Nginx configuration:

```nginx
location /sso/ {
    proxy_pass http://127.0.0.1:8080/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
}
```

This ensures that any requests intended for SSO (e.g., paths starting with /sso/) are rerouted to the Apache virtual host handling Shibboleth.
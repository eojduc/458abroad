# Backup Admin Guide

This document describes how to configure and manage backups for the Java Spring Boot application. It covers the backup solution, configuration steps, and testing procedures.

## 1. Overview

The backup solution:
- Uses `pg_dump` to create a SQL dump of the PostgreSQL database.
- Stores the backup files on a remote server using `scp`.
- Scheduled to run daily via a cron job.
- Sends Slack notifications on backup progress and success/failure.

---

## 2. Prerequisites

1. **Remote Server**  
   - A Linux server with SSH access enabled.
   - Sufficient disk space to store the backups.

2. **Required Packages**  
   - **PostgreSQL 14**: `pg_dump` utility.

3. **Network Access**
   - Port **22** open for SSH access to the remote server.

---

## 3. Backup Configuration

### 3.1 Create a Slack App and Webhook

To receive notifications about the backup status, create a Slack app and configure a webhook URL.

1. **Create a Slack App**  
   - Go to the [Slack API](https://api.slack.com/apps) page and create a new app.
   - Note down the **Webhook URL** from the app settings.

### 3.2 Reserve Server for Backup Storage

Create a directory on the remote server to store the backup files 
with appropriate permissions for the backup user.

1. **Create Backup Directory**  
   - Connect to the remote server via SSH.
   - Create directories for storing backups:

   ```bash
   sudo mkdir -p /backup/postgres
   sudo mkdir -p /backup/postgres/daily
   sudo mkdir -p /backup/postgres/weekly
   sudo mkdir -p /backup/postgres/monthly
   sudo chown -R backupuser:backupuser /backup
   sudo chmod -R 700 /backup
   ```


### 3.3 Configure PostgreSQL Backup Script

Create a shell script to run the `pg_dump` command on the main database and store the backup file
with staggered retention on the remote server.

1. Create a new shell script `pg_backup.sh` in the correct directory:

```bash
sudo mkdir -p /opt/458abroad/backup
sudo vim /opt/458abroad/backup/pg_backup.sh
```

2. Add the following content to the script:

```bash
#!/bin/bash
DB_NAME="abroad" # Your database name
DB_USER="postgres" # Head database user
DB_PASSWORD="password" # Password for the database user
ENV_NAME="prod" # Your server name
REMOTE_BACKUP_HOST="your.hostname.net" # Remote hostname
REMOTE_BACKUP_USER="backupuser" # Backup user
REMOTE_BACKUP_DIR="/backup/postgres" # Remote backup directory
LOG_FILE="/home/vcm/logs/458abroad_backup.log" # Local Log file
TEMP_DIR="/home/vcm/pg_backup_temp" # Local temporary directory
SLACK_WEBHOOK_URL="https://hooks.slack.com/services/YOUR/WEBHOOK/URL" # Slack webhook URL

# Create log directory if it doesn't exist
mkdir -p $(dirname $LOG_FILE)

# Get current date information
DOW=$(date +%u)    # Day of week (1-7)
DOM=$(date +%d)    # Day of month (01-31)
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

# Function to send alert messages to Slack
send_alert() {
    local subject="$1"
    local message="$2"
    # Send the alert to Slack using a webhook
    curl -s -X POST -H 'Content-type: application/json' \
        --data "{\"text\":\"($ENV_NAME) $subject: $message\"}" \
    $SLACK_WEBHOOK_URL > /dev/null
}

# Log function
log() {
    echo "$(date '+%Y-%m-%d %H:%M:%S') - LOG: $1" >> $LOG_FILE
    echo "$(date '+%Y-%m-%d %H:%M:%S') - $1"
}

# Create temporary directory
mkdir -p $TEMP_DIR
log "Starting PostgreSQL backup process"

# Create temporary backup file
TEMP_BACKUP_FILE="$TEMP_DIR/${DB_NAME}_${TIMESTAMP}.sql.gz"
log "Creating backup: $TEMP_BACKUP_FILE"

# Ensure remote directories exist
ssh $REMOTE_BACKUP_USER@$REMOTE_BACKUP_HOST "mkdir -p $REMOTE_BACKUP_DIR/daily $REMOTE_BACKUP_DIR/weekly $REMOTE_BACKUP_DIR/monthly"

# Backup command
if PGPASSWORD=$DB_PASSWORD pg_dump -U $DB_USER $DB_NAME | gzip > $TEMP_BACKUP_FILE; then
    # Verify backup is not empty
    if [ -s "$TEMP_BACKUP_FILE" ]; then
        log "Database backup completed successfully"
        send_alert "Backup Successful" "The backup for $DB_NAME was completed successfully."
    else
        log "ERROR: Database backup file is empty"
        send_alert "Backup Failed" "The backup for $DB_NAME created an empty file. Please check permissions."
        rm -f $TEMP_BACKUP_FILE
        exit 1
    fi
else
    log "Database backup failed"
    send_alert "Backup Failed" "The backup for $DB_NAME failed. Please check the logs for details."
    rm -f $TEMP_BACKUP_FILE
    exit 1
fi

# Transfer the backup to remote server - daily backup
log "Transferring daily backup to remote server"
if scp $TEMP_BACKUP_FILE $REMOTE_BACKUP_USER@$REMOTE_BACKUP_HOST:$REMOTE_BACKUP_DIR/daily/; then
    log "Daily backup transfer successful"
    send_alert "Daily Backup Transfer Successful" "The daily backup for $DB_NAME was transferred successfully to the remote server."
    
    # Create weekly backup (on Sundays, DOW=7)
    if [ "$DOW" -eq 7 ]; then
        log "Creating weekly backup"
        WEEKLY_FILE="${DB_NAME}_week$(date +%V)_${TIMESTAMP}.sql.gz"
        if ssh $REMOTE_BACKUP_USER@$REMOTE_BACKUP_HOST "cp $REMOTE_BACKUP_DIR/daily/$(basename $TEMP_BACKUP_FILE) $REMOTE_BACKUP_DIR/weekly/$WEEKLY_FILE"; then
            log "Weekly backup created successfully"
            send_alert "Weekly Backup Created" "The weekly backup for $DB_NAME was created successfully on the remote server."
        else
            log "ERROR: Failed to create weekly backup"
            send_alert "Weekly Backup Failed" "Failed to create weekly backup on remote server."
        fi
    fi
    
    # Create monthly backup (on 1st of month)
    if [ "$DOM" -eq "01" ]; then
        log "Creating monthly backup"
        MONTHLY_FILE="${DB_NAME}_$(date +%Y%m)_${TIMESTAMP}.sql.gz"
        if ssh $REMOTE_BACKUP_USER@$REMOTE_BACKUP_HOST "cp $REMOTE_BACKUP_DIR/daily/$(basename $TEMP_BACKUP_FILE) $REMOTE_BACKUP_DIR/monthly/$MONTHLY_FILE"; then
            log "Monthly backup created successfully"
            send_alert "Monthly Backup Created" "The monthly backup for $DB_NAME was created successfully on the remote server."
        else
            log "ERROR: Failed to create monthly backup"
            send_alert "Monthly Backup Failed" "Failed to create monthly backup on remote server."
        fi
    fi
else
    log "ERROR: Backup transfer failed"
    send_alert "Backup Transfer Failed" "The backup transfer failed. Please check the logs."
    rm -f $TEMP_BACKUP_FILE
    exit 1
fi

# Clean up old backups using find (more reliable than ls)
log "Cleaning up old backups"
ssh $REMOTE_BACKUP_USER@$REMOTE_BACKUP_HOST "find $REMOTE_BACKUP_DIR/daily -type f -mtime +7 -delete"
ssh $REMOTE_BACKUP_USER@$REMOTE_BACKUP_HOST "find $REMOTE_BACKUP_DIR/weekly -type f -mtime +28 -delete"
ssh $REMOTE_BACKUP_USER@$REMOTE_BACKUP_HOST "find $REMOTE_BACKUP_DIR/monthly -type f -mtime +365 -delete"

# Clean up local temp files
rm -f $TEMP_BACKUP_FILE
rmdir --ignore-fail-on-non-empty $TEMP_DIR

log "Backup process completed successfully"
send_alert "Backup Process Completed" "The backup process for $DB_NAME has completed successfully."
```

3. Make the script executable:

```bash
sudo chmod +x /opt/458abroad/backup/pg_backup.sh
```

4. Save backupuser SSH key for the remote server:

This lets the backup script connect to the remote server without a password. 
You will be prompted for the password of the backupuser on the remote server when running this command.

```bash
ssh-copy-id backupuser@your.hostname.net
```

5. (Optional) Test the backup script manually:

To manually run the backup script and check if it works as expected, execute the script:

```bash
/opt/458abroad/backup/pg_backup.sh
```

6. Schedule the backup script to run daily:

Add a cron job to run the backup script daily at a specific time.

```bash
crontab -e
```

To run the backup script every day at 2 AM, add the following line to the crontab file:

```bash
0 2 * * * /opt/458abroad/backup/pg_backup.sh >> /home/vcm/logs/458abroad_backup.log 2>&1
```

Make sure the Cron daemon is running:

```bash
sudo systemctl status cron
```

It should show as active (running).

---

## 4. Restore and Test from Backup

To restore the database from a backup file, follow these steps:

1. **Download the Backup File**  
   - Connect to the remote server as the backup user and download/copy the backup file to the server where the database needs to be restored.
   - Create a `restores` directory in the backup directory:
   - Use `scp` to copy the file:

   ```bash
   scp abroad_backup_file.sql.gz user@destination_server:/tmp/
   sudo mv /tmp/abroad_backup_file.sql.gz /opt/458abroad/backup/restores/
   ```
   
2. **Stop the application server**

Stop the application server, so you can restore the database without any active connections.

```bash
sudo systemctl stop 458abroad.service
```

3. **Restore the Database**  
   - Run the following command to restore the database from the backup file:

   ```bash
   sudo -i -u postgres
   psql -c "DROP DATABASE IF EXISTS abroad;"
   psql -c "CREATE DATABASE abroad;"
   gunzip -c /opt/458abroad/backup/restores/abroad_backup_file.sql.gz | psql -d abroad
   ```
   
4. **(Optional) Test Database Content**  
   - Connect to the database and check if the data has been restored correctly:

   ```bash
   psql -d abroad
   \dt      # List tables   
   SELECT * FROM table_name;  # View table content
   \q
   ```
   
5. **Restart the Application Server**

After restoring the database, restart the application server:

First, change the service configuration to disable the `RESET_DB` and `FILL_DB` flags:
```bash
sudo vim /opt/458abroad/service.conf
```
```properties
RESET_DB=false
FILL_DB=false
```

Then, restart the service:
```bash
sudo systemctl start 458abroad.service
```

Restore the original service configuration after the restore is complete.
```properties
RESET_DB=false
FILL_DB=false
```


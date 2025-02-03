#!/bin/bash

DB_NAME="abroad"
USER="postgres"  # Replace with your PostgreSQL username
PASSWORD="password"  # Replace with your PostgreSQL password

# Drop and recreate database
PGPASSWORD=$PASSWORD psql -U $USER -h localhost -c "DROP DATABASE IF EXISTS $DB_NAME;"
PGPASSWORD=$PASSWORD psql -U $USER -h localhost -c "CREATE DATABASE $DB_NAME;"

echo "Database reset completed!"
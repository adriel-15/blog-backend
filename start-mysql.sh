#!/bin/bash

# Define container name from your compose file
CONTAINER_NAME="mysql-container"

echo "Checking MySQL container status..."

# Check if container exists and is running
if [ "$(docker ps -q -f name=$CONTAINER_NAME)" ]; then
    echo "MySQL container is running - performing update..."
    docker-compose down -v
elif [ "$(docker ps -aq -f name=$CONTAINER_NAME)" ]; then
    echo "MySQL container exists but is stopped - removing..."
    docker-compose down -v
fi

# Start with clean slate
echo "Starting MySQL with latest schema..."
docker-compose up -d


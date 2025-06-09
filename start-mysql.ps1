# Define container name from your compose file
$CONTAINER_NAME = "mysql-container"

Write-Host "Checking MySQL container status..."

# Check container status
if (docker ps -q -f name=$CONTAINER_NAME) {
    Write-Host "MySQL container is running - performing update..."
    docker-compose down -v
} elseif (docker ps -aq -f name=$CONTAINER_NAME) {
    Write-Host "MySQL container exists but is stopped - removing..."
    docker-compose down -v
}

# Start with clean slate
Write-Host "Starting MySQL with latest schema..."
docker-compose up -d


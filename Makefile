goal: dependency
	echo "In makefile"
dependency:
	echo "Get ready"
test: test-backend test-frontend
	echo "Test done"
test-backend: start-docker-compose
	cd backend && AWS_DYNAMODB_URL="http://localhost:4566" AWS_DYNAMODB_TABLE_NAME="test" ./gradlew test
test-frontend:
	cd frontend && npm run test
start-docker-compose:
	docker compose up -d






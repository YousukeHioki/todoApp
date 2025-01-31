goal: dependency
	echo "In makefile"
dependency:
	echo "Get ready"
build: test copy-bundle
	echo "Build succeeded"
test: test-backend test-frontend
	echo "Test done"
test-backend: start-docker-compose
	cd backend && npm install && AWS_DYNAMODB_URL="http://localhost:4566" AWS_DYNAMODB_TABLE_NAME="test" ./gradlew test
test-frontend:
	cd frontend && npm install && npm run build && npm run test
copy-bundle: delete-and-make
	cp -rf frontend/dist/* backend/src/main/resources/static
delete-and-make
	rm -rf backend/src/main/resources/static && mkdir backend/src/main/resources/static
start-docker-compose:
	docker compose up -d






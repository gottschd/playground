# Notes
cd backend
- mvn package
- docker load -i target\backend-0.0.1-SNAPSHOT.tar

cd ..\webapp
- mvn package
- docker load -i target\webapp-0.0.1-SNAPSHOT.tar

cd ..
docker-compose up
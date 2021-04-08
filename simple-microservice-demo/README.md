# Notes docker compose (windows)
cd backend
- mvn package
- docker load -i target\backend-0.0.1-SNAPSHOT.tar

cd ..\webapp
- mvn package
- docker load -i target\webapp-0.0.1-SNAPSHOT.tar

cd ..
docker-compose up

# Notes K8s for Kind
- create kind cluster named dev
- kind load image-archive target\backend-0.0.1-SNAPSHOT.tar --name dev
- kind load image-archive target\webapp-0.0.1-SNAPSHOT.tar --name dev
- docker exec -it dev-control-plane crictl images // show images in kind cluster
- kubectl -f k8s.yml apply
- curl http://localhost/webapp
- (or) kubectl port-forward deployments/backend-deployment 8081:8081 -n default
- (or) kubectl port-forward deployments/webapp-deployment 8080:8080 -n default
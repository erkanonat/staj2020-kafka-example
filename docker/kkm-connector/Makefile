VERSION=1.0.4
PROJECT=egys
APP=kkm-connector
HVL_REPO=dockerepo.dc01.havelsan.io/botas

build:
	cp ../../target/${APP}-${VERSION}.jar ./${APP}.jar
	docker build -f Dockerfile --pull -t ${PROJECT}/${APP}:${VERSION} -t ${PROJECT}/${APP}:latest -t ${HVL_REPO}/${PROJECT}/${APP}:${VERSION} -t ${HVL_REPO}/${PROJECT}/${APP}:latest .
	rm ./${APP}.jar

push: build
	docker push ${HVL_REPO}/${PROJECT}/${APP}:${VERSION}
	docker push ${HVL_REPO}/${PROJECT}/${APP}:latest

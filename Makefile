.PHONY: test build

test:
	@mvn test --file pom.xml

build:
	@mvn package

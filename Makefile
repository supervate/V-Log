.PHONY: test build

test:
	@mvn test

build: test
	@mvn package

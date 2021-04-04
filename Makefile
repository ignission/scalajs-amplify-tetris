.DEFAULT_GOAL := help

.PHONY: test
test: ## Format and build
	sbt 'fixAll; test'

check: ## Check sources
	sbt 'checkAll; compile; test'

dev: ## webpack watch and sbt rebuilding on source changes
	sbt '~dev'

dev-start: ## Start scala build and webpack watch
	sbt 'devStart'

dev-stop: ## Stop watch
	sbt 'devStop'

.PHONY: dist
dist: ## Dist
	sbt 'dist'

.PHONY: help
help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

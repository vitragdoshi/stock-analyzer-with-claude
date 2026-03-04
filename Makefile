.PHONY: help dev build run clean deps-update deps-audit

help:
	@echo "Usage: make <target>"
	@echo ""
	@echo "Development"
	@echo "  dev            Start backend + frontend in dev mode (separate processes)"
	@echo "  build-frontend Build the React app into backend/target/classes/static"
	@echo "  run            Run bundled Spring Boot app (frontend must be built first)"
	@echo ""
	@echo "Dependencies"
	@echo "  deps-update    Update frontend deps within semver ranges and re-lock"
	@echo "  deps-audit     Check for known vulnerabilities and auto-fix where safe"
	@echo ""
	@echo "Docker"
	@echo "  docker-build   Build the single bundled Docker image"
	@echo "  docker-run     Run the bundled image on port 8080"
	@echo ""
	@echo "Misc"
	@echo "  clean          Remove build artefacts"

# ── Development ──────────────────────────────────────────────────────────────

dev:
	@echo "Starting backend on :8080 and frontend dev server on :3000 ..."
	cd backend && mvn spring-boot:run -DskipFrontend=true &
	cd frontend && npm start

build-frontend:
	cd frontend && npm ci && npm run build
	@echo "Frontend built. Copy to backend static dir if needed:"
	@echo "  mvn -f backend/pom.xml process-resources -DskipFrontend=true"

run:
	cd backend && mvn spring-boot:run -DskipFrontend=true

# ── Dependencies ─────────────────────────────────────────────────────────────

# Updates all frontend packages to the latest versions allowed by the semver
# ranges in package.json (e.g. ^18.2.0 → 18.x.y latest), then writes the new
# exact versions back into package-lock.json.
#
# Run this periodically (e.g. monthly) or when a security advisory is raised.
# After running, review the diff with `git diff frontend/package-lock.json`
# and re-test before committing.
deps-update:
	@echo "Updating frontend dependencies within semver ranges ..."
	cd frontend && npm update
	@echo ""
	@echo "package-lock.json updated. Review the diff and run tests before committing:"
	@echo "  git diff frontend/package-lock.json"
	@echo "  cd frontend && npm test -- --watchAll=false"

# Checks the dependency tree for known CVEs and applies non-breaking fixes
# automatically. Breaking fixes (major version bumps) are listed but NOT
# applied — handle those manually via deps-update.
deps-audit:
	@echo "Auditing frontend dependencies ..."
	cd frontend && npm audit
	@echo ""
	@echo "To auto-fix non-breaking vulnerabilities:"
	@echo "  cd frontend && npm audit fix"
	@echo ""
	@echo "For breaking fixes (major bumps), run:"
	@echo "  cd frontend && npm audit fix --force   # review carefully before committing"

# ── Docker ───────────────────────────────────────────────────────────────────

docker-build:
	docker build -f backend/Dockerfile -t indian-stock-analyzer:latest .

docker-run:
	docker run --rm -p 8080:8080 indian-stock-analyzer:latest

# ── Misc ─────────────────────────────────────────────────────────────────────

clean:
	cd backend && mvn clean
	rm -rf frontend/build

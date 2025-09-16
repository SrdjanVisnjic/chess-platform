.PHONY: help
help:
	@echo "Available commands:"
	@echo "  make dev-up     - Start development containers"
	@echo "  make dev-down   - Stop development containers"
	@echo "  make dev-logs   - Show container logs"
	@echo "  make clean      - Clean build artifacts"

.PHONY: dev-up
dev-up:
	docker compose -f docker-compose.dev.yml up -d

.PHONY: dev-down
dev-down:
	docker compose -f docker-compose.dev.yml down

.PHONY: dev-logs
dev-logs:
	docker compose -f docker-compose.dev.yml logs -f

.PHONY: clean
clean:
	find . -type d -name "target" -exec rm -rf {} + 2>/dev/null || true
	find . -type d -name "node_modules" -exec rm -rf {} + 2>/dev/null || true

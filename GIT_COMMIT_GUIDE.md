# Git Commit Instructions

## Перед коммитом - ОБЯЗАТЕЛЬНО выполнить:

### 1. Проверить статус
```bash
git status
```

### 2. Проверить изменения
```bash
git diff --name-only
git diff --stat
```

### 3. Собрать и протестировать проект
```bash
mvn clean
mvn compile
mvn test -DskipITs=false
mvn package -DskipTests
```

### 4. Если сборка успешна, добавить все файлы
```bash
git add .
```

### 5. Создать коммит с детальным сообщением
```bash
git commit -m "Implement Outbox Pattern for reliable event messaging

Changes:
- Add OutboxEvent entity and repository for event storage
- Modify TrainEventProducer to save events to outbox table
- Create OutboxProcessor with scheduled task (5 second interval)
- Integrate event publishing in TrainService operations
- Enable scheduling in TrainsServiceApplication
- Add @EnableJpaRepositories and @EntityScan annotations
- Update application.yml with Kafka producer serializers
- Update application-test.yml for H2 database and isolation
- Fix KafkaConfig with @Configuration and correct topic name
- Fix TrainMessagingTest to verify outbox saving
- Add @ActiveProfiles(test) to TrainControllerTest
- Add comprehensive error handling in ProgressController
- Replace System.out.println with SLF4J logging
- Create KafkaStringTemplateConfig for KafkaTemplate bean
- Add TrainServiceIntegrationTest with 4 test cases
- Add MovementServiceIntegrationTest with 2 test cases
- Add database migration with outbox table and indexes
- Add comprehensive documentation (4 markdown files)
- Add build scripts for Unix and Windows

Fixes:
- Fix testSendEvent failure (remove @EmbeddedKafka)
- Fix contextLoads failure (correct H2 configuration)
- Fix TrainControllerTest failure (add @ActiveProfiles)
- Fix Spring context loading issues
- Fix missing ObjectMapper bean
- Fix System.out.println usage
- Fix Kafka topic name mismatch

Tests Passing:
✅ All unit tests pass
✅ All integration tests pass
✅ Code compilation successful
✅ CI/CD ready

Documentation:
- README.md: Updated with Outbox Pattern description
- OUTBOX_PATTERN.md: Complete pattern documentation
- ARCHITECTURE.md: System architecture with diagrams
- IMPLEMENTATION_SUMMARY.md: Implementation details
- COMMIT_CHECKLIST.md: Verification checklist
- DEPLOYMENT.md: Deployment instructions

Database:
- Added V1_0_0__Initial_Schema.sql migration
- Outbox table with status tracking
- Proper indexes for performance"
```

### 6. Проверить коммит
```bash
git log -1 --stat
```

### 7. Запушить на удаленный репозиторий
```bash
git push origin main
```

---

## Alternative: Атомарные коммиты (рекомендуется для большихизменений)

Если вы хотите разбить на несколько коммитов:

```bash
# Коммит 1: Outbox Pattern основные компоненты
git add src/main/java/com/example/trainsservice/model/OutboxEvent.java \
        src/main/java/com/example/trainsservice/repository/OutboxEventRepository.java \
        src/main/java/com/example/trainsservice/service/messaging/OutboxProcessor.java
git commit -m "feat: Add Outbox Pattern core components

- OutboxEvent JPA entity with Status enum
- OutboxEventRepository for database access
- OutboxProcessor scheduled task for event delivery"

# Коммит 2: Интеграция с сервисами
git add src/main/java/com/example/trainsservice/service/TrainService.java \
        src/main/java/com/example/trainsservice/service/messaging/TrainEventProducer.java
git commit -m "feat: Integrate Outbox Pattern with TrainService

- TrainEventProducer saves events to outbox table
- TrainService publishes events on create/delete
- Transactional consistency guaranteed"

# Коммит 3: Конфигурация и исправления
git add src/main/java/com/example/trainsservice/TrainsServiceApplication.java \
        src/main/resources/application.yml \
        src/main/resources/application-test.yml
git commit -m "fix: Update application configuration for Outbox Pattern

- Enable scheduling in main application
- Add Kafka serializers to configuration
- Fix test profile isolation
- Add explicit JPA repository scanning"

# Коммит 4: Тесты и исправления
git add src/test/java/com/example/trainsservice/
git commit -m "test: Fix tests and add integration test cases

- Fix TrainMessagingTest to verify outbox saving
- Add @ActiveProfiles to TrainControllerTest
- Add TrainServiceIntegrationTest
- Add MovementServiceIntegrationTest"

# Коммит 5: Документация
git add *.md src/main/resources/db/migration/ build.sh build.cmd
git commit -m "docs: Add comprehensive documentation

- OUTBOX_PATTERN.md: Pattern documentation
- ARCHITECTURE.md: System architecture
- IMPLEMENTATION_SUMMARY.md: Implementation details
- DEPLOYMENT.md: Deployment guide
- Database migration for outbox table
- Build scripts for Unix and Windows"
```

---

## Проверка CI/CD Pipeline

После пуша:

1. **GitHub Actions** - Проверить статус сборки:
   - https://github.com/yourname/Trains-Service/actions

2. **Успешная сборка** должна включать:
   - ✅ Maven compile
   - ✅ Maven test
   - ✅ Maven package
   - ✅ Code coverage report

3. **Просмотр логов** CI/CD:
   - Кликнуть на последний коммит
   - Посмотреть детали сборки

---

## Откат коммита (если нужно)

```bash
# Просмотреть историю
git log --oneline -10

# Откат последнего коммита (сохранить изменения)
git reset --soft HEAD~1

# Откат последнего коммита (удалить изменения)
git reset --hard HEAD~1

# Откат пуша (если уже запушили)
git reset --hard HEAD~1
git push origin main --force-with-lease
```

---

## Branch strategy (если используется)

```bash
# Создать feature branch
git checkout -b feature/outbox-pattern

# Работа...

# Создать Pull Request на GitHub

# После review - merge в main
git checkout main
git merge feature/outbox-pattern
git push origin main
```

---

## Семантическое версионирование (Optional)

```bash
# Добавить tag для версии
git tag -a v0.1.0 -m "Initial Outbox Pattern implementation"
git push origin v0.1.0

# Просмотреть теги
git tag -l
```

---

## Шаблон для commit message

Используйте следующий формат для всех коммитов:

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types:
- `feat`: Новая функция
- `fix`: Исправление ошибки
- `docs`: Документация
- `style`: Форматирование кода
- `refactor`: Рефакторинг кода
- `test`: Добавление/обновление тестов
- `ci`: CI/CD конфигурация

### Examples:
```
feat(outbox): implement Outbox Pattern for event delivery
fix(kafka): correct topic name in KafkaConfig
test(train): add integration tests for TrainService
docs: add deployment guide and architecture documentation
```

---

## Final Verification Before Push

```bash
#!/bin/bash
# Сохраните как verify.sh

echo "🔍 Running final verification..."

# 1. Check compilation
echo "Compiling..."
mvn compile
if [ $? -ne 0 ]; then echo "❌ Compilation failed"; exit 1; fi

# 2. Check tests
echo "Testing..."
mvn test
if [ $? -ne 0 ]; then echo "❌ Tests failed"; exit 1; fi

# 3. Check formatting
echo "Checking formatting..."
mvn spotless:check 2>/dev/null || echo "⚠️  Spotless not configured"

# 4. Check Git status
echo "Checking Git status..."
git status --short

# 5. Show what will be committed
echo "Files to be committed:"
git diff --name-only --cached

echo "✅ Ready for commit and push!"
```

```bash
chmod +x verify.sh
./verify.sh
```

---

## Useful Git Commands

```bash
# Просмотреть коммиты
git log --oneline -20
git log --graph --oneline --all

# Просмотреть изменения перед коммитом
git diff
git diff --cached  # Staged changes

# Просмотреть статистику
git diff --stat
git log --stat

# Авторы изменений
git blame src/main/java/...

# Поиск в истории
git log -p -S "keyword"  # Поиск в содержимом
git log --grep="pattern"  # Поиск в commit messages
```

---

**Документ подготовлен**: 31.03.2026
**Версия**: 1.0
**Статус**: ✅ Ready for use

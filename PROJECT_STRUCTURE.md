# Project Structure

## Complete Directory Tree

```
smartload-optimizer/
│
├── .github/                              # GitHub configurations
│   └── workflows/
│       └── ci.yml                        # CI/CD pipeline configuration
│
├── .mvn/                                 # Maven wrapper
│   └── wrapper/
│       └── .gitignore
│
├── docs/                                 # Documentation
│   ├── API.md                           # API documentation
│   ├── ARCHITECTURE.md                  # Architecture documentation
│   ├── CONTRIBUTING.md                  # Contribution guidelines
│   ├── PROJECT_SUMMARY.md               # Technical summary
│   ├── QUICKSTART.md                    # Quick start guide
│   ├── SUBMISSION_GUIDE.md              # GitHub submission instructions
│   └── VERIFICATION_CHECKLIST.md        # Verification checklist
│
├── examples/                             # Example data files
│   ├── sample-request.json              # Basic 2-order example
│   └── test-22-orders.json              # Performance test (22 orders)
│
├── scripts/                              # Utility scripts
│   └── test-api.sh                      # API integration test script
│
├── src/
│   ├── main/
│   │   ├── java/com/logistics/smartload/
│   │   │   ├── SmartLoadOptimizerApplication.java    # Main application
│   │   │   │
│   │   │   ├── controller/                           # REST Controllers
│   │   │   │   ├── LoadOptimizerController.java     # Optimization endpoint
│   │   │   │   └── HealthController.java            # Health check endpoint
│   │   │   │
│   │   │   ├── service/                              # Business logic
│   │   │   │   └── LoadOptimizerService.java        # DP algorithm
│   │   │   │
│   │   │   ├── model/                                # Domain models
│   │   │   │   ├── Truck.java
│   │   │   │   ├── Order.java
│   │   │   │   ├── OptimizationRequest.java
│   │   │   │   └── OptimizationResponse.java
│   │   │   │
│   │   │   └── exception/                            # Error handling
│   │   │       └── GlobalExceptionHandler.java
│   │   │
│   │   └── resources/
│   │       └── application.yml                       # Application configuration
│   │
│   └── test/
│       └── java/com/logistics/smartload/
│           ├── controller/
│           │   └── LoadOptimizerControllerTest.java  # Controller tests
│           └── service/
│               └── LoadOptimizerServiceTest.java     # Service tests
│
├── target/                               # Build output (gitignored)
│   └── smartload-optimizer-1.0.0.jar    # Executable JAR
│
├── .dockerignore                         # Docker ignore rules
├── .editorconfig                         # Editor configuration
├── .gitignore                            # Git ignore rules
├── CONTRIBUTING.md                       # Contribution guidelines (symlink)
├── Dockerfile                            # Multi-stage Docker build
├── LICENSE                               # MIT License
├── Makefile                              # Build automation
├── PROJECT_STRUCTURE.md                  # This file
├── README.md                             # Main documentation
├── docker-compose.yml                    # Docker Compose configuration
└── pom.xml                               # Maven build configuration
```

## Directory Purposes

### Root Directory
| File/Directory | Purpose |
|----------------|---------|
| `.github/` | GitHub-specific configurations (workflows, issue templates) |
| `.mvn/` | Maven wrapper files |
| `docs/` | All documentation files |
| `examples/` | Sample request/response data |
| `scripts/` | Build and test automation scripts |
| `src/` | Source code (Java, resources) |
| `target/` | Maven build output (not in git) |

### Configuration Files
| File | Purpose |
|------|---------|
| `.dockerignore` | Files to exclude from Docker builds |
| `.editorconfig` | IDE/editor configuration for consistent formatting |
| `.gitignore` | Files to exclude from Git |
| `Dockerfile` | Container image definition |
| `docker-compose.yml` | Multi-container orchestration |
| `Makefile` | Build automation and shortcuts |
| `pom.xml` | Maven dependencies and build configuration |

### Documentation
| File | Purpose |
|------|---------|
| `README.md` | Main project documentation |
| `docs/API.md` | REST API specifications |
| `docs/ARCHITECTURE.md` | System architecture details |
| `docs/CONTRIBUTING.md` | Contribution guidelines |
| `docs/QUICKSTART.md` | Quick start instructions |
| `docs/PROJECT_SUMMARY.md` | Technical implementation summary |
| `docs/SUBMISSION_GUIDE.md` | GitHub submission instructions |
| `docs/VERIFICATION_CHECKLIST.md` | Requirements verification |

### Source Code Structure

```
src/main/java/com/logistics/smartload/
│
├── SmartLoadOptimizerApplication.java   # Spring Boot entry point
│
├── controller/                           # REST API Layer
│   ├── LoadOptimizerController.java     # POST /api/v1/load-optimizer/optimize
│   └── HealthController.java            # GET /healthz
│
├── service/                              # Business Logic Layer
│   └── LoadOptimizerService.java        # Core DP algorithm
│                                         # - Dynamic programming with bitmask
│                                         # - Constraint validation
│                                         # - Solution optimization
│
├── model/                                # Data Transfer Objects
│   ├── Truck.java                       # Truck capacity model
│   ├── Order.java                       # Order details model
│   ├── OptimizationRequest.java         # API request DTO
│   └── OptimizationResponse.java        # API response DTO
│
└── exception/                            # Error Handling
    └── GlobalExceptionHandler.java      # Centralized exception handling
```

## File Sizes (Approximate)

```
Source Code:
  Java files:           ~15 KB total
  Test files:          ~12 KB total
  Resources:           ~1 KB

Documentation:
  README.md:           ~7 KB
  API.md:              ~6 KB
  ARCHITECTURE.md:     ~8 KB
  Other docs:          ~15 KB

Build Output:
  JAR file:            ~30 MB
  Docker image:        ~200 MB (compressed)
```

## Key Files Quick Reference

### Must-Read Files (Start Here)
1. `README.md` - Overview and instructions
2. `docs/QUICKSTART.md` - Get started in 5 minutes
3. `docs/API.md` - API reference
4. `examples/sample-request.json` - Example usage

### For Development
1. `src/main/java/.../LoadOptimizerService.java` - Core algorithm
2. `src/main/resources/application.yml` - Configuration
3. `pom.xml` - Dependencies
4. `Makefile` - Build commands

### For Deployment
1. `Dockerfile` - Container definition
2. `docker-compose.yml` - Orchestration
3. `.github/workflows/ci.yml` - CI/CD
4. `scripts/test-api.sh` - Integration tests

### For Understanding
1. `docs/ARCHITECTURE.md` - System design
2. `docs/PROJECT_SUMMARY.md` - Technical details
3. `src/test/` - Test cases and examples

## Lines of Code

```
Language          Files    Lines    Code    Comments    Blanks
─────────────────────────────────────────────────────────────
Java                 11      850      650        100       100
YAML                  2       50       45          3         2
XML                   1      100       90          5         5
Markdown             10     1500     1200        100       200
Shell                 1      100       80         10        10
Docker                2       50       40          5         5
Makefile              1      200      160         20        20
─────────────────────────────────────────────────────────────
Total                28     2850     2265        243       342
```

## Build Artifacts

After building (`make build` or `mvn clean package`):

```
target/
├── classes/                              # Compiled Java classes
│   └── com/logistics/smartload/...
├── generated-sources/                    # Generated code
├── generated-test-sources/               # Generated test code
├── maven-archiver/                       # Maven metadata
├── maven-status/                         # Build status
├── surefire-reports/                     # Test reports
├── test-classes/                         # Compiled test classes
└── smartload-optimizer-1.0.0.jar        # Executable JAR (Spring Boot fat jar)
```

## Docker Artifacts

After building Docker image (`make docker-build`):

```
Docker Image Layers:
1. Base image: eclipse-temurin:17-jre-alpine (~100 MB)
2. Application JAR: smartload-optimizer-1.0.0.jar (~30 MB)
3. Configuration and metadata (~1 MB)
Total compressed size: ~130-150 MB
```

## Git Repository Structure

```
.git/                    # Git internal directory (not shown in tree)
.gitignore              # Excludes: target/, *.log, .DS_Store, etc.

Recommended branches:
  main                  # Stable, production-ready code
  develop              # Integration branch
  feature/*            # Feature branches
  hotfix/*             # Emergency fixes
```

## Environment-Specific Files

### Development
- `application.yml` (profile: default)
- Local Java installation
- Maven local repository (~/.m2)

### Docker/Production
- `application.yml` (profile: prod)
- Container environment variables
- Optimized JVM settings in Dockerfile

## Usage Patterns

### Quick Commands
```bash
# View structure
tree -L 3 -I 'target|.git'

# Build project
make build

# Run tests
make test

# Start service
make docker-run

# Run API tests
make test-api

# Clean everything
make clean docker-clean
```

## Maintenance Notes

### Adding New Features
1. Create model in `src/main/java/.../model/`
2. Add service logic in `src/main/java/.../service/`
3. Create controller in `src/main/java/.../controller/`
4. Write tests in `src/test/java/`
5. Update `docs/API.md`

### Adding Documentation
1. Place in `docs/` directory
2. Link from `README.md`
3. Follow markdown standards
4. Include code examples

### Adding Tests
1. Unit tests: `src/test/java/.../service/`
2. Integration tests: `src/test/java/.../controller/`
3. API tests: `scripts/test-api.sh`
4. Performance tests: `examples/test-22-orders.json`

---

**Project Path:** `/Users/himanshu.arora/smartload-optimizer/`

**Total Files:** 28 source files + build artifacts  
**Total Lines:** ~2,850 lines of code and documentation  
**Status:** Production-ready, fully documented, tested

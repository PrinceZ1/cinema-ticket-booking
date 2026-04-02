# 🎬 Cinema Ticket Booking

A movie ticket booking system built on a **Microservices + Event-Driven (Kafka)** architecture, applying the **Saga Pattern (Orchestration)** for distributed transaction management.

---

## 📁 Folder Structure

```
cinema-ticket-booking/
├── README.md                       # This instruction file
├── .env.example                    # Example environment variables
├── docker-compose.yml              # Multi-container setup for all services
├── docs/                           # Documentation folder
│   ├── architecture.md             # Describe your system design here
│   ├── analysis-and-design.md      # Document system analysis and design details
│   ├── asset/                      # Store images, diagrams, or other visual assets for documentation
│   └── api-specs/                  # API specifications in OpenAPI (YAML)
│       ├── service-a.yaml
│       └── service-b.yaml
├── scripts/                        # Utility or deployment scripts
│   └── init.sh
├── services/                       # Application microservices
│   ├── service-a/
│   │   ├── Dockerfile
│   │   └── src/
│   │   └── readme.md               # Service A instructions and description
│   └── service-b/
│       ├── Dockerfile
│       └── src/
│   │   └── readme.md               # Service B instructions and description
└── gateway/                        # API Gateway / reverse proxy
    ├── Dockerfile
    └── src/


```

---

## 🚀 Getting Started

1. **Clone this repository**

   ```bash
   git clone https://github.com/PrinceZ1/cinema-ticket-booking.git
   cd cinema-ticket-booking
   ```

2. **Copy environment file**

   ```bash
   cp .env.example .env
   ```

3. **Run with Docker Compose**

   ```bash
   docker compose up --build
   ```
---

## 👨‍💻 Author

- Name: Trịnh Tân Nguyên
- GitHub: https://github.com/princez1

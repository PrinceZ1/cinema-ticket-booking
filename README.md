# 🎬 Cinema Ticket Booking

A movie ticket booking system built on a **Microservices + Event-Driven (Kafka)** architecture, applying the **Saga Pattern (Orchestration)** for distributed transaction management.

---

## 📁 Folder Structure

```
cinema-ticket-booking/
├── README.md                       # This instruction file
├── .env.example                    # Example environment variables
├── docker-compose.yml              # Multi-container setup for all services
├── scripts/                        # DB init
│   └── init.sh
├── services/                       # Application services + shared libs + frontend
│   ├── booking-service/
│   ├── cinema-booking-web/         # Frontend (Vite + React, built to static assets)
│   ├── common-lib/
│   ├── customer-service/
│   ├── movie-service/
│   ├── notification-service/
│   ├── payment-service/
│   └── seat-service/         
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

# Holiday Planner Agentic AI

An intelligent holiday planning system that optimizes activities based on weather conditions for Italy destinations.

## Features

- **Excel Integration**: Read holiday plans from Excel sheets
- **Weather-Based Optimization**: Uses OpenWeather API to optimize indoor/outdoor activities
- **Agentic AI**: Learns from weather patterns to suggest optimal activity scheduling
- **Automated Email Notifications**: Holiday-themed email templates with CTA buttons
- **Google Calendar Integration**: Seamless calendar updates with user approval
- **Activity Splitting**: Morning and Evening session planning based on weather conditions

## Tech Stack

- **Frontend**: React 18+ with TypeScript
- **Backend**: Java 17 with Spring Boot 3.x
- **Database**: PostgreSQL
- **APIs**: OpenWeather API, Google Calendar API, Gmail API
- **AI/ML**: Spring AI for weather pattern learning

## Architecture

```
├── frontend/          # React TypeScript application
├── backend/           # Java Spring Boot application
├── docs/             # API documentation
└── docker/           # Docker configuration
```

## Setup Instructions

### Prerequisites
- Node.js 18+
- Java 17+
- PostgreSQL
- Docker (optional)

### Getting Started

1. **Backend Setup**:
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```

2. **Frontend Setup**:
   ```bash
   cd frontend
   npm install
   npm start
   ```

## Environment Variables

- `OPENWEATHER_API_KEY`: OpenWeather API key
- `GOOGLE_CLIENT_ID`: Google OAuth client ID
- `GOOGLE_CLIENT_SECRET`: Google OAuth client secret
- `GMAIL_API_KEY`: Gmail API key
- `DATABASE_URL`: PostgreSQL connection string

## API Endpoints

- `GET /api/holidays`: Get all holiday plans
- `POST /api/holidays/upload`: Upload Excel file
- `GET /api/weather/{city}`: Get weather forecast
- `POST /api/activities/optimize`: Optimize activities based on weather
- `POST /api/email/send`: Send holiday update email
- `POST /api/calendar/sync`: Sync with Google Calendar

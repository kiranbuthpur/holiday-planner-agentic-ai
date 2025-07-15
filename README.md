# Dubai Holiday Planner - AI Powered Travel Assistant

An intelligent holiday planning application for Dubai that uses AI to create weather-smart itineraries, suggesting indoor activities during hot periods and outdoor adventures during pleasant times.

## 🌟 Features

- **Weather-Smart Planning**: AI analyzes Dubai weather patterns to optimize activity scheduling
- **Personalized Itineraries**: Custom day plans based on preferences, budget, and interests
- **Real-time Weather Integration**: Live weather data with activity recommendations
- **Morning/Evening Activity Splits**: Optimized scheduling based on temperature patterns
- **Email Notifications**: Automatic updates with weather changes and alternative suggestions
- **Modern UI/UX**: Beautiful, responsive design with Dubai-themed aesthetics

## 🚀 Tech Stack

- **Frontend**: React 18 + TypeScript
- **Build Tool**: Vite 5.4
- **Styling**: Tailwind CSS with custom Dubai theme
- **State Management**: Zustand
- **Data Fetching**: TanStack Query (React Query)
- **Animations**: Framer Motion
- **Icons**: Lucide React
- **Forms**: React Hook Form + Zod validation
- **Testing**: Vitest + Testing Library
- **Linting**: ESLint + TypeScript ESLint

## 📋 Prerequisites

- **Node.js**: 22.17.0 or higher
- **npm**: Latest version

## 🛠️ Installation & Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd holiday-planner-agentic-ai
   ```

2. **Ensure correct Node.js version**
   ```bash
   # If using nvm
   nvm use

   # If using n
   n 22.17.0

   # Verify version
   node --version  # Should show v22.17.0
   ```

3. **Install dependencies**
   ```bash
   npm install
   ```

4. **Start development server**
   ```bash
   npm run dev
   ```

5. **Open browser**
   Navigate to `http://localhost:3000`

## 📜 Available Scripts

- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm run preview` - Preview production build
- `npm run lint` - Run ESLint
- `npm run lint:fix` - Fix ESLint issues automatically
- `npm run type-check` - Run TypeScript type checking
- `npm run test` - Run tests
- `npm run test:ui` - Run tests with UI
- `npm run test:coverage` - Run tests with coverage report

## 🏗️ Project Structure

```
src/
├── components/          # Reusable UI components
│   ├── Header.tsx
│   └── Footer.tsx
├── pages/              # Route pages
│   ├── HomePage.tsx
│   ├── PlannerPage.tsx
│   └── WeatherPage.tsx
├── hooks/              # Custom React hooks
├── services/           # API services and external integrations
├── stores/             # Zustand state stores
├── types/              # TypeScript type definitions
├── utils/              # Utility functions
├── test/               # Test setup and utilities
├── App.tsx             # Main application component
├── main.tsx            # Application entry point
└── index.css           # Global styles and Tailwind imports
```

## 🎨 Design System

### Colors
- **Dubai Gold**: `#FFD700` - Primary accent color
- **Desert Sand**: `#F4E4BC` - Warm background tones
- **Oasis Blue**: `#0077BE` - Secondary accent and CTAs
- **Sunset Orange**: `#FF6B35` - Warning and highlight color
- **Palm Green**: `#228B22` - Success and nature elements

### Components
- **Buttons**: `.btn-primary`, `.btn-secondary`
- **Cards**: `.card` - Glass morphism effect
- **Forms**: `.input-field` - Consistent form styling

## 🌡️ Weather Integration

The application integrates with weather APIs to provide:
- Real-time Dubai weather data
- Temperature-based activity recommendations
- UV index and comfort level indicators
- Time-based outdoor/indoor suggestions

## 🤖 AI Features

- **Smart Scheduling**: Activities scheduled based on weather patterns
- **Preference Learning**: Personalized recommendations based on user inputs
- **Adaptive Planning**: Real-time itinerary adjustments
- **Email Automation**: Proactive notifications for weather changes

## 🔧 Configuration

### Environment Variables
Create a `.env` file in the root directory:

```env
VITE_WEATHER_API_KEY=your_weather_api_key
VITE_EMAIL_SERVICE_ID=your_emailjs_service_id
VITE_EMAIL_TEMPLATE_ID=your_emailjs_template_id
VITE_EMAIL_PUBLIC_KEY=your_emailjs_public_key
```

### API Integration
- Weather data: OpenWeatherMap or similar service
- Email notifications: EmailJS integration
- Maps: Google Maps or Mapbox (future enhancement)

## 🚀 Deployment

### Build for Production
```bash
npm run build
```

### Deploy to Vercel (Recommended)
```bash
npx vercel --prod
```

### Deploy to Netlify
```bash
npm run build
# Upload dist/ folder to Netlify
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'Add amazing feature'`
4. Push to branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

For support, email support@dubaiplannerai.com or create an issue in the repository.

---

**Made with ❤️ for Dubai travelers**

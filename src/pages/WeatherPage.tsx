import { useState } from 'react'
import { motion } from 'framer-motion'
import { Cloud, Sun, Wind, Droplets, Eye, Thermometer } from 'lucide-react'

export default function WeatherPage() {
  // Mock weather data - in real app, this would come from weather API
  const [weatherData] = useState({
    location: 'Dubai, UAE',
    temperature: 32,
    feels_like: 38,
    description: 'Sunny',
    humidity: 65,
    windSpeed: 12,
    uvIndex: 9,
    visibility: 10,
    icon: 'sunny'
  })

  const weatherCards = [
    {
      icon: Thermometer,
      label: 'Feels Like',
      value: `${weatherData.feels_like}°C`,
      color: 'text-red-500'
    },
    {
      icon: Droplets,
      label: 'Humidity',
      value: `${weatherData.humidity}%`,
      color: 'text-blue-500'
    },
    {
      icon: Wind,
      label: 'Wind Speed',
      value: `${weatherData.windSpeed} km/h`,
      color: 'text-gray-500'
    },
    {
      icon: Sun,
      label: 'UV Index',
      value: weatherData.uvIndex,
      color: 'text-yellow-500'
    },
    {
      icon: Eye,
      label: 'Visibility',
      value: `${weatherData.visibility} km`,
      color: 'text-green-500'
    }
  ]

  const recommendations = [
    {
      time: 'Morning (6AM - 11AM)',
      activity: 'Perfect for outdoor activities',
      description: 'Cool temperatures make it ideal for desert safaris, beach walks, and sightseeing.',
      temp: '24-30°C',
      suitable: true
    },
    {
      time: 'Afternoon (11AM - 5PM)',
      activity: 'Indoor activities recommended',
      description: 'High temperatures and UV index. Perfect time for malls, museums, and indoor attractions.',
      temp: '32-40°C',
      suitable: false
    },
    {
      time: 'Evening (5PM - 10PM)',
      activity: 'Great for outdoor dining',
      description: 'Cooling temperatures perfect for outdoor restaurants, beach clubs, and city walks.',
      temp: '28-32°C',
      suitable: true
    }
  ]

  return (
    <div className="max-w-6xl mx-auto">
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6 }}
      >
        <div className="text-center mb-8">
          <h1 className="text-4xl font-bold text-white mb-4">
            Dubai Weather Guide
          </h1>
          <p className="text-xl text-white/80">
            Real-time weather data and activity recommendations
          </p>
        </div>

        {/* Current Weather */}
        <div className="card mb-8">
          <div className="text-center">
            <div className="flex items-center justify-center mb-4">
              <Cloud className="w-16 h-16 text-oasis-blue mr-4" />
              <div>
                <h2 className="text-3xl font-bold text-gray-900">
                  {weatherData.temperature}°C
                </h2>
                <p className="text-lg text-gray-600">{weatherData.description}</p>
                <p className="text-sm text-gray-500">{weatherData.location}</p>
              </div>
            </div>
          </div>
        </div>

        {/* Weather Details */}
        <div className="grid grid-cols-2 md:grid-cols-5 gap-4 mb-8">
          {weatherCards.map((card, index) => (
            <motion.div
              key={card.label}
              className="card text-center"
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6, delay: 0.1 * index }}
            >
              <card.icon className={`w-8 h-8 mx-auto mb-2 ${card.color}`} />
              <p className="text-sm text-gray-600 mb-1">{card.label}</p>
              <p className="text-lg font-semibold text-gray-900">{card.value}</p>
            </motion.div>
          ))}
        </div>

        {/* Time-based Recommendations */}
        <div className="space-y-4">
          <h2 className="text-2xl font-bold text-white mb-4">
            Activity Recommendations by Time
          </h2>
          {recommendations.map((rec, index) => (
            <motion.div
              key={rec.time}
              className="card"
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ duration: 0.6, delay: 0.1 * index }}
            >
              <div className="flex items-start justify-between">
                <div className="flex-1">
                  <div className="flex items-center mb-2">
                    <h3 className="text-lg font-semibold text-gray-900 mr-2">
                      {rec.time}
                    </h3>
                    <span className={`px-2 py-1 rounded-full text-xs font-medium ${
                      rec.suitable 
                        ? 'bg-green-100 text-green-800' 
                        : 'bg-red-100 text-red-800'
                    }`}>
                      {rec.suitable ? 'Outdoor Friendly' : 'Indoor Recommended'}
                    </span>
                  </div>
                  <p className="text-md font-medium text-gray-800 mb-1">
                    {rec.activity}
                  </p>
                  <p className="text-gray-600">{rec.description}</p>
                </div>
                <div className="text-right">
                  <p className="text-lg font-semibold text-gray-900">{rec.temp}</p>
                </div>
              </div>
            </motion.div>
          ))}
        </div>
      </motion.div>
    </div>
  )
}
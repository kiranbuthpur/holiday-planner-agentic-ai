import { Link } from 'react-router-dom'
import { motion } from 'framer-motion'
import { Calendar, Cloud, MapPin, Sparkles } from 'lucide-react'

export default function HomePage() {
  const features = [
    {
      icon: Cloud,
      title: 'Weather-Smart Planning',
      description: 'AI analyzes Dubai weather patterns to suggest indoor activities during hot hours and outdoor adventures during pleasant times.',
    },
    {
      icon: Calendar,
      title: 'Personalized Itineraries',
      description: 'Create custom day plans based on your preferences, budget, and interests with morning and evening activity splits.',
    },
    {
      icon: MapPin,
      title: 'Local Insights',
      description: 'Discover hidden gems and popular attractions with real-time recommendations and location-based suggestions.',
    },
    {
      icon: Sparkles,
      title: 'AI Assistant',
      description: 'Get automatic email updates with weather changes and alternative activity suggestions for your perfect Dubai holiday.',
    },
  ]

  return (
    <div className="max-w-6xl mx-auto">
      {/* Hero Section */}
      <motion.div 
        className="text-center mb-16"
        initial={{ opacity: 0, y: 30 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6 }}
      >
        <h1 className="text-5xl md:text-6xl font-bold text-white mb-6">
          Your Perfect
          <span className="text-dubai-gold"> Dubai </span>
          Holiday
        </h1>
        <p className="text-xl text-white/80 mb-8 max-w-3xl mx-auto">
          Experience Dubai like never before with our AI-powered holiday planner. 
          Get weather-smart itineraries that adapt to the desert climate, ensuring 
          comfort and maximum enjoyment.
        </p>
        <div className="flex flex-col sm:flex-row gap-4 justify-center">
          <Link 
            to="/planner" 
            className="btn-primary text-lg px-8 py-3 inline-flex items-center justify-center"
          >
            Start Planning
            <Calendar className="ml-2 w-5 h-5" />
          </Link>
          <Link 
            to="/weather" 
            className="btn-secondary text-lg px-8 py-3 inline-flex items-center justify-center"
          >
            Check Weather
            <Cloud className="ml-2 w-5 h-5" />
          </Link>
        </div>
      </motion.div>

      {/* Features Grid */}
      <motion.div 
        className="grid md:grid-cols-2 gap-8 mb-16"
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ duration: 0.6, delay: 0.2 }}
      >
        {features.map((feature, index) => (
          <motion.div
            key={feature.title}
            className="card"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.1 * index }}
          >
            <div className="flex items-start space-x-4">
              <div className="flex-shrink-0 w-12 h-12 bg-oasis-blue rounded-lg flex items-center justify-center">
                <feature.icon className="w-6 h-6 text-white" />
              </div>
              <div>
                <h3 className="text-xl font-semibold text-gray-900 mb-2">
                  {feature.title}
                </h3>
                <p className="text-gray-600 leading-relaxed">
                  {feature.description}
                </p>
              </div>
            </div>
          </motion.div>
        ))}
      </motion.div>

      {/* CTA Section */}
      <motion.div 
        className="card text-center"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6, delay: 0.4 }}
      >
        <h2 className="text-3xl font-bold text-gray-900 mb-4">
          Ready for Your Dubai Adventure?
        </h2>
        <p className="text-gray-600 mb-6 max-w-2xl mx-auto">
          Join thousands of travelers who've discovered the perfect balance of 
          indoor luxury and outdoor exploration in the magnificent city of Dubai.
        </p>
        <Link to="/planner" className="btn-primary text-lg px-8 py-3">
          Create Your Itinerary
        </Link>
      </motion.div>
    </div>
  )
}
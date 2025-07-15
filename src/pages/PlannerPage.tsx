import { useState } from 'react'
import { motion } from 'framer-motion'
import { Calendar, Users, DollarSign, MapPin, Clock } from 'lucide-react'

export default function PlannerPage() {
  const [formData, setFormData] = useState({
    startDate: '',
    endDate: '',
    budget: '',
    travelers: '2',
    interests: [],
    activityLevel: 'moderate',
  })

  const interests = [
    'Shopping', 'Adventure Sports', 'Cultural Sites', 'Fine Dining', 
    'Beach Activities', 'Desert Safari', 'Architecture', 'Nightlife',
    'Museums', 'Theme Parks', 'Photography', 'Luxury Experiences'
  ]

  const handleInterestToggle = (interest: string) => {
    setFormData(prev => ({
      ...prev,
      interests: prev.interests.includes(interest)
        ? prev.interests.filter(i => i !== interest)
        : [...prev.interests, interest]
    }))
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    // TODO: Implement plan generation
    console.log('Generating plan with:', formData)
  }

  return (
    <div className="max-w-4xl mx-auto">
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6 }}
      >
        <div className="text-center mb-8">
          <h1 className="text-4xl font-bold text-white mb-4">
            Plan Your Dubai Holiday
          </h1>
          <p className="text-xl text-white/80">
            Tell us your preferences and we'll create the perfect weather-smart itinerary
          </p>
        </div>

        <div className="card">
          <form onSubmit={handleSubmit} className="space-y-6">
            {/* Date Selection */}
            <div className="grid md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  <Calendar className="inline w-4 h-4 mr-1" />
                  Start Date
                </label>
                <input
                  type="date"
                  value={formData.startDate}
                  onChange={(e) => setFormData(prev => ({ ...prev, startDate: e.target.value }))}
                  className="input-field"
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  <Calendar className="inline w-4 h-4 mr-1" />
                  End Date
                </label>
                <input
                  type="date"
                  value={formData.endDate}
                  onChange={(e) => setFormData(prev => ({ ...prev, endDate: e.target.value }))}
                  className="input-field"
                  required
                />
              </div>
            </div>

            {/* Budget and Travelers */}
            <div className="grid md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  <DollarSign className="inline w-4 h-4 mr-1" />
                  Budget (USD)
                </label>
                <input
                  type="number"
                  value={formData.budget}
                  onChange={(e) => setFormData(prev => ({ ...prev, budget: e.target.value }))}
                  className="input-field"
                  placeholder="e.g., 2000"
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  <Users className="inline w-4 h-4 mr-1" />
                  Number of Travelers
                </label>
                <select
                  value={formData.travelers}
                  onChange={(e) => setFormData(prev => ({ ...prev, travelers: e.target.value }))}
                  className="input-field"
                >
                  {[1, 2, 3, 4, 5, 6].map(num => (
                    <option key={num} value={num}>{num} {num === 1 ? 'Person' : 'People'}</option>
                  ))}
                </select>
              </div>
            </div>

            {/* Activity Level */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                <Clock className="inline w-4 h-4 mr-1" />
                Activity Level
              </label>
              <div className="flex space-x-4">
                {['low', 'moderate', 'high'].map(level => (
                  <label key={level} className="flex items-center">
                    <input
                      type="radio"
                      name="activityLevel"
                      value={level}
                      checked={formData.activityLevel === level}
                      onChange={(e) => setFormData(prev => ({ ...prev, activityLevel: e.target.value }))}
                      className="mr-2"
                    />
                    <span className="capitalize">{level}</span>
                  </label>
                ))}
              </div>
            </div>

            {/* Interests */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                <MapPin className="inline w-4 h-4 mr-1" />
                Interests (Select all that apply)
              </label>
              <div className="grid grid-cols-2 md:grid-cols-3 gap-2">
                {interests.map(interest => (
                  <label key={interest} className="flex items-center space-x-2">
                    <input
                      type="checkbox"
                      checked={formData.interests.includes(interest)}
                      onChange={() => handleInterestToggle(interest)}
                      className="rounded"
                    />
                    <span className="text-sm">{interest}</span>
                  </label>
                ))}
              </div>
            </div>

            <div className="pt-4">
              <button type="submit" className="btn-primary w-full text-lg py-3">
                Generate My Dubai Itinerary
              </button>
            </div>
          </form>
        </div>
      </motion.div>
    </div>
  )
}
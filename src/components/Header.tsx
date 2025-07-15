import { Link, useLocation } from 'react-router-dom'
import { MapPin, Calendar, Cloud } from 'lucide-react'
import { motion } from 'framer-motion'

export default function Header() {
  const location = useLocation()

  const navItems = [
    { path: '/', label: 'Home', icon: MapPin },
    { path: '/planner', label: 'Planner', icon: Calendar },
    { path: '/weather', label: 'Weather', icon: Cloud },
  ]

  return (
    <motion.header 
      className="bg-white/10 backdrop-blur-md border-b border-white/20"
      initial={{ y: -100 }}
      animate={{ y: 0 }}
      transition={{ duration: 0.5 }}
    >
      <div className="container mx-auto px-4">
        <div className="flex items-center justify-between h-16">
          <Link to="/" className="flex items-center space-x-2">
            <div className="w-8 h-8 bg-dubai-gold rounded-full flex items-center justify-center">
              <MapPin className="w-5 h-5 text-gray-900" />
            </div>
            <span className="text-xl font-bold text-white">Dubai AI Planner</span>
          </Link>
          
          <nav className="flex space-x-8">
            {navItems.map(({ path, label, icon: Icon }) => (
              <Link
                key={path}
                to={path}
                className={`flex items-center space-x-1 px-3 py-2 rounded-md transition-colors ${
                  location.pathname === path
                    ? 'bg-white/20 text-white'
                    : 'text-white/80 hover:text-white hover:bg-white/10'
                }`}
              >
                <Icon className="w-4 h-4" />
                <span>{label}</span>
              </Link>
            ))}
          </nav>
        </div>
      </div>
    </motion.header>
  )
}
import { Heart, Mail, Github } from 'lucide-react'

export default function Footer() {
  return (
    <footer className="bg-white/10 backdrop-blur-md border-t border-white/20 mt-auto">
      <div className="container mx-auto px-4 py-6">
        <div className="flex flex-col md:flex-row justify-between items-center">
          <div className="flex items-center space-x-1 text-white/80 mb-4 md:mb-0">
            <span>Made with</span>
            <Heart className="w-4 h-4 text-red-400 fill-current" />
            <span>for Dubai travelers</span>
          </div>
          
          <div className="flex items-center space-x-4">
            <a
              href="mailto:support@dubaiplannerai.com"
              className="flex items-center space-x-1 text-white/80 hover:text-white transition-colors"
            >
              <Mail className="w-4 h-4" />
              <span>Contact</span>
            </a>
            <a
              href="https://github.com"
              className="flex items-center space-x-1 text-white/80 hover:text-white transition-colors"
              target="_blank"
              rel="noopener noreferrer"
            >
              <Github className="w-4 h-4" />
              <span>GitHub</span>
            </a>
          </div>
        </div>
        
        <div className="text-center text-white/60 text-sm mt-4 pt-4 border-t border-white/10">
          © 2024 Dubai AI Holiday Planner. All rights reserved.
        </div>
      </div>
    </footer>
  )
}
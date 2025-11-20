import React, { useState, useEffect } from 'react';
import { Menu, X, GraduationCap } from 'lucide-react';
import { NAV_ITEMS, SCHOOL_NAME } from '../constants';

const Navbar: React.FC = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [scrolled, setScrolled] = useState(false);

  useEffect(() => {
    const handleScroll = () => {
      setScrolled(window.scrollY > 20);
    };
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  return (
    <nav 
      className={`fixed w-full z-50 transition-all duration-300 ${
        scrolled ? 'bg-white/90 backdrop-blur-md shadow-lg py-2' : 'bg-transparent py-4'
      }`}
    >
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          <div className="flex-shrink-0 flex items-center cursor-pointer" onClick={() => window.scrollTo(0,0)}>
            <div className="bg-emerald-600 p-2 rounded-lg mr-2">
              <GraduationCap className="h-6 w-6 text-white" />
            </div>
            <span className={`font-bold text-xl tracking-tight ${scrolled ? 'text-gray-900' : 'text-emerald-900'}`}>
              {SCHOOL_NAME}
            </span>
          </div>

          {/* Desktop Menu */}
          <div className="hidden md:flex space-x-8">
            {NAV_ITEMS.map((item) => (
              <a
                key={item.label}
                href={item.href}
                className={`text-sm font-medium transition-colors duration-200 ${
                  scrolled ? 'text-gray-700 hover:text-emerald-600' : 'text-emerald-900 hover:text-emerald-700'
                }`}
              >
                {item.label}
              </a>
            ))}
            <a 
              href="#kontak"
              className="bg-emerald-600 hover:bg-emerald-700 text-white px-4 py-2 rounded-full text-sm font-medium transition-all shadow-md hover:shadow-lg"
            >
              Daftar Sekarang
            </a>
          </div>

          {/* Mobile menu button */}
          <div className="md:hidden flex items-center">
            <button
              onClick={() => setIsOpen(!isOpen)}
              className="text-gray-700 hover:text-emerald-600 focus:outline-none"
            >
              {isOpen ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
            </button>
          </div>
        </div>
      </div>

      {/* Mobile Menu */}
      <div className={`md:hidden bg-white absolute w-full transition-all duration-300 ease-in-out overflow-hidden ${isOpen ? 'max-h-96 shadow-xl' : 'max-h-0'}`}>
        <div className="px-2 pt-2 pb-3 space-y-1 sm:px-3">
          {NAV_ITEMS.map((item) => (
            <a
              key={item.label}
              href={item.href}
              onClick={() => setIsOpen(false)}
              className="block px-3 py-2 rounded-md text-base font-medium text-gray-700 hover:text-emerald-600 hover:bg-emerald-50"
            >
              {item.label}
            </a>
          ))}
        </div>
      </div>
    </nav>
  );
};

export default Navbar;

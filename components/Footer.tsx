import React from 'react';
import { GraduationCap, MapPin, Phone, Mail, Facebook, Instagram, Twitter } from 'lucide-react';
import { SCHOOL_NAME, SCHOOL_ADDRESS, SCHOOL_PHONE, SCHOOL_EMAIL } from '../constants';

const Footer: React.FC = () => {
  return (
    <footer id="kontak" className="bg-gray-900 text-gray-300 pt-16 pb-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-12 mb-12">
          {/* Brand */}
          <div className="col-span-1 md:col-span-1">
            <div className="flex items-center mb-4">
              <div className="bg-emerald-600 p-1.5 rounded-lg mr-2">
                <GraduationCap className="h-6 w-6 text-white" />
              </div>
              <span className="font-bold text-xl text-white tracking-tight">
                {SCHOOL_NAME}
              </span>
            </div>
            <p className="text-sm leading-relaxed mb-6">
              Mencetak generasi penerus bangsa yang berakhlak mulia, cerdas, dan peduli lingkungan.
            </p>
            <div className="flex space-x-4">
              <a href="#" className="hover:text-emerald-500 transition-colors"><Facebook className="w-5 h-5" /></a>
              <a href="#" className="hover:text-emerald-500 transition-colors"><Instagram className="w-5 h-5" /></a>
              <a href="#" className="hover:text-emerald-500 transition-colors"><Twitter className="w-5 h-5" /></a>
            </div>
          </div>

          {/* Quick Links */}
          <div>
            <h4 className="text-white font-semibold mb-4 uppercase text-sm tracking-wider">Tautan Cepat</h4>
            <ul className="space-y-2 text-sm">
              <li><a href="#beranda" className="hover:text-emerald-500 transition-colors">Beranda</a></li>
              <li><a href="#tentang" className="hover:text-emerald-500 transition-colors">Profil Sekolah</a></li>
              <li><a href="#akademik" className="hover:text-emerald-500 transition-colors">Program Studi</a></li>
              <li><a href="#galeri" className="hover:text-emerald-500 transition-colors">Galeri</a></li>
            </ul>
          </div>

          {/* Contact Info */}
          <div className="col-span-1 md:col-span-2">
            <h4 className="text-white font-semibold mb-4 uppercase text-sm tracking-wider">Hubungi Kami</h4>
            <div className="space-y-4 text-sm">
              <div className="flex items-start">
                <MapPin className="w-5 h-5 mr-3 text-emerald-500 flex-shrink-0 mt-0.5" />
                <span>{SCHOOL_ADDRESS}</span>
              </div>
              <div className="flex items-center">
                <Phone className="w-5 h-5 mr-3 text-emerald-500 flex-shrink-0" />
                <span>{SCHOOL_PHONE}</span>
              </div>
              <div className="flex items-center">
                <Mail className="w-5 h-5 mr-3 text-emerald-500 flex-shrink-0" />
                <span>{SCHOOL_EMAIL}</span>
              </div>
            </div>
            
            {/* Newsletter Mockup */}
            <div className="mt-6">
                <h5 className="text-white text-sm font-medium mb-2">Dapatkan Berita Terbaru</h5>
                <div className="flex">
                    <input type="email" placeholder="Email Anda" className="bg-gray-800 text-white px-4 py-2 rounded-l-md w-full focus:outline-none focus:ring-1 focus:ring-emerald-500 text-sm" />
                    <button className="bg-emerald-600 px-4 py-2 rounded-r-md text-white hover:bg-emerald-700 text-sm font-medium">Langganan</button>
                </div>
            </div>
          </div>
        </div>

        <div className="border-t border-gray-800 pt-8 text-center text-sm">
          <p>&copy; {new Date().getFullYear()} {SCHOOL_NAME}. All rights reserved.</p>
        </div>
      </div>
    </footer>
  );
};

export default Footer;

import React from 'react';
import { motion } from 'framer-motion';
import { ArrowRight, Leaf, GraduationCap } from 'lucide-react';

const Hero: React.FC = () => {
  return (
    <section id="beranda" className="relative h-screen flex items-center justify-center overflow-hidden bg-emerald-50">
      {/* Background Elements */}
      <div className="absolute inset-0 w-full h-full z-0">
        <div className="absolute top-[-10%] right-[-5%] w-96 h-96 bg-emerald-200 rounded-full blur-3xl opacity-50 animate-pulse"></div>
        <div className="absolute bottom-[-10%] left-[-5%] w-96 h-96 bg-green-200 rounded-full blur-3xl opacity-50 animate-pulse"></div>
      </div>
      
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 z-10 grid grid-cols-1 md:grid-cols-2 gap-12 items-center">
        <motion.div
          initial={{ opacity: 0, x: -50 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ duration: 0.8, ease: "easeOut" }}
        >
          <div className="inline-flex items-center px-3 py-1 rounded-full bg-emerald-100 text-emerald-800 text-sm font-medium mb-6">
            <Leaf className="w-4 h-4 mr-2" /> Sekolah Adiwiyata Mandiri
          </div>
          <h1 className="text-5xl md:text-6xl font-bold text-gray-900 mb-6 leading-tight">
            Membangun Generasi <span className="text-emerald-600">Cerdas</span> & <span className="text-emerald-600">Berkarakter</span>
          </h1>
          <p className="text-lg text-gray-600 mb-8 leading-relaxed">
            Bergabunglah bersama SMA Sandikasih. Kami memadukan keunggulan akademik dengan nilai-nilai kelestarian lingkungan dan teknologi masa depan.
          </p>
          <div className="flex flex-col sm:flex-row gap-4">
            <a href="#kontak" className="flex items-center justify-center px-8 py-4 bg-emerald-600 text-white rounded-full font-semibold hover:bg-emerald-700 transition-all shadow-lg hover:shadow-emerald-300/50 group">
              Daftar Siswa Baru
              <ArrowRight className="ml-2 w-5 h-5 group-hover:translate-x-1 transition-transform" />
            </a>
            <a href="#tentang" className="flex items-center justify-center px-8 py-4 bg-white text-emerald-900 border border-emerald-200 rounded-full font-semibold hover:bg-emerald-50 transition-all">
              Pelajari Lebih Lanjut
            </a>
          </div>
        </motion.div>

        <motion.div
          initial={{ opacity: 0, scale: 0.8 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 0.8, delay: 0.2 }}
          className="relative"
        >
            <div className="relative rounded-2xl overflow-hidden shadow-2xl border-4 border-white">
                <img 
                    src="https://picsum.photos/800/600?random=10" 
                    alt="Siswa Belajar" 
                    className="w-full h-full object-cover transform hover:scale-105 transition-transform duration-700"
                />
                <div className="absolute inset-0 bg-gradient-to-t from-emerald-900/40 to-transparent"></div>
            </div>
            
            {/* Floating Card */}
            <motion.div 
                initial={{ y: 20, opacity: 0 }}
                animate={{ y: 0, opacity: 1 }}
                transition={{ delay: 1, duration: 0.5 }}
                className="absolute -bottom-6 -left-6 bg-white p-4 rounded-xl shadow-xl max-w-xs hidden md:block"
            >
                <div className="flex items-center space-x-3">
                    <div className="bg-emerald-100 p-2 rounded-full">
                        <GraduationCap className="text-emerald-600 w-6 h-6" />
                    </div>
                    <div>
                        <p className="text-xs text-gray-500">Total Lulusan</p>
                        <p className="text-xl font-bold text-gray-900">5,000+</p>
                    </div>
                </div>
            </motion.div>
        </motion.div>
      </div>
    </section>
  );
};

export default Hero;
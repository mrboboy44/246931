import React from 'react';
import { motion } from 'framer-motion';

const Gallery: React.FC = () => {
  const images = [20, 21, 22, 23, 24, 25]; // Random seeds for picsum

  return (
    <section id="galeri" className="py-20 bg-white">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center mb-12">
          <h2 className="text-3xl font-bold text-gray-900">Galeri Kegiatan</h2>
          <p className="mt-4 text-gray-500">Momen-momen berharga di SMA Sandikasih</p>
        </div>

        <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
          {images.map((seed, idx) => (
            <motion.div
              key={seed}
              initial={{ opacity: 0, scale: 0.9 }}
              whileInView={{ opacity: 1, scale: 1 }}
              viewport={{ once: true }}
              transition={{ duration: 0.4, delay: idx * 0.1 }}
              className={`relative overflow-hidden rounded-xl shadow-md group ${idx === 0 ? 'col-span-2 row-span-2' : ''}`}
            >
              <img 
                src={`https://picsum.photos/800/800?random=${seed}`} 
                alt={`Kegiatan ${idx + 1}`} 
                className="w-full h-full object-cover transform group-hover:scale-110 transition-transform duration-700 ease-in-out"
              />
              <div className="absolute inset-0 bg-black/30 opacity-0 group-hover:opacity-100 transition-opacity duration-300 flex items-end p-6">
                <span className="text-white font-medium">Kegiatan Sekolah {idx + 1}</span>
              </div>
            </motion.div>
          ))}
        </div>
      </div>
    </section>
  );
};

export default Gallery;

import React from 'react';
import { motion } from 'framer-motion';
import { Target, Heart, Lightbulb } from 'lucide-react';

const About: React.FC = () => {
  const features = [
    { icon: Target, title: "Visi", desc: "Menjadi sekolah unggulan berwawasan lingkungan." },
    { icon: Heart, title: "Nilai", desc: "Integritas, Peduli, dan Inovatif." },
    { icon: Lightbulb, title: "Misi", desc: "Mengembangkan potensi siswa secara holistik." }
  ];

  return (
    <section id="tentang" className="py-20 bg-white">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center mb-16">
          <h2 className="text-emerald-600 font-semibold tracking-wide uppercase">Tentang Kami</h2>
          <p className="mt-2 text-3xl leading-8 font-extrabold tracking-tight text-gray-900 sm:text-4xl">
            Lebih dari Sekedar Sekolah
          </p>
          <p className="mt-4 max-w-2xl text-xl text-gray-500 mx-auto">
            SMA Sandikasih berkomitmen menciptakan lingkungan belajar yang aman, nyaman, dan menantang bagi setiap siswa.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {features.map((feature, index) => (
            <motion.div
              key={feature.title}
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              transition={{ duration: 0.5, delay: index * 0.2 }}
              className="p-8 bg-emerald-50 rounded-2xl hover:shadow-lg transition-shadow cursor-pointer group"
            >
              <div className="w-12 h-12 bg-emerald-600 rounded-lg flex items-center justify-center mb-6 group-hover:scale-110 transition-transform">
                <feature.icon className="w-6 h-6 text-white" />
              </div>
              <h3 className="text-xl font-bold text-gray-900 mb-3">{feature.title}</h3>
              <p className="text-gray-600">{feature.desc}</p>
            </motion.div>
          ))}
        </div>

        <div className="mt-20 flex flex-col lg:flex-row items-center gap-12">
            <div className="lg:w-1/2">
                <img 
                    src="https://picsum.photos/600/400?random=15" 
                    alt="Kepala Sekolah" 
                    className="rounded-2xl shadow-xl w-full"
                />
            </div>
            <div className="lg:w-1/2">
                <h3 className="text-2xl font-bold text-gray-900 mb-4">Sambutan Kepala Sekolah</h3>
                <p className="text-gray-600 mb-6 leading-relaxed">
                    "Pendidikan bukan hanya tentang mengisi wadah, tetapi menyalakan api. Di SMA Sandikasih, kami percaya setiap anak memiliki cahaya unik yang siap bersinar. Kami menyediakan 'tanah' yang subur agar benih-benih unggul ini dapat tumbuh menjadi pohon yang kuat dan bermanfaat bagi sesama."
                </p>
                <div>
                    <p className="font-bold text-gray-900">Dr. Irwan Gunawan, M.Pd</p>
                    <p className="text-emerald-600">Kepala Sekolah</p>
                </div>
            </div>
        </div>
      </div>
    </section>
  );
};

export default About;

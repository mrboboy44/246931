import React from 'react';
import { motion } from 'framer-motion';
import { PROGRAMS } from '../constants';
import { Atom, Globe, BookOpen } from 'lucide-react';

const iconMap: Record<string, React.ElementType> = {
  'atom': Atom,
  'globe': Globe,
  'book': BookOpen,
};

const Programs: React.FC = () => {
  return (
    <section id="akademik" className="py-20 bg-emerald-900 text-white">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex flex-col md:flex-row justify-between items-end mb-12">
            <div className="mb-6 md:mb-0">
                <h2 className="text-emerald-400 font-semibold tracking-wide uppercase mb-2">Akademik</h2>
                <h3 className="text-3xl md:text-4xl font-bold">Program Unggulan</h3>
            </div>
            <p className="max-w-md text-emerald-200 text-lg">
                Kurikulum komprehensif yang dirancang untuk persiapan perguruan tinggi dan dunia profesional.
            </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {PROGRAMS.map((program, index) => {
            const Icon = iconMap[program.icon] || BookOpen;
            return (
              <motion.div
                key={program.title}
                initial={{ opacity: 0, y: 30 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true }}
                transition={{ duration: 0.5, delay: index * 0.1 }}
                className="bg-emerald-800/50 backdrop-blur-sm p-8 rounded-2xl border border-emerald-700 hover:bg-emerald-800 transition-colors"
              >
                <div className="w-14 h-14 bg-emerald-500/20 rounded-full flex items-center justify-center mb-6">
                  <Icon className="w-7 h-7 text-emerald-300" />
                </div>
                <h4 className="text-xl font-bold mb-4">{program.title}</h4>
                <p className="text-emerald-200 leading-relaxed">{program.description}</p>
              </motion.div>
            );
          })}
        </div>
      </div>
    </section>
  );
};

export default Programs;

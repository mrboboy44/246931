import React from 'react';
import Navbar from './components/Navbar';
import Hero from './components/Hero';
import About from './components/About';
import Programs from './components/Programs';
import Gallery from './components/Gallery';
import Footer from './components/Footer';
import ChatWidget from './components/ChatWidget';

const App: React.FC = () => {
  return (
    <div className="min-h-screen bg-white text-gray-800 font-sans antialiased selection:bg-emerald-200 selection:text-emerald-900">
      <Navbar />
      <main>
        <Hero />
        <About />
        <Programs />
        <Gallery />
      </main>
      <Footer />
      <ChatWidget />
    </div>
  );
};

export default App;

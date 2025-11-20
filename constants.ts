import { NavItem, Program, Testimonial } from './types';

export const SCHOOL_NAME = "SMA Sandikasih";
export const SCHOOL_ADDRESS = "Jl. Merpati Putih No. 88, Jakarta Selatan";
export const SCHOOL_PHONE = "+62 21 5555 8888";
export const SCHOOL_EMAIL = "info@smasandikasih.sch.id";

export const NAV_ITEMS: NavItem[] = [
  { label: 'Beranda', href: '#beranda' },
  { label: 'Tentang Kami', href: '#tentang' },
  { label: 'Akademik', href: '#akademik' },
  { label: 'Galeri', href: '#galeri' },
  { label: 'Kontak', href: '#kontak' },
];

export const PROGRAMS: Program[] = [
  {
    title: 'MIPA (Matematika & IPA)',
    description: 'Fokus pada penguasaan sains dan teknologi dengan laboratorium modern dan kurikulum berbasis riset.',
    icon: 'atom'
  },
  {
    title: 'IPS (Ilmu Pengetahuan Sosial)',
    description: 'Membentuk pemimpin masa depan dengan pemahaman mendalam tentang ekonomi, sosiologi, dan geografi.',
    icon: 'globe'
  },
  {
    title: 'Bahasa & Budaya',
    description: 'Mengeksplorasi sastra dan bahasa asing (Inggris, Jepang, Jerman) untuk komunikasi global.',
    icon: 'book'
  }
];

export const TESTIMONIALS: Testimonial[] = [
  {
    id: 1,
    name: "Budi Santoso",
    role: "Alumni 2020",
    content: "SMA Sandikasih membentuk karakter saya menjadi disiplin dan mencintai lingkungan. Fasilitasnya luar biasa!",
    image: "https://picsum.photos/100/100?random=1"
  },
  {
    id: 2,
    name: "Siti Aminah",
    role: "Orang Tua Murid",
    content: "Sangat tenang menyekolahkan anak di sini. Guru-gurunya sangat perhatian dan lingkungannya asri.",
    image: "https://picsum.photos/100/100?random=2"
  },
  {
    id: 3,
    name: "Michael Tan",
    role: "Ketua OSIS 2023",
    content: "Organisasi di SMA Sandikasih sangat didukung. Banyak ruang untuk berekspresi dan berinovasi.",
    image: "https://picsum.photos/100/100?random=3"
  }
];

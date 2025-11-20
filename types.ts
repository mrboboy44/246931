export interface NavItem {
  label: string;
  href: string;
}

export interface Program {
  title: string;
  description: string;
  icon: string;
}

export interface Testimonial {
  id: number;
  name: string;
  role: string;
  content: string;
  image: string;
}

export interface ChatMessage {
  id: string;
  role: 'user' | 'model';
  text: string;
  timestamp: Date;
}

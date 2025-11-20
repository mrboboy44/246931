import { GoogleGenAI } from "@google/genai";
import { SCHOOL_NAME, SCHOOL_ADDRESS, PROGRAMS } from '../constants';

const apiKey = process.env.API_KEY || '';
let aiClient: GoogleGenAI | null = null;

if (apiKey) {
  aiClient = new GoogleGenAI({ apiKey });
}

const SYSTEM_INSTRUCTION = `
Anda adalah asisten AI ramah dan membantu untuk ${SCHOOL_NAME}.
Tujuan anda adalah membantu calon siswa dan orang tua dengan informasi tentang sekolah.

Informasi Sekolah:
- Nama: ${SCHOOL_NAME}
- Alamat: ${SCHOOL_ADDRESS}
- Program Studi: ${PROGRAMS.map(p => p.title).join(', ')}.
- Nilai Inti: Hijau, Bersih, Berkarakter, Berprestasi.
- Jam Operasional: Senin-Jumat, 07:00 - 15:00.

Gunakan bahasa Indonesia yang sopan, formal namun hangat.
Jawablah pertanyaan dengan ringkas. Jika tidak tahu jawabannya, sarankan untuk menghubungi kontak sekolah secara langsung.
`;

export const generateChatResponse = async (userMessage: string): Promise<string> => {
  if (!aiClient) {
    return "Maaf, fitur chat sedang dalam perbaikan (API Key missing). Silakan hubungi kami via WhatsApp.";
  }

  try {
    const response = await aiClient.models.generateContent({
      model: 'gemini-2.5-flash',
      contents: userMessage,
      config: {
        systemInstruction: SYSTEM_INSTRUCTION,
        temperature: 0.7,
      }
    });
    
    return response.text || "Maaf, saya tidak dapat memproses permintaan Anda saat ini.";
  } catch (error) {
    console.error("Error calling Gemini API:", error);
    return "Terjadi kesalahan pada sistem. Silakan coba lagi nanti.";
  }
};

import PrimeUI from 'tailwindcss-primeui';

export default {
  darkMode: 'class',
  content: [
    './index.html',
    './src/**/*.{vue,js,ts,jsx,tsx}',
    './node_modules/primevue/**/*.{vue,js,ts,jsx,tsx}'
  ],
  theme: {
    extend: {
      fontFamily: {
        'cyber': ['Orbitron', 'sans-serif'],
        'sans': ['Orbitron', 'system-ui', 'sans-serif'],
      },
      colors: {
        // Cyberpunk palette
        'cyber-pink': '#FF006E',
        'cyber-blue': '#00F5FF',
        'cyber-purple': '#8B00FF',
        'cyber-yellow': '#FFD60A',
        'cyber-green': '#00FF41',

        // Backgrounds
        'dark-bg': '#0A0E27',
        'darker-bg': '#050814',
        'card-bg': '#151932',
        'hover-bg': '#1E2340',

        // Text
        'text-primary': '#E0E0E0',
        'text-secondary': '#A0A0A0',
        'text-muted': '#606060',

        // Accents
        'neon-border': '#00F5FF',
        'glow-pink': '#FF006E',
        'glow-blue': '#00F5FF',
      },
      boxShadow: {
        'neon-pink': '0 0 10px #FF006E, 0 0 20px #FF006E, 0 0 30px #FF006E',
        'neon-blue': '0 0 10px #00F5FF, 0 0 20px #00F5FF, 0 0 30px #00F5FF',
        'neon-purple': '0 0 10px #8B00FF, 0 0 20px #8B00FF, 0 0 30px #8B00FF',
        'neon-green': '0 0 10px #00FF41, 0 0 20px #00FF41, 0 0 30px #00FF41',
      },
      animation: {
        'pulse-glow': 'pulse-glow 2s cubic-bezier(0.4, 0, 0.6, 1) infinite',
        'flicker': 'flicker 3s linear infinite',
      },
      keyframes: {
        'pulse-glow': {
          '0%, 100%': {
            boxShadow: '0 0 10px currentColor, 0 0 20px currentColor',
            opacity: '1'
          },
          '50%': {
            boxShadow: '0 0 20px currentColor, 0 0 40px currentColor, 0 0 60px currentColor',
            opacity: '0.8'
          },
        },
        'flicker': {
          '0%, 100%': { opacity: '1' },
          '41.99%': { opacity: '1' },
          '42%': { opacity: '0.8' },
          '43%': { opacity: '1' },
          '45.99%': { opacity: '1' },
          '46%': { opacity: '0.7' },
          '46.5%': { opacity: '1' },
        },
      },
    },
  },
  plugins: [PrimeUI],
};

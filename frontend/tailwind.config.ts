import type { Config } from "tailwindcss";

const config: Config = {
  content: [
    "./pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./components/**/*.{js,ts,jsx,tsx,mdx}",
    "./app/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: "#2660a6",
        "app-bg": "#f4f4f2",
        breach: "#db0011",
        warning: "#d97706",
        safe: "#0b6b3a",
      },
    },
  },
  plugins: [],
};

export default config;

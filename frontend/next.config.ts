import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  // Allow fetching from local backend during dev
  async headers() {
    return [
      {
        source: "/api/:path*",
        headers: [{ key: "Access-Control-Allow-Origin", value: "*" }],
      },
    ];
  },
};

export default nextConfig;

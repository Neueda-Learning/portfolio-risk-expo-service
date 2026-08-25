# Build stage
FROM node:20-alpine AS builder

WORKDIR /app

# Accept build argument for API URL (default fallback)
ARG NEXT_PUBLIC_API_URL=http://localhost:8080
ENV NEXT_PUBLIC_API_URL=$NEXT_PUBLIC_API_URL

# Accept build argument for backend URL (for server-side requests)
ARG BACKEND_URL=http://backend:8080
ENV BACKEND_URL=$BACKEND_URL

# Copy package files
COPY frontend/package*.json ./

# Install dependencies
RUN npm ci

# Copy application code
COPY frontend/ .

# Build Next.js application with environment variable
RUN npm run build

# Production stage
FROM node:20-alpine

WORKDIR /app

# Accept runtime environment variable for backend URL
ARG BACKEND_URL=http://backend:8080
ENV BACKEND_URL=$BACKEND_URL

# Install dumb-init to handle signals properly
RUN apk add --no-cache dumb-init

# Copy package files
COPY --from=builder /app/package*.json ./

# Install only production dependencies
RUN npm ci --only=production && npm cache clean --force

# Copy built application from builder
COPY --from=builder /app/.next ./.next

# Create non-root user for security
RUN addgroup -g 1001 -S nodejs && \
    adduser -S nextjs -u 1001 && \
    chown -R nextjs:nodejs /app

USER nextjs

EXPOSE 3000

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD node -e "require('http').get('http://localhost:3000', (r) => {if (r.statusCode !== 200) throw new Error(r.statusCode)})"

ENTRYPOINT ["dumb-init", "--"]
CMD ["npm", "start"]

FROM node:20-alpine

WORKDIR /app

# 先裝依賴（加速快取）
COPY package*.json ./
RUN npm ci

# 再 copy 其他檔案
COPY . .

EXPOSE 3000

# Vite 要綁 0.0.0.0 才能被外部連到
CMD ["npm", "run", "dev", "--", "--host", "0.0.0.0", "--port", "3000"]

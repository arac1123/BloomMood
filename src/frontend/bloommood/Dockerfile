FROM node:20-alpine
WORKDIR /app

COPY package*.json ./
RUN npm ci
# 這樣有點太簡陋了
COPY . .

EXPOSE 3000
CMD ["npm","run","dev","--","--host","0.0.0.0","--port","3000"]

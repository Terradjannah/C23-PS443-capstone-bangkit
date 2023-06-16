FROM node:18

WORKDIR /app
COPY package.json /app
COPY . /app
RUN npm install
EXPOSE 8080
CMD ["node", "index.js"]
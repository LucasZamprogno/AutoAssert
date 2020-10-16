FROM node:12

ARG REPO
ENV REPO=$REPO
ARG RUN_COM="npm run test"
ENV RUN_COM=$RUN_COM
ARG BUILD_COM="npm install"
ENV BUILD_COM=$BUILD_COM
#ARG disappears at runtime

RUN apt-get update && apt-get install nano

COPY ./runner /app/main-runner
RUN npm install -g gulp grunt lerna
# May be needed by some repos

WORKDIR /app

RUN git clone $REPO main
COPY ./build_com.sh /app/main

WORKDIR /app/main

RUN echo $BUILD_COM >> build_com.sh
RUN chmod +x build_com.sh
RUN ./build_com.sh

WORKDIR /app/main-runner

RUN npm install

CMD node ./main.js

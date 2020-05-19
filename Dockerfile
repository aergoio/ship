FROM openjdk:8

# This isn't more efficient because ./build.sh will still download gradle
#FROM gradle:jdk8

RUN apt-get update
RUN apt-get -y install nodejs npm
RUN npm install npm@latest -g

WORKDIR /ship
COPY . .

ENV VERSION 1.0-RC1

# Run in a single container to reuse gradle daemon
RUN ./build.sh clean \
&& ./build.sh deps \
&& ./build.sh npm \
&& ./build.sh gradle \
&& ./build.sh assemble

ENV PATH /ship/assembly/build/distributions/ship-$VERSION/bin:$PATH

CMD ["ship"]

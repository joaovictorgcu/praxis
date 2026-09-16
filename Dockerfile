# Imagem do praxis para plataformas de container (Render, Koyeb, Fly, Cloud Run).
# Duas etapas: a primeira compila com o Maven Wrapper do repositorio, a segunda
# carrega so o JRE e o jar - a imagem final nao leva Maven nem codigo-fonte.

FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /praxis

# Dependencias primeiro: enquanto o pom nao muda, esta camada vem do cache e o
# build nao baixa o repositorio inteiro de novo a cada push.
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

COPY src ./src
RUN mvn -q -B -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /praxis

# Usuario sem privilegio: nada aqui precisa de root em execucao.
RUN useradd --system --create-home praxis
USER praxis

COPY --from=build /praxis/target/*.jar praxis.jar

# MaxRAMPercentage no lugar de -Xmx: a heap acompanha o limite do container,
# que nos planos gratuitos costuma ser 512 MB.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC -Dfile.encoding=UTF-8"
ENV SPRING_PROFILES_ACTIVE=prod

# A plataforma escolhe a porta e a injeta em PORT; 8080 e so o padrao local.
EXPOSE 8080
CMD ["sh", "-c", "exec java $JAVA_OPTS -jar praxis.jar"]

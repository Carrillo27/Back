# Backend de Papeleria Sol para Render

# 1. Compilar el WAR
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY . .
RUN mvn -B -q package

# 2. Servidor GlassFish Web Profile
FROM eclipse-temurin:21-jdk

RUN apt-get update \
 && apt-get install -y --no-install-recommends curl unzip \
 && rm -rf /var/lib/apt/lists/*

RUN curl -fsSL -o /tmp/gf.zip https://repo1.maven.org/maven2/org/glassfish/main/distributions/web/8.0.0/web-8.0.0.zip \
 && unzip -q /tmp/gf.zip -d /opt \
 && rm /tmp/gf.zip \
 && sed -i 's/-Xmx512m/-Xmx192m/g' /opt/glassfish8/glassfish/domains/domain1/config/domain.xml \
 && useradd --system --home /opt/glassfish8 glassfish \
 && chown -R glassfish /opt/glassfish8

COPY --from=build --chown=glassfish /src/extra/target/papeleria-api.war /opt/glassfish8/glassfish/domains/domain1/autodeploy/

USER glassfish
EXPOSE 8080

CMD ["/opt/glassfish8/glassfish/bin/asadmin", "start-domain", "--verbose", "domain1"]
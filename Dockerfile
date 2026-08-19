# 1. Base Image - Naya aur supported Java 17 image
FROM eclipse-temurin:17-jdk-jammy

# 2. Kotlin aur zaroori tools install karo
RUN apt-get update && apt-get install -y wget unzip
RUN wget https://github.com/JetBrains/kotlin/releases/download/v1.9.22/kotlin-compiler-1.9.22.zip
RUN unzip kotlin-compiler-1.9.22.zip -d /opt/
ENV PATH $PATH:/opt/kotlinc/bin

# 3. Cloud machine mein ek folder banao
WORKDIR /app

# 4. Apna saara code us folder mein copy karo
COPY . /app

# 5. SQLite (Database) ki jar file download karo
RUN wget https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.42.0.0/sqlite-jdbc-3.42.0.0.jar

# 6. Kotlin code ko compile karo
RUN kotlinc server.kt -include-runtime -d server.jar

# 7. Port 8080 open karo jahan website chalegi
EXPOSE 8080

# 8. Server ko Start karo!
CMD ["java", "-cp", "sqlite-jdbc-3.42.0.0.jar:server.jar", "ServerKt"]
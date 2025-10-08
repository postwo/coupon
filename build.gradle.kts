val bootJar: org.springframework.boot.gradle.tasks.bundling.BootJar by tasks

bootJar.enabled = false

plugins {
	java
	id("org.springframework.boot") version "3.3.4"
	id("io.spring.dependency-management") version "1.1.3"
}

// Spring Boot의 의존성 관리 플러그인이 참조하는 Netty 버전을 4.1.118.Final로 오버라이드합니다.
extra["netty.version"] = "4.1.125.Final" // 취약점 패치 버전


group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

// subprojects는 api,consumer,core 에대해서 밑 라이브러리들을 설정한다는 의미이다
subprojects {
	apply(plugin = "java")
	apply(plugin = "io.spring.dependency-management")
	apply(plugin = "org.springframework.boot")

	repositories {
		mavenCentral()
	}

	dependencies {
		implementation("org.springframework.boot:spring-boot-starter-data-jpa")
		compileOnly("org.projectlombok:lombok")
		annotationProcessor("org.projectlombok:lombok")
		runtimeOnly("com.h2database:h2")
		runtimeOnly("com.mysql:mysql-connector-j")
		implementation("org.springframework.boot:spring-boot-starter")

		implementation("com.querydsl:querydsl-jpa:5.0.0:jakarta")
		annotationProcessor("com.querydsl:querydsl-apt:5.0.0:jakarta")
		annotationProcessor("jakarta.annotation:jakarta.annotation-api")
		annotationProcessor("jakarta.persistence:jakarta.persistence-api")

		implementation("org.springframework.boot:spring-boot-starter-actuator")
		implementation("io.micrometer:micrometer-registry-prometheus")

		implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")

		testImplementation("org.springframework.boot:spring-boot-starter-test")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
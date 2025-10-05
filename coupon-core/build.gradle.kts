val bootJar: org.springframework.boot.gradle.tasks.bundling.BootJar by tasks

bootJar.enabled = false

repositories {
	mavenCentral()
}
dependencies {
	// implementation 'org.springframework.boot:spring-boot-starter-web' 라이브러리를 사용하면 밑 두개의 라이브러리는 설정할필요없다
	// 이유는 내장 되어있기때문에

	//Java 8 날짜/시간 지원을 위한 핵심 모듈입니다. 이 모듈 안에 LocalDateTimeSerializer 같은 클래스들이 들어있습니다. 이 모듈이 있어야 Jackson이 java.time 객체를 인식하고 처리할 수 있습니다.
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
	//Jackson의 핵심 데이터 바인딩 기능을 제공합니다. JSON을 Java 객체로, Java 객체를 JSON으로 변환하는 ObjectMapper 클래스가 이
	// 라이브러리에 포함되어 있습니다. 이 라이브러리가 없으면 jackson-datatype-jsr310 모듈 자체를 사용할 수 없습니다.
	implementation("com.fasterxml.jackson.core:jackson-databind")
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.redisson:redisson-spring-boot-starter:3.39.0")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
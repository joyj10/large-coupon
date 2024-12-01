dependencies {
    implementation(project(":coupon-core"))
    implementation("org.springframework.boot:spring-boot-starter-web")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

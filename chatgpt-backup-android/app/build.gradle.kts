plugins { id("com.android.application") }
android {
 namespace = "com.thedoc.chatgptbackup"
 compileSdk = 35
 defaultConfig {
  applicationId = "com.thedoc.chatgptbackup"
  minSdk = 26
  targetSdk = 35
  versionCode = 3
  versionName = "3.0"
  testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
 }
}
dependencies {
 implementation("androidx.core:core:1.15.0")
 androidTestImplementation("androidx.test:core:1.6.1")
 androidTestImplementation("androidx.test.ext:junit:1.2.1")
 androidTestImplementation("androidx.test:runner:1.6.2")
}
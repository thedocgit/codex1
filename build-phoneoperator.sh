#!/usr/bin/env bash
set -euo pipefail

ROOT="$PWD"
WORK="$ROOT/.phoneoperator-build"
SDK="$WORK/android-sdk"
JDK="$WORK/jdk17"
GRADLE="$WORK/gradle"
mkdir -p "$WORK" "$SDK/cmdline-tools" "$JDK" "$GRADLE" "$ROOT/public"

echo "== Reconstructing PhoneOperator source =="
cat PhoneOperator/package/part*.b64 | tr -d '\n\r ' | base64 -d > "$WORK/PhoneOperator_source_complete.zip"
echo "602fa3a8ea3b1ca1e66493cbe3285890ec289cd6e9a153e10ad50da49d7a25ea  $WORK/PhoneOperator_source_complete.zip" | sha256sum -c -
unzip -q "$WORK/PhoneOperator_source_complete.zip" -d "$WORK/src"
sed -i 's|androidx.core:core-ktx:1.19.0|androidx.core:core-ktx:1.17.0|' "$WORK/src/PhoneOperator/app/build.gradle.kts"

echo "== Installing JDK 17 =="
curl -fL --retry 3 --retry-delay 2   https://corretto.aws/downloads/latest/amazon-corretto-17-x64-linux-jdk.tar.gz   -o "$WORK/jdk17.tar.gz"
tar -xzf "$WORK/jdk17.tar.gz" -C "$JDK" --strip-components=1
export JAVA_HOME="$JDK"
export PATH="$JAVA_HOME/bin:$PATH"
java -version

echo "== Installing Android command-line tools =="
curl -fL --retry 3 --retry-delay 2   https://dl.google.com/android/repository/commandlinetools-linux-15859902_latest.zip   -o "$WORK/cmdline-tools.zip"
unzip -q "$WORK/cmdline-tools.zip" -d "$WORK/cmdline-unpack"
mkdir -p "$SDK/cmdline-tools/latest"
cp -R "$WORK/cmdline-unpack/cmdline-tools/." "$SDK/cmdline-tools/latest/"

export ANDROID_HOME="$SDK"
export ANDROID_SDK_ROOT="$SDK"
export PATH="$SDK/cmdline-tools/latest/bin:$SDK/platform-tools:$PATH"

yes | sdkmanager --licenses >/dev/null || true
sdkmanager "platform-tools" "platforms;android-36" "build-tools;36.0.0"

echo "== Installing Gradle 8.13 =="
curl -fL --retry 3 --retry-delay 2   https://services.gradle.org/distributions/gradle-8.13-bin.zip   -o "$WORK/gradle.zip"
unzip -q "$WORK/gradle.zip" -d "$GRADLE"
export PATH="$GRADLE/gradle-8.13/bin:$PATH"
gradle --version

echo "== Building APK =="
cd "$WORK/src/PhoneOperator"
gradle :app:assembleDebug --no-daemon --stacktrace

APK="app/build/outputs/apk/debug/app-debug.apk"
test -s "$APK"
cp "$APK" "$ROOT/public/PhoneOperator-debug.apk"
sha256sum "$ROOT/public/PhoneOperator-debug.apk" | tee "$ROOT/public/PhoneOperator-debug.apk.sha256"

cat > "$ROOT/public/index.html" <<'HTML'
<!doctype html>
<html lang="pt-BR">
<head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>PhoneOperator APK</title></head>
<body>
<h1>PhoneOperator</h1>
<p><a href="/PhoneOperator-debug.apk">Baixar APK compilado</a></p>
<p><a href="/PhoneOperator-debug.apk.sha256">SHA-256</a></p>
</body>
</html>
HTML

echo "== APK ready =="
ls -lh "$ROOT/public/PhoneOperator-debug.apk"

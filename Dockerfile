FROM ghcr.io/cirruslabs/android-sdk:36 AS build

WORKDIR /workspace

RUN curl -fL --retry 3 https://services.gradle.org/distributions/gradle-8.13-bin.zip -o /tmp/gradle.zip \
    && unzip -q /tmp/gradle.zip -d /opt/gradle \
    && rm /tmp/gradle.zip

ENV PATH="/opt/gradle/gradle-8.13/bin:${PATH}"

COPY . /workspace/repo

RUN mkdir -p /workspace/src \
    && cat /workspace/repo/PhoneOperator/package/part*.b64 | tr -d '\n\r ' | base64 -d > /workspace/PhoneOperator_source_complete.zip \
    && echo "602fa3a8ea3b1ca1e66493cbe3285890ec289cd6e9a153e10ad50da49d7a25ea  /workspace/PhoneOperator_source_complete.zip" | sha256sum -c - \
    && unzip -q /workspace/PhoneOperator_source_complete.zip -d /workspace/src \
    && sed -i 's|androidx.core:core-ktx:1.19.0|androidx.core:core-ktx:1.17.0|' /workspace/src/PhoneOperator/app/build.gradle.kts

WORKDIR /workspace/src/PhoneOperator

RUN java -version \
    && gradle --version \
    && gradle :app:assembleDebug --no-daemon --stacktrace \
    && test -s app/build/outputs/apk/debug/app-debug.apk \
    && sha256sum app/build/outputs/apk/debug/app-debug.apk > /workspace/PhoneOperator-debug.apk.sha256

FROM python:3.13-alpine

WORKDIR /srv

COPY --from=build /workspace/src/PhoneOperator/app/build/outputs/apk/debug/app-debug.apk /srv/PhoneOperator-debug.apk
COPY --from=build /workspace/PhoneOperator-debug.apk.sha256 /srv/PhoneOperator-debug.apk.sha256

RUN printf '%s\n' \
  '<!doctype html><html lang="pt-BR"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>PhoneOperator APK</title></head><body><h1>PhoneOperator</h1><p><a href="/PhoneOperator-debug.apk">Baixar APK compilado</a></p><p><a href="/PhoneOperator-debug.apk.sha256">SHA-256</a></p></body></html>' \
  > /srv/index.html

EXPOSE 8080
CMD ["sh","-c","python -m http.server ${PORT:-8080} --bind 0.0.0.0"]

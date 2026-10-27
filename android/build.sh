#!/usr/bin/env bash
# Gera public/radiosnetto.apk sem Gradle, só com as ferramentas do Android SDK.
# Uso: ANDROID_HOME=/caminho/do/sdk KEYSTORE=/caminho/app.jks KEYSTORE_PASS=... ./android/build.sh
set -euo pipefail
cd "$(dirname "$0")"
BT="$ANDROID_HOME/build-tools/35.0.0"
JAR="$ANDROID_HOME/platforms/android-35/android.jar"
OUT="$(mktemp -d)"

"$BT/aapt2" compile --dir res -o "$OUT/res.zip"
"$BT/aapt2" link -I "$JAR" --manifest AndroidManifest.xml -o "$OUT/base.apk" "$OUT/res.zip"
mkdir -p "$OUT/classes"
javac -source 11 -target 11 -nowarn -classpath "$JAR" -d "$OUT/classes" $(find src -name '*.java')
"$BT/d8" --min-api 21 --lib "$JAR" --output "$OUT" $(find "$OUT/classes" -name '*.class')
(cd "$OUT" && zip -qj base.apk classes.dex)
"$BT/zipalign" -f -p 4 "$OUT/base.apk" "$OUT/aligned.apk"
"$BT/apksigner" sign --ks "$KEYSTORE" --ks-pass env:KEYSTORE_PASS --out ../public/radiosnetto.apk "$OUT/aligned.apk"
rm -rf "$OUT"
echo "APK gerado em public/radiosnetto.apk"

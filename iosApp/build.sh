#!/usr/bin/env bash
# Сборка HalerChat.app для iPhone на Linux через omarchy-apple-dev.
# Установка на устройство: xtool install iosApp/xtool/HalerChat.app
set -euo pipefail
OAD="${OAD:-$HOME/omarchy-apple-dev}"
root=$(dirname "$(readlink -f "$0")")/..
out="$root/composeApp/build/omarchy-kmp/ios_arm64"

"$OAD/kmp/build-framework.sh" "$root" --module :composeApp --dynamic --xcframework
# build-framework.sh берёт ресурсы только самого composeApp; ресурсы feature-модулей собирает этот таск
"$root/gradlew" -p "$root" -q :composeApp:iosArm64AggregateResources

cd "$root/iosApp"
rm -rf Frameworks && mkdir Frameworks && cp -a "$out/ComposeApp.xcframework" Frameworks/
CONFIGURATION=debug "$OAD/ship.sh" --device
rm -rf xtool/HalerChat.app/compose-resources && mkdir xtool/HalerChat.app/compose-resources
cp -a "$root/composeApp/build/kotlin-multiplatform-resources/aggregated-resources/iosArm64/composeResources" xtool/HalerChat.app/compose-resources/

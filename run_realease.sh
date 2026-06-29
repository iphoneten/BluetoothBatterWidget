#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

APP_ID="com.example.bluetoothbatterwidget"
MAIN_ACTIVITY="${APP_ID}/.MainActivity"
GRADLE_TASK="${GRADLE_TASK:-assembleRelease}"
APK_PATH="${APK_PATH:-app/build/outputs/apk/release/app-release.apk}"
INSTALL_APK=1
LAUNCH_APP=1
FRESH_INSTALL=0

KEYSTORE_FILE="${BBW_RELEASE_STORE_FILE:-/Users/andyfan/Desktop/pythonPro/cloudflareImages/ImageDriverMobile/android/keystore/360度网盘.jks}"
KEYSTORE_PASSWORD="${BBW_RELEASE_STORE_PASSWORD:-qwasz123}"
KEY_ALIAS="${BBW_RELEASE_KEY_ALIAS:-key0}"
KEY_PASSWORD="${BBW_RELEASE_KEY_PASSWORD:-qwasz123}"

for arg in "$@"; do
  case "$arg" in
    --no-install)
      INSTALL_APK=0
      LAUNCH_APP=0
      ;;
    --no-launch)
      LAUNCH_APP=0
      ;;
    --fresh-install)
      FRESH_INSTALL=1
      ;;
    --help|-h)
      echo "Usage: ./run_realease.sh [--no-install] [--no-launch] [--fresh-install]"
      echo
      echo "Options:"
      echo "  --no-install     Build release APK only"
      echo "  --no-launch      Install only, do not launch the app"
      echo "  --fresh-install  Uninstall first so launcher reloads widget provider dimensions"
      echo
      echo "Environment:"
      echo "  GRADLE_TASK                 Gradle task to run. Default: assembleRelease"
      echo "  APK_PATH                    APK path to install. Default: app/build/outputs/apk/release/app-release.apk"
      echo "  GRADLE_BIN                  Optional Gradle executable. Default: auto-detect installed Gradle, then ./gradlew"
      echo "  GRADLE_USER_HOME            Optional Gradle cache dir. Default: project .gradle-user-home"
      echo "  JAVA_HOME                   Optional JDK path. Default: auto-detect JDK 17 on macOS"
      echo "  BBW_RELEASE_STORE_FILE      Release keystore path"
      echo "  BBW_RELEASE_STORE_PASSWORD  Release keystore password"
      echo "  BBW_RELEASE_KEY_ALIAS       Release key alias"
      echo "  BBW_RELEASE_KEY_PASSWORD    Release key password"
      exit 0
      ;;
    *)
      echo "Unknown argument: $arg" >&2
      echo "Run ./run_realease.sh --help for usage." >&2
      exit 1
      ;;
  esac
done

read_sdk_dir() {
  if [[ -n "${ANDROID_HOME:-}" ]]; then
    echo "$ANDROID_HOME"
    return
  fi

  if [[ -n "${ANDROID_SDK_ROOT:-}" ]]; then
    echo "$ANDROID_SDK_ROOT"
    return
  fi

  if [[ -f "$ROOT_DIR/local.properties" ]]; then
    awk -F= '/^sdk.dir=/ {print substr($0, index($0, "=") + 1); exit}' "$ROOT_DIR/local.properties" \
      | sed 's/\\ / /g'
  fi
}

select_java_home() {
  if [[ -n "${JAVA_HOME:-}" ]]; then
    echo "$JAVA_HOME"
    return
  fi

  if [[ -x /usr/libexec/java_home ]]; then
    /usr/libexec/java_home -v 17 2>/dev/null || true
  fi
}

wrapper_gradle_version() {
  awk -F'gradle-' '/^distributionUrl=/ {
    split($2, parts, "-")
    print parts[1]
    exit
  }' "$ROOT_DIR/gradle/wrapper/gradle-wrapper.properties"
}

find_gradle_version() {
  local version="$1"
  local candidates=("$HOME"/.gradle/wrapper/dists/gradle-"$version"-*/*/gradle-"$version"/bin/gradle)
  local candidate

  for candidate in "${candidates[@]}"; do
    if [[ -x "$candidate" ]]; then
      echo "$candidate"
      return 0
    fi
  done

  return 1
}

select_gradle_bin() {
  local wrapper_version
  local candidate
  local version

  if [[ -n "${GRADLE_BIN:-}" ]]; then
    echo "$GRADLE_BIN"
    return
  fi

  if command -v gradle >/dev/null 2>&1; then
    command -v gradle
    return
  fi

  wrapper_version="$(wrapper_gradle_version)"
  if [[ -n "$wrapper_version" ]]; then
    candidate="$(find_gradle_version "$wrapper_version" || true)"
    if [[ -n "$candidate" ]]; then
      echo "$candidate"
      return
    fi
  fi

  for version in 8.9 8.8 8.7 8.6 8.5 8.4 8.3 8.2 8.10 8.11 8.12 8.13; do
    candidate="$(find_gradle_version "$version" || true)"
    if [[ -n "$candidate" ]]; then
      echo "$candidate"
      return
    fi
  done

  echo "$ROOT_DIR/gradlew"
}

if [[ ! -f "$KEYSTORE_FILE" ]]; then
  echo "Release keystore not found: $KEYSTORE_FILE" >&2
  exit 1
fi

DETECTED_JAVA_HOME="$(select_java_home)"
if [[ -n "$DETECTED_JAVA_HOME" ]]; then
  export JAVA_HOME="$DETECTED_JAVA_HOME"
  export PATH="$JAVA_HOME/bin:$PATH"
else
  echo "JDK 17 was not found. Android Gradle Plugin 8.2 requires JDK 17 or newer." >&2
  echo "Install JDK 17 or set JAVA_HOME before running this script." >&2
  exit 1
fi

export GRADLE_USER_HOME="${GRADLE_USER_HOME:-$ROOT_DIR/.gradle-user-home}"

GRADLE_BIN="$(select_gradle_bin)"
if [[ ! -x "$GRADLE_BIN" ]]; then
  echo "Gradle executable not found or not executable: $GRADLE_BIN" >&2
  exit 1
fi

export BBW_RELEASE_STORE_FILE="$KEYSTORE_FILE"
export BBW_RELEASE_STORE_PASSWORD="$KEYSTORE_PASSWORD"
export BBW_RELEASE_KEY_ALIAS="$KEY_ALIAS"
export BBW_RELEASE_KEY_PASSWORD="$KEY_PASSWORD"

echo "==> Building signed release APK: $GRADLE_TASK"
echo "==> Using Java: $JAVA_HOME"
echo "==> Using Gradle: $GRADLE_BIN"
echo "==> Using Gradle user home: $GRADLE_USER_HOME"
echo "==> Using keystore: $KEYSTORE_FILE"
if [[ "$GRADLE_BIN" == "$ROOT_DIR/gradlew" ]]; then
  echo "No installed Gradle 8.x was found. ./gradlew may download Gradle from services.gradle.org."
fi
"$GRADLE_BIN" --no-daemon "$GRADLE_TASK"

if [[ ! -f "$APK_PATH" ]]; then
  echo "APK not found: $APK_PATH" >&2
  exit 1
fi

echo "==> Release APK: $ROOT_DIR/$APK_PATH"

if [[ "$INSTALL_APK" -eq 0 ]]; then
  echo "Done."
  exit 0
fi

SDK_DIR="$(read_sdk_dir)"
if [[ -z "$SDK_DIR" ]]; then
  echo "Android SDK not found. Set ANDROID_HOME or add sdk.dir to local.properties." >&2
  exit 1
fi

ADB="$SDK_DIR/platform-tools/adb"
if [[ ! -x "$ADB" ]]; then
  echo "adb not found or not executable: $ADB" >&2
  exit 1
fi

echo "==> Checking connected Android device"
"$ADB" start-server >/dev/null
DEVICE_COUNT="$("$ADB" devices | awk 'NR > 1 && $2 == "device" {count++} END {print count + 0}')"
if [[ "$DEVICE_COUNT" -eq 0 ]]; then
  echo "No authorized Android device found." >&2
  echo "Connect a phone, enable USB debugging, and accept the RSA prompt." >&2
  exit 1
fi

if [[ "$FRESH_INSTALL" -eq 1 ]]; then
  echo "==> Fresh install requested. Existing app data and widgets will be removed."
  "$ADB" uninstall "$APP_ID" >/dev/null 2>&1 || true
fi

echo "==> Installing APK: $APK_PATH"
"$ADB" install -r "$APK_PATH"

if [[ "$LAUNCH_APP" -eq 1 ]]; then
  echo "==> Launching app: $MAIN_ACTIVITY"
  "$ADB" shell am start -n "$MAIN_ACTIVITY"
fi

echo "Done."

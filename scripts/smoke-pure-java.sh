#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
javac -encoding UTF-8 -d "$TMP" \
  modules/lux-engine/src/main/java/dev/luxmods/engine/api/LuxServiceKey.java \
  modules/lux-engine/src/main/java/dev/luxmods/engine/api/LuxServiceRegistry.java \
  modules/lux-discovery/src/main/java/dev/luxmods/discovery/DiscoveryDebouncer.java \
  modules/lux-discovery/src/main/java/dev/luxmods/discovery/DiscoveryLedger.java \
  scripts/src/LuxSmokeMain.java
java -cp "$TMP" LuxSmokeMain

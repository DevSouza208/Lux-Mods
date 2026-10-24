#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
javac --release 21 -encoding UTF-8 -d "$TMP" \
  modules/lux-engine/src/main/java/dev/luxmods/engine/api/LuxServiceKey.java \
  modules/lux-engine/src/main/java/dev/luxmods/engine/api/LuxServiceRegistry.java \
  modules/lux-engine/src/main/java/dev/luxmods/engine/api/LuxEventType.java \
  modules/lux-engine/src/main/java/dev/luxmods/engine/api/LuxEventBus.java \
  modules/lux-discovery/src/main/java/dev/luxmods/discovery/DiscoveryDebouncer.java \
  modules/lux-discovery/src/main/java/dev/luxmods/discovery/DiscoveryLedger.java \
  modules/lux-discovery/src/main/java/dev/luxmods/discovery/LuxDiscoveryEvents.java \
  modules/lux-atlas/src/main/java/dev/luxmods/atlas/CompassMath.java \
  scripts/src/LuxCompassSmoke.java \
  scripts/src/LuxSmokeMain.java \
  scripts/src/LuxEventDepthSmoke.java
java -cp "$TMP" LuxSmokeMain
java -cp "$TMP" LuxEventDepthSmoke
java -cp "$TMP" LuxCompassSmoke

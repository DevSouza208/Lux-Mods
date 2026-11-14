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
  modules/lux-engine/src/main/java/dev/luxmods/engine/api/LuxQuestTracker.java \
  modules/lux-engine/src/main/java/dev/luxmods/engine/api/LuxQuestPage.java \
  modules/lux-discovery/src/main/java/dev/luxmods/discovery/DiscoveryDebouncer.java \
  modules/lux-discovery/src/main/java/dev/luxmods/discovery/DiscoveryLedger.java \
  modules/lux-discovery/src/main/java/dev/luxmods/discovery/LuxDiscoveryEvents.java \
  modules/lux-atlas/src/main/java/dev/luxmods/atlas/CompassMath.java \
  modules/lux-atlas/src/main/java/dev/luxmods/atlas/client/AtlasViewport.java \
  modules/lux-atlas/src/main/java/dev/luxmods/atlas/client/AtlasTerrainColor.java \
  modules/lux-atlas/src/main/java/dev/luxmods/atlas/client/AtlasTileLayers.java \
  modules/lux-atlas/src/main/java/dev/luxmods/atlas/client/AtlasLevelOfDetail.java \
  modules/lux-atlas/src/main/java/dev/luxmods/atlas/client/AtlasRegionMerge.java \
  modules/lux-atlas/src/main/java/dev/luxmods/atlas/client/AtlasWriteJournal.java \
  modules/lux-dialogues/src/main/java/dev/luxmods/dialogues/NpcContractLifecycle.java \
  modules/lux-dialogues/src/main/java/dev/luxmods/dialogues/ContractInventoryPlan.java \
  modules/lux-dialogues/src/main/java/dev/luxmods/dialogues/client/ContractUiState.java \
  modules/lux-hud/src/main/java/dev/luxmods/hud/HudBarMath.java \
  modules/lux-hud/src/main/java/dev/luxmods/hud/HudPartyCodec.java \
  scripts/src/LuxNpcContractSmoke.java \
  scripts/src/LuxQuestTrackerSmoke.java \
  scripts/src/LuxQuestPageSmoke.java \
  scripts/src/LuxContractInventorySmoke.java \
  scripts/src/LuxContractUiSmoke.java \
  scripts/src/LuxHudSmoke.java \
  scripts/src/LuxCompassSmoke.java \
  scripts/src/LuxAtlasViewportSmoke.java \
  scripts/src/LuxAtlasTerrainColorSmoke.java \
  scripts/src/LuxAtlasTileLayersSmoke.java \
  scripts/src/LuxAtlasLodSmoke.java \
  scripts/src/LuxAtlasRegionMergeSmoke.java \
  scripts/src/LuxAtlasJournalSmoke.java \
  scripts/src/LuxSmokeMain.java \
  scripts/src/LuxEventDepthSmoke.java
java -cp "$TMP" LuxSmokeMain
java -cp "$TMP" LuxEventDepthSmoke
java -cp "$TMP" LuxCompassSmoke
java -cp "$TMP" LuxHudSmoke
java -cp "$TMP" LuxAtlasViewportSmoke
java -cp "$TMP" LuxAtlasLodSmoke
java -cp "$TMP" LuxAtlasRegionMergeSmoke
java -cp "$TMP" LuxAtlasJournalSmoke
java -cp "$TMP" LuxAtlasTerrainColorSmoke
java -cp "$TMP" LuxAtlasTileLayersSmoke
java -cp "$TMP" LuxNpcContractSmoke
java -cp "$TMP" LuxContractUiSmoke
java -cp "$TMP" LuxContractInventorySmoke
java -cp "$TMP" LuxQuestTrackerSmoke
java -cp "$TMP" LuxQuestPageSmoke

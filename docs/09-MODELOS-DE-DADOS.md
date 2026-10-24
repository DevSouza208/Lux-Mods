# Modelos de dados conceituais

Este documento registra chaves e invariantes para orientar implementação. Não são classes nem schemas de produção.

## Village
Village { id UUID, dimension, center, biome, name, tierAtGeneration, layoutVersion, parcels[], residents[], jobs[], foundedGameDay }. Tier permanece fixo v1.

## Parcel / Building
Parcel { id, villageId, bounds3D/mask, ownerNpcIds[], civic, blueprintVersion, expectedBlocksAndAir[], allowedRepairModes, lastInspectionDay }.
Building { id, parcelId, buildingType, bedAssignments[], jobAssignments[], state: operating|vacant|decaying|restoring, conservation, repairTickets[] }.
Never allow repair past mask; verify block entities and protections. Bounding box alone may be grande demais; usar máscara fina.

## NPC
NpcState { entityUuid, villageId?, displayName, profession, professionTier, personalitySeed, bedPos?, jobPos?, opinionByPlayer, memoryFacts, availableNeed?, needReviewDay }. Store **profession tier** separately from trading XP vanilla.

## NeedOffer
NeedOffer { offerId, npcUuid, templateId, createdDay, reviewDay, itemRequirements[], payment[], state:available|reserved|fulfilled|withdrawn, claimPolicy, claimant?, expiryDay? }.
- Re-evaluate only when gameDay >= reviewDay and offer not reserved.
- Dialogue is read-only for need state.
- Delivery consume+completion atomic, idempotency key.

## PlayerFact
PlayerFact { playerUuid, factId, scope, firstSeenGameDay, source, data }. Fatos por pessoa; reset de quests não apaga necessariamente o que o jogador de fato viu.

## Quest
QuestDefinition { questId, schemaVersion, graphNodes, edges, localizationKey, availabilityConditions, completionPolicy }.
QuestInstance { instanceId, definitionId, ownerScope, state, activatedNodeIds, factsSnapshot?, deadlineDay? }.
Quests não precisam nascer com NPC; contratos contextuais são adaptadores para instâncias.

## Backpack
BackpackState { playerUuid, schemaVersion, capacityLevel:1..10, magnetLevel:0..2, stackUpgrade:0..2, blockRefill, toolRefill, extraSlots[], featureToggles }.
Invariantes: capacidade por tabela; hotbar sempre 9; slots nativos 27 no máximo; extras persistidos; nunca armazenar ItemStack inválido de 128; upgrades persistem quando regras de morte disparam.

## World/Client preferences
ServerConfig { enabledModules, villageRules, questRefreshIntervalDays, npcCrimeRules, inventoryRules, perfBudgets, featureFlags }; ClientPrefs { hudMode, anchorPositions, fadeTimers, questWidgetMode, atlasCompassStyle, discoveryMode }. Não permitir que cliente altere configuração autoritativa.

## Versionamento
IDs estáveis e namespaces, schemaVersion em tudo, migrations testadas, backups. Campo desconhecido deve ter fallback seguro, não limpar inventários inteiros.

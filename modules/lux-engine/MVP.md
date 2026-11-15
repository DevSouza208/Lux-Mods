# Lux Engine — MVP estabilizado

**Estado do código:** base comum dos nove módulos Lux. Não adiciona gameplay, interface ou modificações de mundo.

## Responsabilidade
- Disponibilizar `LuxEngine.EVENTS` e `LuxEngine.SERVICES` para os módulos existentes.
- Registrar a configuração comum `logLoadedModules`.
- Listar apenas os mod IDs dos JARs atuais, sem tratar módulos opcionais como obrigatórios.
- Limpar os snapshots somente do cliente ao desconectar, sem modificar dados do servidor.

## Contratos preservados
- `LuxEventBus.subscribe/publish` com assinaturas removíveis e entrega síncrona.
- `LuxServiceRegistry.register/find/registeredIds` com contrato tipado.
- `LuxGuiStyle`, `LuxUpgradeRules`, `LuxUpgradeState` e classes públicas existentes: preservadas para compatibilidade; não mover pacotes neste MVP.
- O Engine **não** deve registrar tick de jogador, tecla, camada HUD, geração de estruturas nem operações de inventário.

## Módulos atualmente distribuídos
`lux_engine`, `lux_discovery`, `lux_compass`, `lux_quests`, `lux_upgrades`, `lux_hud`, `lux_menu`, `lux_borderless`, `lux_dialogues`.

`lux_atlas` foi removido. Lux Quests permanece congelado. A presença no catálogo não significa funcionalidade validada em jogo.

## Critérios de verificação
1. `lux-engine` inicia sozinho, sem referenciar outro módulo Lux opcional.
2. O catálogo retorna apenas os módulos realmente carregados.
3. Testes de catálogo, eventos e serviços executam sem regressões.
4. `assembleTestPack` compila na máquina do usuário, pelo atualizador **existente**. Sem GitHub Actions.
5. Nenhum dado de personagem, progresso, XP ou mundo é migrado ou apagado pelo Engine.

**Limite desta etapa:** revisão do Engine não corrige os bugs do Lux Compass ou da HUD. Esses serão tratados separadamente após validar o Engine.

# Lux Mods — auditoria dos sete módulos (14/11/2026)

## Mudança enviada
O preflight `scripts/preflight.py` agora cobre os sete mods do `assembleTestPack`, e não somente cinco:
- lux-engine
- lux-discovery
- lux-atlas
- lux-hud
- lux-menu
- lux-borderless
- lux-dialogues

Ele valida a presença do subprojeto Gradle, classe principal e MOD_ID, metadados NeoForge e a existência da dependência de Lux Engine nos módulos que a declaram. Também verifica que a tarefa de empacotamento depende do build de todos os sete módulos.

Lux Borderless é intencionalmente exclusivo do cliente; por isso fica fora da verificação de imports client-only em classes comuns. Ele ainda necessita de teste de carregamento dedicado às suas condições de instalação.

## Verificações externas relevantes
Documentação JavaDoc NeoForge 1.21.1 confirma a existência de `ClientPlayerNetworkEvent.LoggingOut`, `ClientTickEvent.Post`, e `StructureManager.getAllStructuresAt(BlockPos)`/`getStructureAt(BlockPos, Structure)`. Isso resolve a dúvida sobre a existência desses métodos, mas não valida todas as integrações nem a compilação.

## Bloqueio atual
O CI vem retornando jobs de build e smoke como `failure` com lista vazia de etapas e sem logs acessíveis. Não assumir que a falha seja erro de código nem marcar build concluído antes de obter evidência.

## Próximos critérios
Executar preflight + smoke Java 21, compilar todos os módulos, validar JARs gerados e criar um pacote unificado; só depois liberar teste in-game. Os testes devem cobrir mundo/Nether, regiões, diário LXJ1, descoberta de estruturas, diálogos, HUD e bússola. Preservar câmera e não criar minimapa.

# Lux Mods — checkpoint antes do teste único (14/11/2026)

## Commits desta rodada
- `cc2bc150`: região LXT2 e importação LXT1 são serializadas; migração não sobrescreve amostras recentes.
- `f8a6c613`, `8d09a7b0`: regra de importação sem sobrescrita extraída para `AtlasRegionMerge.mergeMissing`.
- `7ed59c8d`: o teste Java puro agora verifica migração não destrutiva e idempotente.

## Estratégia de entrega
- Desenvolvimento continua somente na branch `feat/engine-discovery-bootstrap`. O usuário pediu que o teste no Minecraft ocorra **uma vez**, ao final.
- O pacote NÃO está pronto. Só avisar que a implementação terminou quando houver sete JARs NeoForge 1.21.1 válidos e testes automatizados aprovados.
- Ainda há falhas de CI com jobs sem etapas/logs disponíveis, o que exige diagnóstico do ambiente de GitHub Actions antes de atribuir erros à compilação Java.
- Não incluir minimapa; manter bússola, inimigos vermelhos, cavalo e a câmera original.
- Não alterar a branch `main` nem instalar arquivos na máquina do usuário.

## Testes pendentes para concluir
1. Executar `bash scripts/smoke-pure-java.sh`.
2. Compilar `./gradlew assembleTestPack --no-daemon --stacktrace` em Java 21 e conferir os sete artefatos.
3. Verificar migração LXT1 com arquivos de região já atualizados, troca de dimensão, corrupção do diário LXJ1, carregamento regional e zoom.
4. Validar o ambiente completo Minecraft com Atlas, Discovery, Dialogues, HUD, Rest e dependências do modpack.

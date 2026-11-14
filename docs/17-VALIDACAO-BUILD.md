# Gate de build — primeiro protótipo

Estado em 2026-10-24: PR #14 draft, sem build NeoForge executado.

## Evidências
- JDK 21 presente no ambiente de teste.
- Executado teste isolado em Java 21 com as classes puras de serviços, debouncer e ledger; verificações passaram.
- Consultadas as APIs oficiais NeoForge 1.21.1 de attachments e payloads (links abaixo).

## Não validados
- Gradle/Minecraft/NeoForge completo.
- Execução client e dedicated server (inclui separação de classes client-only).
- Persistência real por jogador após logout, morte, dimensão e múltiplas contas.
- Render real, integração de dois JARs e versão exata dos modloader APIs.

## Problema de CI
GitHub Actions retorna startup_failure com zero jobs/sem logs de steps (issue #15). Pode depender de configuração, billing, entitlement ou serviço do GitHub; causa não confirmada. Revisar Settings > Actions > General e detalhes dos runs; não supor que houve erro de compilação.

## Estratégia
O workflow testa lógica Java antes do Gradle. Enquanto o Actions não iniciar jobs, executar scripts/check-environment.ps1 no Windows e anexar logs na PR. Não mesclar a PR nem publicar release estável sem compilação e teste de jogo.

Docs oficiais:
https://docs.neoforged.net/docs/1.21.1/datastorage/attachments/
https://docs.neoforged.net/docs/1.21.1/networking/payload/

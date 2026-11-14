# Como executar Engine + Discovery juntos no ModDevGradle

Os dois mods são JARs distintos. Para testes dentro do jogo, execute o projeto **Discovery**, que agora registra como mods locais tanto `lux_discovery` quanto `lux_engine`.

## No Windows / PowerShell

    .\gradlew.bat :lux-discovery:runClient

É um teste de desenvolvimento. O launcher não recebe automaticamente o pacote final, e o comando depende de download dos artefatos oficiais durante a primeira execução.

Para iniciar o servidor de teste (aceite a EULA oficial quando solicitar):

    .\gradlew.bat :lux-discovery:runServer

Para testar apenas a base Engine:

    .\gradlew.bat :lux-engine:runClient

## Proteções no build

- `project(':lux-discovery') { dependencies { implementation project(':lux-engine') } }` coloca a API do Engine no classpath.
- `neoForge.mods.lux_engine.sourceSet(project(':lux-engine').sourceSets.main)` torna o Engine um **mod local** no run de Discovery.
- `unitTest.loadedMods = [mods.lux_discovery, mods.lux_engine]` lista explicitamente ambos os mods para os testes JUnit do Discovery.
- O pacote para instalação manual continua via `assembleTestPack`, gerando **JARs separados** em `build/lux-testpack/mods/`.

## Situação de validação

Em 24/10/2026: testes Java 21 independentes de Minecraft executados com sucesso. O build completo do Gradle/NeoForge não foi possível neste ambiente por indisponibilidade de downloads Maven/Gradle. O GitHub Actions ainda apresenta `startup_failure` antes de iniciar jobs. A configuração acima é uma correção a verificar por execução real — não está aprovada para a branch main.

Referência do Gradle plugin: https://github.com/neoforged/ModDevGradle (exemplos de `mods`, `sourceSet`, `unitTest.loadedMods`).

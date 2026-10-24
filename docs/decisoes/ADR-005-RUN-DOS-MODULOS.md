# ADR-005 — Executar Engine e Discovery juntos no desenvolvimento

**Status:** configuração publicada, não validada com Gradle nem jogo real.

## Problema

Discovery precisa de lux_engine; depender do projeto Java (`implementation project(':lux-engine')`) não equivale necessariamente a registrar o Engine como mod no run NeoForge. A lista de mods locais do ModDevGradle é explícita.

## Decisão

O subprojeto `:lux-discovery` declara seu próprio mod via configuração comum e registra o source set de `:lux-engine` no `neoForge.mods`. Os testes JUnit do Discovery declaram `loadedMods = [mods.lux_discovery, mods.lux_engine]`.

O teste `LuxModLoadingTest` consulta `ModList.get().isLoaded()` para garantir que os dois identificadores estão presentes no ambiente de teste do FML. Se falhar, **não** mascarar o erro desativando o teste: rever a configuração de módulos na versão real do plugin.

## Comandos Windows

```powershell
.\gradlew.bat :lux-discovery:test
.\gradlew.bat :lux-discovery:runClient
.\gradlew.bat :lux-discovery:runServer
```

O runServer pode exigir aceitar a EULA oficial num mundo exclusivo de testes. `assembleTestPack` gera os dois JARs para uma instalação NeoForge isolada — sem alterar o Minecraft pessoal.

## Limitações

O acesso externo Maven/Gradle indisponível neste ambiente impede o build completo. GitHub Actions continua com `startup_failure` sem iniciar jobs (issue #15). Nenhum dos comandos acima está aprovado como executado. Java puro (sem API Minecraft) é compilável e testes locais foram executados separadamente.

Fonte: https://github.com/neoforged/ModDevGradle — `testproject/build.gradle`, uso de `sourceSet project(':subproject').sourceSets.main` em `neoForge.mods`.

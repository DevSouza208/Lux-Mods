# Primeiro código — implementação experimental

**Branch de implementação:** `feat/engine-discovery-bootstrap`.

## O que existe nesta branch
- Gradle multi-project com dois módulos JAR independentes: `lux_engine`, `lux_discovery`.
- Wrapper oficial Gradle importado do template NeoForge 1.21.1 ModDevGradle (inclui JAR).
- Engine com catálogo simples de módulos Lux e configuração de diagnóstico.
- Discovery com notificação vanilla para **primeiro bioma da sessão**, espera de 35 ticks e cooldown de 120 ticks, configuráveis.
- Testes JUnit de lógica pura e workflow CI `./gradlew build`.

## O que AINDA não foi implementado
- **Persistência das descobertas** ao fechar o jogo/mudar de dimensão.
- Nomes de estruturas, vilas ou personalização cinematográfica real do HUD.
- Core API de eventos/persistência completa.
- Carregamento dos módulos de fora desta branch.
- Build Gradle verificado localmente (ambiente sem acesso de rede para dependências).
- JAR funcional testado no Minecraft.

## Como usar em Windows (depois do build passar)
```powershell
git clone https://github.com/DevSouza208/Lux-Mods.git
cd Lux-Mods
git switch feat/engine-discovery-bootstrap
.\gradlew.bat build
```

JARs esperados (após build): `modules/lux-engine/build/libs/` e `modules/lux-discovery/build/libs/`. Copie **os dois** para uma instância de Minecraft Java 1.21.1 com o NeoForge da mesma linha 21.1.x. Não coloque no seu save principal sem backup.

## Por que dois projetos
JARs separados permitem instalar somente Engine ou Engine + Discovery, sem obrigar todo o conjunto. Deixar `mods` independentes é requisito central do GDD. Os próximos módulos serão incluídos gradualmente via settings.gradle.

## Fonte da stack
Template: https://github.com/NeoForgeMDKs/MDK-1.21.1-ModDevGradle — copiar apenas wrapper e adaptar scripts. O Gradle wrapper pertence ao projeto Gradle (Apache License 2.0).

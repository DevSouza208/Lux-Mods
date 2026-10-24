# Preparação do ambiente e primeira compilação

Este é um **guia de implementação futura**. O repositório ainda não contém Gradle wrapper ou código executável.

## Ferramentas
- Windows/Linux/macOS com JDK 21 x64 (para candidato Minecraft 1.21.1 NeoForge).
- IntelliJ IDEA recomendado para debugging do Minecraft, ou VS Code com Java/Gradle configurados.
- Git e cliente GitHub; Minecraft Java licenciado para testes reais.
- Instância isolada no launcher (não mexer no modpack pessoal). Guardar backups dos saves.

## Começando
1. Clone: git clone https://github.com/DevSouza208/Lux-Mods.git
2. Leia docs/GDD.md e docs/04-ROADMAP.md.
3. Antes de programar, verificar MDK oficial compatível e versões publicadas.
4. Criar workspace NeoForge oficial (ModDevGradle ou NeoGradle) e preservar arquivos de licença/Gradle wrapper do upstream conforme permitido.
5. Rodar java -version; após configurar workspace, ./gradlew build e runClient. No Windows usar gradlew.bat.
6. Rodar servidor dedicado para validar ausência de import de classes client-only e sync de eventos.
7. Registrar números de versões no ADR de stack e em config de build.

## Primeiro protótipo mensurável
Engine mínimo + Discovery: iniciar jogo, detectar bioma do player, exibir título “Planícies”, ter toggle OFF e debounce. Não começar pela vila inteira ou editor web.

## Dependências externas
Curios deve ser opcional e validada na versão alvo. Não usar JAR de outro mod copiado dentro do repositório por conveniência. Serviços de web studio, se usados, têm projeto próprio e devem importar apenas schema comum.

## Check de conclusão
JAR instalado, logs de execução client e dedicated server, screenshot do Discovery, build reproduzível, teste de persistência e sem erro crítico.

## Links técnicos
- https://docs.neoforged.net/docs/1.21.1/gettingstarted/
- https://docs.neoforged.net/docs/1.21.1/items/
- https://docs.neoforged.net/docs/1.21.1/datastorage/ (consultar índice e versão)
- https://docs.neoforged.net/

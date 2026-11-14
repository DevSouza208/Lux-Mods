# Lux Boot — progresso antecipado escuro e preservado (1.21.1)

**Estado:** ferramenta opt-in pronta para teste posterior; nenhuma mudança automática na instância SKlauncher. Tela roxa Lux completa ainda não implementada.

## Descoberta primária

O FancyModLoader da branch 1.21.1 já verifica a opção darkMojangStudiosBackground em options.txt dentro de DisplayWindow.initialize. Quando true, escolhe ColourScheme.BLACK (fundo preto). initRender continua registrando performanceBar e progressBars. Portanto **não precisamos alterar earlyWindowControl**; desativá-lo remove justamente os indicadores que queremos preservar.

Fontes de código:
- https://github.com/neoforged/FancyModLoader/blob/1.21.1/earlydisplay/src/main/java/net/neoforged/fml/earlydisplay/DisplayWindow.java
- https://github.com/neoforged/FancyModLoader/blob/1.21.1/earlydisplay/src/main/java/net/neoforged/fml/earlydisplay/ColourScheme.java
- https://github.com/neoforged/FancyModLoader/blob/1.21.1/earlydisplay/src/main/java/net/neoforged/fml/earlydisplay/RenderElement.java

## Script: scripts/lux-boot-theme.ps1

- Preview (padrão): exibe mudanças sem modificar arquivo algum.
- Apply: configura apenas darkMojangStudiosBackground:true em options.txt da instância teste; backup completo do arquivo e cópia de fml.toml sem alterar este último.
- Restore: recupera só a preferência anterior via boot-state.json do backup (schema 2), sem sobrescrever as demais opções atuais.
- Não é chamado por Atualizar Lux Mods.bat. Não toca em mundos/JARs.

Com o jogo FECHADO, PowerShell na raiz do repositório:

    powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\lux-boot-theme.ps1 -Action Preview
    powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\lux-boot-theme.ps1 -Action Apply
    powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\lux-boot-theme.ps1 -Action Restore

## Por que não é ainda o Lux Boot completo?

A janela antecipada não oferece, na branch 1.21.1, a configuração de temas JSON que existe na branch principal mais nova. Para personalizar fundo roxo, molduras e progresso com identidade Lux é necessário um provedor ImmediateWindowProvider específico para FML 1.21.1, instalado fora do ciclo normal de @Mod e testado em sandbox isolado. Um provider inválido pode bloquear startup em vez de fazer fallback, conforme issue https://github.com/neoforged/FancyModLoader/issues/415 .

**Limite de validação:** roteiro e script revisados estaticamente; PowerShell do Windows e NeoForge 21.1.264 não foram executados nesta revisão.
## Prévia interativa sem risco de boot

Criados `experimental/lux-boot-preview/index.html`, `theme.json` e `README.md` para construir a linguagem visual grafite/roxo com barras ilustrativas em proporção **854 × 480** (escala lógica do FML 1.21.1). A prévia roda inteiramente em um navegador, não exige servidor web, possui Pausar e Reiniciar e mantém texto **PROGRESSO SIMULADO** visível. NÃO deve ser confundida com carregamento real e não é incluída nos JARs. Os números do protótipo são ficção visual; um provider real deve consumir `StartupNotificationManager` e `ProgressMeter` do FML sem alterar esse sistema.

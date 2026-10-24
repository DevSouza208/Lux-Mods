# Lux Loading Slideshow — fotos e animação

**Minecraft:** Java 1.21.1, NeoForge 21.1.264; quinto mod `lux_menu` (depende só de Lux Engine). **Estado:** código publicado na PR #14 (draft). O usuário ainda não enviou as fotos; não há build/playtest NeoForge comprovado desta revisão.

## O que realmente funciona (quando compilar e instalar)

Durante telas normais de loading de mundos, como `LevelLoadingScreen`, `ReceivingLevelScreen` e `ProgressScreen`, o Lux Menu procura de 1 a 8 fotos sob:

`modules/lux-menu/src/main/resources/assets/lux_menu/textures/gui/loading/`

Nomes esperados, sempre PNG com 1920×1080 pixels: `slide_01.png`, `slide_02.png`…`slide_08.png`. O sistema exibe a foto inteira com recorte central proporcional à resolução, mantém a imagem 5 segundos e faz crossfade **de 1,2s** para a próxima. Um ícone discreto em forma de diamante e `CARREGANDO`, `CARREGANDO.`, `CARREGANDO..`, `CARREGANDO...` aparecem no rodapé. A duração mede o tempo de exibição **e não é porcentagem real de carregamento**. Com zero fotos, preserva a tela vanilla.

**Limitação essencial:** isto NÃO altera a tela vermelha de inicialização NeoForge/FML, que aparece antes do carregamento dos mods, nem o Mojang `LoadingOverlay` que não é `Screen`. Não prometer que o slide será visto na primeira abertura do jogo; aparecerá ao entrar/criar/carregar mundos quando uma tela compatível for renderizada e houver ao menos uma foto.

## Kit ZIP com instalador Windows

`Lux_Mods_Slideshow_Fotos_e_Upload_v1.zip` inclui:

- `FOTOS-AQUI/` para o usuário colocar as fotos (a pasta começa sem fotos).
- `ENVIAR-FOTOS-GITHUB.bat` — clicar duas vezes.
- `ENVIAR-FOTOS.ps1` — organização e envio seguro, confirmação explícita.
- `PREVIA-FOTOS.html` — apresentação local no navegador sem comunicação com servidor.
- `LEIA-ME-PRIMEIRO.txt` — instruções rápidas.

Fluxo: o usuário extrai o ZIP; coloca 1–8 imagens PNG/JPG/JPEG em `FOTOS-AQUI`, em ordem alfabética (usar `01-...`, `02-...` etc. para controlar). O PowerShell prepara cópias 1920×1080 com recorte central; renomeia `slide_01.png`…; confirma `S`; atualiza `feat/engine-discovery-bootstrap`, troca somente slides gerenciados nessa pasta e faz commit/push. Não envia automaticamente ao abrir o script e não modifica a imagem original. Se há alteração Git local pendente, aborta. Depende de Git for Windows autenticado e de `%USERPROFILE%\LuxModsDev` já clonado.

**Não subir o ZIP fechado no repositório:** o mod só descobre os PNGs depois que eles estiverem na pasta de resources. Após enviar fotos, com Minecraft fechado, o usuário executa seu atualizador habitual que recompila e copia os JARs.

## Configuração opcional

Arquivo do cliente: `config/lux_menu-client.toml`: `loadingSlideshow = true`, `slideSeconds = 5`, `fadeMillis = 1200`. A opção `enabled = false` também desativa o tema Lux inteiro, preservando as telas vanilla.

## Checklist para sessão futura

Revisar 12 itens em `docs/30-CHECKLIST-DE-TESTES.md`, grupo `6B`, com imagens de dia/noite, 1 e 8 fotos, zoom de GUI, sem fotos e multiplayer. O cálculo `SlideTiming.java` passou smoke Java 21 isolado em 24/10; JUnit foi adicionado para rodar durante o Gradle, sem alegar aprovação dele antes da compilação.

## Próximo passo: tela vermelha inicial

Fora do escopo dessa feature. Só será possível com solução de carregamento antecipado compatível com FancyModLoader 1.21.1, distribuída separadamente e com rollback; `docs/32-LUX-BOOT-CARREGAMENTO.md` descreve o estudo.

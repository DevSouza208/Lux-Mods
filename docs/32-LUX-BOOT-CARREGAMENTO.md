# Lux Boot — preparação da tela de carregamento NeoForge 1.21.1

**Status:** pesquisa realizada e utilitário de configuração **opt-in** publicado. Não há novo mod boot nem customização da tela inicial no pacote padrão. O comando padrão apenas apresenta informações, e o usuário não precisa testar agora.

## Limitação técnica confirmada

O FancyModLoader atual documenta temas JSON em earlydisplay/THEMING.md, mas a árvore da branch **1.21.1** usada por nosso NeoForge 21.1.x não contém esse mecanismo de temas. Portanto não devemos copiar theme-default.json do loader mais novo nem prometer uma tela Lux completa a partir desse arquivo.

O NeoForge documenta **earlyWindowControl=false** como forma de desativar a janela antecipada FML. A opção vanilla darkMojangStudiosBackground:true deixa escura a fase Mojang posterior. **Essas opções desativam os indicadores de progresso antecipados do FML e não implementam nosso tema ornamental**.

Outra rota seria um custom earlyWindowProvider compatível com 1.21.1; **não usaremos agora**, pois um provider ausente pode não voltar automaticamente ao default (issue #415 FancyModLoader). Separar esse projeto em módulo optativo e realizar rollback externo antes de instalar.

## Ferramenta experimental isolada

Arquivo: scripts/lux-boot-theme.ps1

Atua exclusivamente em %APPDATA%\.sklauncher\instances\teste. Exige que config/fml.toml e options.txt existam. Rejeita uma configuração desconhecida/duplicada, copia arquivos originais para lux-boot-backup com timestamp antes de modificar e permite restaurá-los.

No PowerShell a partir do repositório:

    powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\lux-boot-theme.ps1 -Action Preview

Somente quando quiser experimentar o carregamento escuro, com Minecraft fechado:

    powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\lux-boot-theme.ps1 -Action Apply

Reverter:

    powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\lux-boot-theme.ps1 -Action Restore

O script **não é executado** pelo atualizador de mods. A tela de boot atual permanece como está até escolha explícita do usuário.

## Testes adiados
- Pré-visualização não altera byte algum nos arquivos da instância.
- Aplicação faz backup externo da configuração antes de mudar.
- Nova inicialização escura realmente carrega e não perde a função principal.
- Reversão restaura comportamento padrão e não altera saves/JARs.
- Avaliar início do jogo e impacto de perder as barras de progresso FML.

## Referências
- https://github.com/neoforged/FancyModLoader/blob/main/earlydisplay/THEMING.md (versão atual, NÃO compatível por presunção).
- https://github.com/neoforged/FancyModLoader/tree/1.21.1/earlydisplay
- https://neoforged.net/meta/displayerrors/
- https://github.com/neoforged/FancyModLoader/issues/415
- https://github.com/lukaskabc/SimpleCustomEarlyLoading (estudo de custom provider, não instalado).

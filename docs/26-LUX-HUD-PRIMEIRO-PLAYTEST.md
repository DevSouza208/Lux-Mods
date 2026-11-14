# Lux HUD — primeiro playtest

**Plataforma:** Minecraft 1.21.1, NeoForge 21.1.264, Java 21, instância `teste` do SKlauncher.

## Teste prático

1. Fechar o Minecraft e executar o atalho `Atualizar Lux Mods.bat` da área de trabalho.
2. Confirmar `BUILD SUCCESSFUL` e quatro arquivos: Engine, Discovery, Atlas e **HUD**. Se o build falhar, nenhuma versão nova deve ser instalada.
3. Entrar no jogo e conferir painel superior esquerdo: cabeça real da própria skin, vida, fome e nível/XP. A hotbar e a bússola do Atlas continuam embaixo.
4. Testar perder vida, comer, receber XP e usar uma poção. As barras e os ícones devem representar apenas informações reais.
5. À direita: `MISSÕES` e `Nenhuma missão acompanhada` até Lux Quests enviar conteúdo; não há quests falsas.
6. Testar `F1`: a HUD deve sumir. Testar chat, inventário e menu: não pode sobrepor interfaces.
7. Conferir config CLIENT em `config/lux_hud-client.toml`: `mode=CLASSIC` preserva interface vanilla; `mode=RPG` restaura painel.

## Teste multiplayer temporário

Com dois ou mais usuários e os mods instalados nos clientes e no servidor, um operador cria um time vanilla:

    /team add luxparty
    /team join luxparty Jogador1
    /team join luxparty Jogador2

O jogador deve ver **somente seus companheiros do mesmo time**, em tamanho menor abaixo do painel. A vida e o nível são enviados pelo servidor a cada segundo; jogadores fora do time não aparecem. Não há simulação de vida nem exposição de todos os jogadores do servidor.

## Limites atuais

- Grupo via scoreboard team é ponte de teste; invites/consentimento formal dependem de Lux Party.
- Missões completas/com progresso exigem o módulo Lux Quests; a área de missões é apenas UI e API de integração local nesta versão.
- Layout visual ainda bruto, sem animações nem lógica de autoocultação.
- Build e runtime ainda não foram validados após a inclusão do HUD. Não fechar PR antes de recompilar e testar cliente e servidor dedicado.

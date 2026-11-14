# Lux Atlas — preparação para teste integrado (14/11/2026)

Esta rodada **não requer teste manual imediato**: o usuário vai testar os módulos em conjunto quando houver um pacote de build válido.

## Correções de integridade feitas na branch

1. **Amostras não journalizadas preservadas por dimensão**: se o disco recusar a criação do diário LXJ1 e o jogador atravessar um portal, `AtlasWalkDiscovery` mantém as amostras em `UNSUBMITTED` com o caminho do mundo original; tenta escrever o diário novamente mais tarde.
2. **Fechamento da tela do Atlas não perde as amostras**: se `AtlasMapScreen` falhar ao criar o diário ao fechar, entrega seu mapa sujo para a mesma fila de tentativas da exploração passiva.
3. **Validação integral do backlog**: `AtlasWriteQueue.registerScope` lê e valida todos os diários de uma dimensão antes de inserir quaisquer desses registros na fila. A presença de um arquivo corrompido não permite recuperação parcial silenciosa do conjunto.
4. **Status de erro preservado**: aceitar um novo diário não apaga a indicação de erro de gravação anterior. Um sucesso real de aplicação limpa o erro (a validação in-game do fluxo ainda é necessária).

## Limitações remanescentes

- `UNSUBMITTED` é uma proteção **em memória**: se o sistema não conseguir escrever nem o diário, um desligamento forçado ainda pode perder esses dados. O mapa deve mostrar erro, não promessa de segurança.
- Escrita LXJ1/fsync é síncrona e pode causar pequenos travamentos; a compactação LXT2 ocorre em segundo plano.
- O diário recupera apenas lotes já registrados; amostras ainda não entregues ao `flush` não são duráveis.
- Não há validação de build do NeoForge 1.21.1 nem de integração com mods no jogo.
- A execução do GitHub Actions 43557959679 encerrou como failure em ambos os jobs (build e smoke); os detalhes do runner ainda precisam ser recuperados. **Não fornecer JARs como prontos.**

## Checklist do pacote único de testes

1. Compilar os módulos para NeoForge 1.21.1 com Java 21 (e só então disponibilizar os JARs).
2. Abrir backup de mundo existente e testar Atlas em coordenadas positivas/negativas, zoom e marcos.
3. Explorar e atravessar portal antes e durante gravações. Confirmar que cada mundo/dimensão mantém os seus próprios dados.
4. Simular journal correto remanescente de crash e journal corrompido; confirmar restauração ou aviso sem exclusão.
5. Verificar os pontos vermelhos para inimigos, ícone do cavalo e memória do último avistamento.
6. Exercitar vila/portal arruinado, descoberta registrada, diálogos e HUD.
7. Medir quedas de FPS ao mapear intensamente e identificar gargalos de leitura de regiões.

Sem minimapa. Sem mudanças de câmera. Nenhuma alteração em `main`.

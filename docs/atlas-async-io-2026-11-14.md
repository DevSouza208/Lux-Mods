# Lux Atlas — fila assíncrona de gravação (14/11/2026)

## Alterações enviadas
- Nova `AtlasWriteQueue`: executor dedicado de **um único escritor daemon** para todos os arquivos de terreno. As gravações são ordenadas e não competem entre exploração automática e mapa aberto.
- `enqueue(scope, changes)` copia as amostras do chamador para lotes em memória, segmentados por diretório de mundo/dimensão. Não considera o envio como um salvamento concluído.
- Os lotes são mesclados por coordenada. Mudanças mais recentes prevalecem sobre lotes que falharam ao gravar.
- O `AtlasTerrainCache.saveToScope` existente executa as operações reais de leitura/mesclagem/compactação/substituição de arquivos no escritor, em vez de na thread principal, durante a exploração normal.
- `AtlasWalkDiscovery.flush()` envia lotes sem bloquear a coleta; tenta reenviar alterações pendentes a cada ~10 segundos e preserva o caminho da dimensão de origem ao trocar de mundo.
- `AtlasMapScreen` usa a mesma fila no autosave e ao fechar o mapa, mostrando status "Salvando exploracao..." ou erro no rodapé.
- Ao sair do servidor, `drain(3000)` faz espera limitada pela conclusão real; existe também shutdown hook com `drain(5000)` como última tentativa na saída normal da JVM.
- Erros de IO mantêm o lote pendente para reenvio, sem loop agressivo; exceções inesperadas no escritor também são convertidas em falha.
- Tudo usa dados explorados; não adiciona minimapa, não altera câmera nem lógica da bússola.

## Riscos e próximos passos
- **Ainda não compilado/testado no NeoForge 1.21.1.** Não distribuir JARs antes de build e testes de jogo.
- A fila é temporária **em memória**. Encerramento forçado, falta de energia, corrupção do disco ou falha persistente de IO ainda podem deixar amostras sem persistência. Uma implementação posterior poderá usar um journal durável com recuperação no próximo início.
- `drain` espera apenas até o timeout; não bloqueia o Minecraft indefinidamente. Status de erro é exibido no mapa; saída registra aviso no stderr, não apresenta confirmação de salvamento.
- A migração legada e leitura de regiões para exibição ainda podem provocar IO na thread do cliente; mover essas operações a um executor de leitura exige controle cuidadoso do cache e cancelamento por mundo.
- Não há limite de memória da fila em situações de disco lento/inacessível; medir em mundo grande antes de lançar a versão.
- Verificação obrigatória: reconectar após sair normalmente, trocar dimensão, desconectar durante escrita, simular falha de disco, abrir/fechar Atlas sob carga, e confirmar que o terreno salvo não desaparece.

## Teste de aceitação desejado
1. Criar backup do mundo e pasta `lux-atlas`.
2. Explorar centenas de blocos, abrir Atlas, movimentar e alterar o zoom, salvar novos marcadores, sair normalmente.
3. Reabrir e confirmar que o terreno está integralmente presente em cada dimensão.
4. Simular arquivo regional somente leitura: o Atlas deve exibir erro, manter amostras pendentes em memória e tentar novamente sem apagá-las.
5. Medir tempo de resposta e TPS/FPS com fila carregada, verificando ausência de travamento causado por gzip no tick principal.

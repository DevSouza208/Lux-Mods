# Lux Atlas — diário persistente de recuperação (14/11/2026)

## Implementado na branch feat/engine-discovery-bootstrap
- `AtlasWriteJournal.java`: cada lote de alterações é salvo como um arquivo LXJ1 separado em `<pasta da dimensão>/.pending/`, com cabeçalho, contagem e CRC32; a gravação do arquivo e o rename são concluídos antes de o método `enqueue` retornar sucesso.
- `AtlasWriteQueue.java`: um único escritor aplica os diários na ordem de enfileiramento. Depois que `AtlasTerrainCache.saveToScope` termina, o diário correspondente é removido. Se houver erro na leitura/aplicação/remoção, o lote permanece no disco e na fila, e lotes posteriores **não são aplicados adiante dele**.
- Ao entrar no mundo/dimensão, `registerScope` procura arquivos LXJ1 que sobraram de outra sessão, valida a integridade e os agenda para reaplicação antes das novas alterações. Diários truncados/corrompidos são preservados para diagnóstico; não são descartados automaticamente.
- `AtlasWalkDiscovery` e `AtlasMapScreen` limpam seus mapas de alterações pendentes **somente se a criação do diário retornar sucesso**. Em caso de falha, conservam as amostras na RAM e o Atlas exibe mensagem de erro.
- `AtlasTerrainCache` chama `FileChannel.force(true)` para os arquivos regionais antes da substituição atômica; sincronização do diretório após rename é best effort. O diário também força o conteúdo antes do rename.
- O escritor mantém os dados separados por diretório de mundo/dimensão e é compatível com os arquivos de região LXT2 existentes.
- Novo `LuxAtlasJournalSmoke` verifica round-trip, isolamento por dimensão, recuperação simulada pela presença do diário, corrupção detectada e remoção após confirmação. Adicionado a `scripts/smoke-pure-java.sh`.

## Limites atuais e honestidade sobre segurança
- Escrita do **pequeno diário LXJ1 ainda é síncrona no thread chamador**: reduz o trabalho pesado de gzip na thread do jogo, mas cada lote pode causar uma breve espera de IO.
- O journal só protege lotes que já foram submetidos por `flush()`; amostras recolhidas entre dois flushes continuam apenas em memória. Em um desligamento forçado, esses segundos de exploração ainda podem ser perdidos.
- Persistência absoluta contra falha do hardware não é garantida. `fsync` de diretórios não funciona igualmente em todos os sistemas; queda de energia e controladores de disco podem ter comportamento variável.
- Um diário corrompido bloqueia a recuperação daquele escopo até intervenção; preservamos o arquivo para não causar perda silenciosa.
- Não existe limite rígido de tamanho do backlog de diários com disco gravável mas regiões sempre com falha. Requer teste sob falhas prolongadas.
- **Não há build NeoForge 1.21.1 nem teste integrado em Minecraft concluído nesta rodada.** Uma contagem de chaves/chaves balanceadas não é compilação.

## Teste de aceitação obrigatório
1. Faça backup de mundo e pasta `lux-atlas`.
2. Explore terreno e confirme arquivos `.pending/*.lxj` durante escrita. Saia normalmente e valide que os journals desaparecem após gravação.
3. Reinicie após término abrupto enquanto existe um journal, sem apagar `.pending`. Confirme que as amostras aparecem após recarregar a dimensão.
4. Force um journal truncado. Confira que o Atlas acusa falha e não o exclui.
5. Teste troca entre Overworld e Nether, coordenadas negativas, arquivos antigos LXT1 e mapas LXT2.
6. Meça tempo de frame ao gerar lotes; considere configurar tamanho/frequência de flush depois do profiling.

Sem minimapa. Bússola de monstros/cavalo e câmera permanecem inalteradas.

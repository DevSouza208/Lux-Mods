# Lux Atlas — arquivo persistente por regiões (13/11/2026)

## Problema observado
O mapa anterior mantinha até 400 mil amostras em RAM e salvava apenas o conteúdo atual dessa memória num snapshot monolítico. Terrenos antigos podiam sumir ao explorar muitos lugares; o raio de amostragem passiva era pequeno e a tela exibia só uma ilha verde.

## Mudanças no código
- Amostras de terreno passam a ser persistidas em arquivos regionais compactados `lux-atlas/<mundo>_<dimensão>_regions/r.<rx>.<rz>.lxt.gz`.
- Uma região compreende 128x128 blocos; arquivo contém coordenadas/cores de amostras descobertas. Arquivos de outras regiões **não são apagados** pela expiração do cache RAM.
- Atualizações são mescladas às amostras já armazenadas no disco; gravação em arquivo temporário seguida de substituição atômica quando suportada pelo sistema de arquivos.
- Snapshots antigos LXT1 (`..._map.bin.gz` e `..._walk.bin.gz`) são importados uma única vez, e os arquivos originais são preservados.
- A tela carrega lentamente só os arquivos regionais próximos da área exibida, inclusive quando chunks não estão mais carregados no Minecraft.
- Quando o usuário navega para outra área, o cache RAM é podado; regiões são relidas do disco quando revisitadas.
- A coleta automática cobre caminhos imediatos num raio de 24 blocos e também varre progressivamente regiões com chunks já carregados em até 96 blocos ao redor do jogador, processando até 900 pontos distantes por ciclo de 40 ticks.
- O mapa salva novas amostras a cada ~10 segundos enquanto aberto e ao fechar; a coleta em segundo plano grava por ciclos.
- Sem limite global predefinido no número de regiões exploradas; o limite prático depende de espaço em disco e desempenho de IO.

## Limitações e próximos passos
- **Não há build nem teste dentro do jogo para esta alteração.** Cópia de segurança do mundo e pasta `lux-atlas` recomendada antes da instalação.
- Não é um mapa completo do planeta: só mostra chunks efetivamente observados. Não inventa terrenos não visitados nem revela estruturas subterrâneas.
- Dados de terrain são cores de superfície 2x2, não histórico detalhado de todos os blocos, cavernas, entidades ou mudanças de terreno; para igualar/superar JourneyMap ainda faltam minimapa, mapas de cavernas, ícones, waypoint management avançado e rendering em escala.
- Leitura da região ocorre no thread da UI (limitada a quatro arquivos por quadro) e escrita incremental ocorre no thread do cliente. Isso exige teste de FPS e profiling em mundos grandes antes de chamar pronto.
- Durante troca de mundo e falhas de IO, pode haver uma janela pequena de exploração ainda não persistida; serão necessárias rotinas de flush e retry mais fortes.
- Atenção: arquivos de um mesmo mundo/nome de servidor ainda herdam o identificador `AtlasMarkers.worldKey` e seu tratamento de conflitos precisa ser revisado futuramente.

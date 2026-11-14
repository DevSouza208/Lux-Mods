# Lux Atlas — zoom regional com nível de detalhe (14/11/2026)

## Problema
Os mapas persistiam amostras de blocos individuais, mas quando a escala era 4/8/16 blocos por célula, o desenho procurava apenas uma amostra no canto da célula. Isso podia gerar falsos espaços vazios ao afastar o zoom, embora houvesse blocos explorados ali.

## Mudanças implementadas no código
- Novo `AtlasLevelOfDetail`: agrega amostras registradas de 1 bloco em células de 2, 4, 8 e 16 blocos, usando média de cores.
- A agregação usa as coordenadas do mundo, inclusive negativas, não a posição do viewport; arrastar o mapa não muda onde a célula cai.
- Arquivos antigos com amostras 2x2 também participam, sem alterar os arquivos originais; amostras novas com 1 bloco substituem a contribuição do registro antigo correspondente.
- Nenhuma região desconhecida é inventada. Uma célula regional só ganha cor se houver pelo menos uma amostra descoberta no seu espaço.
- O cache derivado é **só memória de tela** e nunca substitui o arquivo detalhado. O zoom de 1 bloco continua lendo a amostra original.
- O índice LOD é recalculado quando a área visível muda ou novas amostras chegam, no máximo uma vez a cada 20 ticks; não a cada frame.
- `LuxAtlasLodSmoke` adicionado à suíte Java pura, com amostras negativas, cores médias, preservação da névoa e preferência pelos dados detalhados.

## Limites importantes
- Cada célula resumida ocupa 2 pixels, então ela é uma **representação visual de uma área**, não uma cópia de todos os seus blocos em tela. Uma única amostra encontrada pode colorir uma célula inteira no zoom regional; futuras melhorias poderão indicar a densidade de exploração para não dar impressão de área totalmente mapeada.
- A média de cores não substitui níveis de detalhe com textura/contornos avançados nem representa relevo 3D.
- O carregamento ainda realiza IO síncrono limitado por tick; mundos grandes exigem profiling.
- O teste Java foi **adicionado, mas não executado no repositório via CI**. Consulta do último commit não retornou verificações nem workflows; não há JAR NeoForge validado.
- Sem minimapa e sem alterações na câmera.

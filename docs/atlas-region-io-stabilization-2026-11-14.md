# Lux Atlas — estabilização do carregamento regional (14/11/2026)

## Correções desta rodada
1. **Removida a expulsão LRU dentro de `tiles.put()`**. Quando um mundo grande carregava uma região, cada `put` podia invalidar outra região marcada como já carregada; isso provocava ciclos de carregamento e disco.
2. **Poda explícita de RAM pela extensão exata da viewport**, incluindo mudanças de zoom e pan. Só descarta a cópia em memória, nunca os arquivos `r.<x>.<z>.lxt.gz` no disco.
3. **Leitura regional deslocada do render frame para o tick de tela**, limitada a um arquivo por tick. Ainda é IO síncrono, porém evita repetir leitura a cada frame a 60+ FPS.
4. **Falhas de leitura não são tratadas como sucesso**. Há intervalo de 100 ticks antes de tentar novamente; outras regiões continuam sendo desenhadas.

## Observações
- O mapa ainda necessita profiling em mundos gigantes; uma região pode conter até 16.384 amostras 1x1 no formato atual.
- O limite de desenho de células na tela e o cache de superfície ainda não usam um pipeline de imagens/texturas assíncronas.
- Os smoke tests puros verificam matemática de coordenadas, não a integração com as APIs NeoForge.
- **Commits no código não significam JAR compilado**: rodar o build NeoForge 1.21.1 e testes in-game antes de distribuir.
- Sem minimapa; bússola com pontos de inimigos e marcador do cavalo continua intocada.

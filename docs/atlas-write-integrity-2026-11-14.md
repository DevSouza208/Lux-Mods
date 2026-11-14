# Lux Atlas — gravação de regiões e integridade (14/11/2026)

## Implementado nesta rodada
- A gravação por região compara as amostras enviadas com o arquivo existente. Quando nenhuma posição/cor mudou, não recompacta nem substitui o arquivo.
- Extraída a regra de mesclagem pura `AtlasRegionMerge.merge`, preservando coordenadas anteriores e atualizando somente as alteradas.
- A leitura de uma região é transacional em memória: decodifica por inteiro num mapa temporário antes de transferir ao mapa visível.
- Verifica o cabeçalho, quantidade, chaves duplicadas, coordenadas pertencentes ao arquivo regional certo e bytes adicionais fora do formato. Um arquivo inválido não contamina regiões vizinhas nem é sobrescrito por gravações comuns.
- Adicionado `LuxAtlasRegionMergeSmoke` à suíte `scripts/smoke-pure-java.sh`.

## Testes e limitações
- Executada **verificação isolada com Java 21** para a lógica de mesclagem; passou casos de adição, repetição idêntica, mudança de valor e preservação de outra coordenada.
- Os testes completos da suíte do repositório e o build NeoForge 1.21.1 **não foram executados nesta rodada**. A verificação isolada da regra de mesclagem não testa a API Minecraft ou o caminho de IO comprimido.
- A leitura e escrita dos arquivos regionais ainda são síncronas; este pacote reduz escritas desnecessárias, mas NÃO constitui async IO. A próxima implementação deve desenhar e testar uma fila de gravação serializada, com flush de desconexão e recuperação de falhas.
- Mantenha backup dos mundos e da pasta `lux-atlas` antes de testar novas versões.
- A bússola, o marcador do cavalo, as bolinhas vermelhas de inimigos e a ausência de minimapa foram preservados.

## Critério para considerar pronto
Compilar os módulos, executar `scripts/smoke-pure-java.sh` e testar exploração longa, carregamento de arquivos corruptos, troca de dimensão e reentrada no mesmo mundo, sem perda de amostras.

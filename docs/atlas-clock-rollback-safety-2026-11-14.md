# Lux Atlas — correção da ordem dos diários (14/11/2026)

## Problema encontrado
Os diários LXJ1 eram nomeados por timestamp do sistema operacional mais sequência reiniciada a cada processo. Se o relógio do PC recuasse entre duas sessões, um arquivo novo poderia ter nome anterior a um arquivo pendente mais antigo. O replay ordenado poderia então aplicar a amostra velha por último e sobrepor a recente.

## Implementação
- `AtlasWriteJournal.create` agora mantém relógio lógico monotônico POR ESCOPO de mundo e dimensão.
- Na primeira criação de diário daquele escopo numa sessão, procura os arquivos pendentes e recupera o maior timestamp + sequência.
- A próxima alteração usa o maior valor entre relógio atual e timestamp recuperado; se usar o mesmo timestamp, incrementa a sequência anterior.
- O estado lógico é atualizado apenas DEPOIS da criação, forçamento e substituição do arquivo. Tentativas que falham não avançam o contador em memória.
- `LuxAtlasJournalSmoke` inclui simulação de arquivo pendente com relógio artificialmente avançado e confirma que o próximo journal fica cronologicamente posterior e a ordenação de replay está correta.

## Limites
- Esse tratamento cobre relógio revertido e relançamento normal do mod. Arquivos manualmente renomeados para formatos arbitrários ou execução simultânea de várias instâncias sobre o mesmo diretório ainda exigem proteção adicional.
- A lógica de journal foi atualizada no GitHub, mas não houve execução completa de Java 21/Gradle para esta revisão.
- O CI segue sem recursos de execução conforme informado pelo usuário; não tratar ausência de execução como falha de compilação do código.
- Continuar na branch feat/engine-discovery-bootstrap; não mexer na câmera, na bússola ou criar minimapa.

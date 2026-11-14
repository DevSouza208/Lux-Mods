# Lux Mods — execução do roadmap (14/11/2026)

Branch: `feat/engine-discovery-bootstrap`
Meta: **não existe minimapa no HUD**. A bússola é a navegação imediata; o Atlas aberto em M é o mapa geográfico persistente.

## Implementado em código nesta rodada (AINDA SEM BUILD/TESTE IN-GAME)

1. **Atlas — amostragem 1 bloco**: `AtlasWalkDiscovery` grava amostras por bloco, com área próxima preenchida e varredura progressiva até 96 blocos de distância apenas em chunks carregados. `AtlasMapScreen` renderiza amostras exatas e usa a antiga resolução 2x2 como fallback.
2. **Atlas — armazenamento por regiões**: mantém arquivos compactados em disco, migra snapshots antigos, permite leitura sob demanda, mantém limite de RAM separado do arquivo permanente, salva ao explorar, ao fechar o mapa e ao desconectar do jogo. **Falta teste de carga de mundo grande e de durabilidade após crash.**
3. **Atlas — marcadores**: nome, tipo e cor, edição, confirmação antes de remover. Marcadores antigos continuam legíveis (campos extras opcionais no JSON). Estruturas verificadas podem produzir marcadores automáticos.
4. **Compass**: bolinhas vermelhas para monstros hostis conhecidos pelo cliente num raio de 32 blocos; ícone pixel-art de cavalo do jogador, com última posição conhecida diferenciada por `?`. A posição antiga é aproximada e NÃO representa um rastreador em tempo real de entidades descarregadas. Sem minimapa.
5. **Discovery**: além de biomas, amostra estruturas que o servidor confirma na área atual. Envia identificação e início da estrutura, deduplica por instância e dimensão; exibe título em português. Sem inventar estruturas/POIs. Fila visual evita sobrescrita de títulos próximos. O Atlas pode escutar essas descobertas sem dependência direta entre seus módulos.
6. **Dialogues**: interface PT-BR para villagers e Guard Villagers com banco de saudações, boatos e opção "Qual é a boa?". Diálogos gerais não iniciam missões. Guardas não têm seu combate ou patrulha pausados.
7. **HUD e Rest**: mudanças das rodadas anteriores preservadas (vinhetas, fogueira com descanso, sono/exaustão, curativos). Necessário testar Thirst Was Taken e todos os alimentos para impedir cura indevida.

## Pendente antes de declarar a atualização instalável
- Compilar sete JARs com NeoForge 1.21.1 e corrigir quaisquer erros. GitHub Actions falhou anteriormente sem executar Gradle; **nenhum build válido foi demonstrado nesta rodada**.
- Testar mapas negativos de coordenadas, troca de dimensões, vários mundos com nomes semelhantes, mapas legados, queda de energia e arquivos danificados.
- Testar se a marcação dos cavalos identifica corretamente dono/entidade no cliente, inclusive após desmontar e atravessar dimensão.
- Testar detecção de vilas grandes (a área entre edifícios pode estar fora da bounding box de estrutura), portais arruinados e estruturas de mods.
- Avaliar performance e uso de memória ao varrer e armazenar grandes mundos; IO ainda é parcialmente síncrono e requer profiling.
- Concluir painel de Missões e Upgrades, categorização mais profunda de descobertas, mapa subterrâneo/cavernas, níveis de detalhes e ícones de entidades no mapa; NÃO IMPLEMENTADOS.
- Estender mais o banco de falas se desejado e validar traduções de cada mod.
- Rever visual geral da GUI, controles e responsividade em resoluções menores.

## Checklist sugerido ao testar
1. Backup do mundo e da pasta `lux-atlas`.
2. Explorar 500 blocos de distância, abrir M em vários zooms, fechar, sair do jogo, entrar novamente e voltar até uma região antiga. Conferir superfície e marcadores.
3. Adicionar um marcador Casa, trocar nome/tipo/cor, remover com confirmação; testar a persistência.
4. Verificar compass N/S/L/O, mobs hostis e cavalo sob diferentes direções de câmera.
5. Encontrar vila e portal arruinado, verificar banner único e marcador geográfico. Conversar com villager e guardas sem bloquear combate.
6. Verificar vida/fome/sede, fogueira sentada/em pé, sono de dois dias e desmaio de três dias.

**Não instalar JARs antigos achando que incluem os commits.** A branch guarda o código, mas precisa passar pelo processo de build.

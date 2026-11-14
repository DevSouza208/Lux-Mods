# Lux Mods — diálogos e Atlas (rodada 13/11/2026)

## Lux Dialogues: aldeões e Guard Villagers
- Villagers vanilla: diálogo em PT-BR com saudação, "Como vai?", "Ouviu boatos?", loja (quando aplicável) e sair.
- Guard Villagers: reconhecimento opcional pelo registro `guardvillagers:*guard*`. Não cria dependência de compilação do mod de terceiros.
- Conversar com a mão principal vazia, sem agachar, fora de combate e perto do NPC.
- O guarda olha para quem está conversando sem interromper a patrulha ou sobrescrever alvo de combate; a sessão termina se entrar em combate, se afastar ou expirar.
- Segurar item/agachar mantém interação normal do mod original.
- Banco de falas em `DialogueLines.java`: 18 saudações gerais, 30 boatos gerais, 8 saudações dos guardas, 10 boatos dos guardas e 5 respostas a "Como vai?".
- O texto é ficcional e genérico, sem gatilhos de quests, verificação de estruturas reais ou flags de estado; a escolha é determinística pelo NPC + dia do Minecraft.
- A conversa não substitui negociação vanilla dos villagers.

## Lux Atlas: correções de estrutura
- Cache do mapa padronizado em amostras de terreno alinhadas a uma grade de 2x2 blocos, independente de zoom.
- Exploração passiva ampliada para 24 blocos de raio por eixo ao redor do jogador, apenas chunks já carregados.
- Amostras com sombreamento de relevo simples para distinguir terrenos.
- Cache ampliado para 400 mil amostras e persiste por mundo/dimensão; tela do mapa recebe atualização da exploração enquanto aberta.
- Zoom na posição do cursor, minimizando saltos de navegação.
- Sem geração de chunks distantes, sem copiar dados do JourneyMap e sem dependência adicional.

## Limitações, pendências e testes
- **Não foi compilado ou testado in-game nesta rodada.** Não substitua JARs existentes só com base nesses commits; use build de teste quando disponível.
- Verificar o ID real de entidade do Guard Villagers na versão instalada e se o clique direito nativo dele é preservado quando há arma na mão/agachamento.
- Confirmar que o guard realmente vira a cabeça para o player e interrompe conversa ao entrar em combate.
- Confirmar as linhas do diálogo em resoluções pequenas e se o layout cabe na tela.
- Testar cache Atlas em três zooms, panning, reabrir jogo e troca de dimensões.
- Um mapa mais completo que JourneyMap exigirá ainda minimapa, renderização por regiões, ícones de entidades, muito mais opções de marcadores, descoberta incremental de biomas e profilers de performance. Essas funcionalidades NÃO foram implementadas nem prometidas como prontas.

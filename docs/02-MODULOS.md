# Catálogo de módulos

| Identificador | Função | Integração com Engine | Status |
|---|---|---|---|
| lux-engine | Detecção, eventos, configuração, persistência, API de integração | Base | Planejamento |
| lux-atlas | Bússola e mapa com M, marcadores e dimensões | Regiões/vilas e HUD | Planejamento |
| lux-discovery | Títulos ao entrar em biomas/vilas/estruturas, nomes persistentes | Atlas/Villages/Lore | Primeiro experimento indicado |
| lux-hud | Layout RPG, autoocultação e painel multiplayer/quests | Atlas/Quests/Progression | Planejamento |
| lux-npc | Diálogos, personalidade, comércio, memória, reações e guardas | Lore/Quests/Villages | Planejamento |
| lux-lore | Livros escritos, rumores, conhecimento e pistas não obrigatórias | NPC/Discovery/Quests | Planejamento |
| lux-quests | Contratos por NPC e grafos/blueprints de tarefas | NPC/Villages/HUD | Planejamento |
| lux-villages | Geração, lotes, proprietários, rotinas, reparos e população | NPC/Quests/Discovery | Planejamento |
| lux-progression | Níveis RPG, perks, XP e atributos | HUD/Quests | Planejamento |
| lux-inventory | Mochila de dez níveis e melhorias, acesso integrado | NPC/Items/Quests | GDD v1 definido |
| lux-items | Itens por pacote externo, equipamentos, acessórios, consumíveis | Inventory/Food/Curios | Planejamento |
| lux-food | Alimentos/bebidas prontos, sem novas receitas v1 | Items/NPC | Escopo mínimo definido |
| lux-studio | App web 2D/voxel 3D, previews e exportação de pacotes | Items | Ferramenta externa planejada |
| lux-materials | Unificação de minérios/itens/tags/generation por modpack | Adaptadores | **Standby** |
| lux-launcher | Instalação/atualização de modpacks | Externo | **Standby** |

**Regra de dependência:** cada módulo Minecraft utiliza o Engine como base técnica, mas não obriga instalar os demais módulos. Studio e launcher são aplicações externas. O mínimo do Engine deve ser leve e sem influência no gameplay.

## Dependências funcionais

- Discovery básico requer apenas bioma + Engine; nomes de vilas ficam melhores com Villages.
- Atlas básico requer apenas Engine; marcas de discovery/vilas são opcionais.
- HUD básico funciona com os valores vanilla; XP RPG, missões e bússola são provedores opcionais.
- NPC básico usa diálogos e comércio próprio; missões e lore entram como provedores opcionais.
- Villages tem proprietário, camas, planta e reparos sem exigir quests. NPC estende reações.
- Quests funciona como motor independente de objetivos; contratos provenientes de NPC exigem adapter.
- Inventory core não depende de NPC para os upgrades serem testados; os serviços entram posteriormente.
- Food usa o carregador Items (ou registro próprio de catálogo empacotado se necessária independência do distribuidor).

## Nomes de trabalho

Lux Mods é a família; Lux Engine é o núcleo; versões reais, IDs finais, arte, marcas e canais de distribuição estão sujeitos a revisão. Não usar “v1” como sinônimo de pronto.

# Lux Mods — Game Design Document (GDD)

**Edição:** 0.1 · **Estado:** pré-produção · **Idioma-base:** pt-BR · **Data:** 2026-10-24

> Este documento traduz a visão discutida em requisitos. É um plano, NÃO uma afirmação de funcionalidades já criadas. Decisões ainda sujeitas a protótipo são identificadas como propostas.

## Pitch

Um Minecraft Java mais vivo, narrativo e explorável — sem obrigar o jogador a seguir uma campanha. Um conjunto de mods que se reconhecem: aldeias com moradores e propriedades, personagens com cultura e memória, rumores e livros, quests opcionais, mapas e HUD RPG, mochilas progressivas, itens e comida personalizados.

## Pilares de design

1. **O mundo não gira em torno do jogador.** Histórias e pessoas existem mesmo sem participação do jogador.
2. **História não é sinônimo de missão.** Um rumor ou item pode revelar algo sem criar um objetivo no diário.
3. **Consequências, não proibições arbitrárias.** Pode-se vandalizar uma propriedade; o dono restaura, reclama e pode chamar guardas.
4. **Modularidade real.** Cada módulo jogável pode operar com o mínimo Lux Engine, sem exigir todos os outros. Integrações só são habilitadas quando o provedor existe.
5. **Minecraft continua Minecraft.** Itens, drops, Ender Dragon, sobrevivência e construção vanilla permanecem úteis.
6. **Configuração e respeito ao desempenho.** HUD ajustável, regras por servidor, IA limitada a chunks carregados, salvamento consistente.
7. **Criar valor com sistemas existentes.** Profissões, camas, moeda regional, pérolas, estruturas e comida passam a ter mais significado.

## Loop do jogador

Explorar → descobrir uma região/local → conversar/perguntar/ler → negociar ou aceitar uma oportunidade → agir livremente (craft, construir, minerar, lutar) → mundo e NPCs reagem → equipar/melhorar itens, mochila e habilidades → continuar.

Não há obrigação de completar uma missão principal. O caminho ao End é uma **investigação opcional**, não uma campanha que bloqueia o dragão.

## Estrutura de jogo

- **Exploração:** bússola + mapa (Atlas), títulos cinematográficos para biomas e vilas (Discovery).
- **Social:** villagers, piglins, pillagers e bruxas têm falas e comportamentos dependentes de inventário, profissão, cultura, personalidade, memória e reputação (NPC/Lore).
- **Sociedades:** vilas nascem de tamanhos variados, associam edifícios/camas/profissões aos moradores, consertam danos e respeitam limites de propriedade (Villages). Crescimento de tier da vila fica fora da v1.
- **Missões:** contratos do NPC persistem 2–3 dias de jogo entre revisões, com necessidades simuladas por sorteios no ciclo diário, compartilhadas no servidor; quests especiais podem ter grafos/blueprints (Quests).
- **Progressão:** níveis RPG persistentes e perks sem destruir os usos vanilla de XP (Progression).
- **Interface:** rosto da skin/vida/fome/XP no canto superior esquerdo, companheiros menores abaixo, hotbar + bússola embaixo, HUD dinâmica e rastreador opcional (HUD).
- **Inventário:** dez níveis de mochila, hotbar intacta, slots vanilla bloqueados/liberados, expansão controlada além dos 27 slots; upgrades via bancadas ou serviços especializados (Inventory).
- **Conteúdo:** motor de itens por pacotes + editor web de sprite/voxel e previews (Items/Studio); alimentos prontos e bebidas sem crafting na primeira versão (Food).

## Uma cena que representa a visão

O jogador chega a uma vila montanhosa coerente com a encosta. Surge “Pedra Alta — local descoberto”. Atlas registra o nome. O bibliotecário oferece comércio, rumores e uma conversa especial ao ver uma pérola no inventário. O ferreiro está irritado porque alguém fechou sua porta com blocos; remove o obstáculo e restaura a parede danificada. O taverneiro talvez precise de carvão durante os próximos dias. O jogador pode ajudar, ler um livro, negociar ou ir embora. Não há trilho narrativo.

## Referência de interface

- Diálogo cinematográfico: personagem visível de um lado, opções na parte inferior, comércio no painel oposto (referências de linguagem visual: Witcher/Skyrim; não copiar arte).
- HUD que desaparece na exploração e reaparece por ação/contexto (referência funcional: RPGs de mundo aberto).
- Descoberta de locais: título discreto/cinematográfico, respeitando cooldown e preferência do jogador.
- Alimentos e itens: visual autoral pixel/voxel coerente com Minecraft.

## Público e multiplayer

Sobrevivência solo, grupos cooperativos e criadores de modpacks. Mundos de teste singleplayer e servidor dedicado são ambos obrigatórios. Necessidades de NPC e nomes de vilas são compartilhados no mundo; conhecimento e crimes podem ser individuais por jogador; ações simultâneas não geram recompensas ou itens duplicados.

## Definição de sucesso da primeira versão integrada

Um mundo de teste executa Engine + HUD + Atlas + Discovery + NPC + Villages + Quests + Inventory e demonstra: entrada em vila, nome consistente no mapa, conversa e comércio, necessidade persistente, reparo de edifício, mochila que respeita crafting, HUD dinâmica, sem crashes, exploits óbvios ou perda de inventário. Outros módulos podem ser incorporados gradualmente.

## Escopo que NÃO cabe em v1

Campanha principal obrigatória; sistema econômico real de demanda/estoques; vilas subindo de tier e construindo novos bairros; alimentação automática; temperatura e sede; launcher próprio; mapa planetário; milhares de diálogos antes de validar o motor; unificação universal de minérios; integração irrestrita com qualquer mod externo.

## Índice do restante

Ver 01-VISAO-E-ESCOPO, 02-MODULOS, 03-ARQUITETURA, 04-ROADMAP, 05-RISCOS-E-DECISOES, 06-QUALIDADE-E-TESTES, docs/modulos e docs/exemplos.

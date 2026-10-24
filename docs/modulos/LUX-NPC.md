# Lux NPC — Conversa, cultura, comércio e memória

## Visão
Villagers, piglins, pillagers e bruxas não são botões de troca; são habitantes com profissão, conhecimento, cultura, personalidade, memória e consequências sociais.

## Interface de conversa
Ao iniciar conversa, jogador perde movimentação voluntária, mas o mundo não é pausado em multiplayer. Personagem fica visível de um lado; diálogo/opções na faixa inferior; opção **Negociar** abre lista de comércio do lado oposto; sair/restaurar controles. Prever dano e interrupções para não deixar o jogador indefeso sem saída.

## Nomes e profissão
NPC recebe nome/ID persistente. Villagers vinculados a profissão, casa e estação pelo Villages quando presente. Graus de experiência profissional sorteados por NPC (novato/aprendiz/competente/especialista/mestre); **não** subir de tier meramente por comprar muitas vezes. Tiers altos podem surgir sem raridade artificial obrigatória. Catálogo individual variável e maior nos tiers avançados; bibliotecários podem vender vários livros diferentes, sem listas idênticas.

## Comércio cultural
- **Villagers:** esmeraldas; comércio numa aba da conversa; preços/variedade por NPC.
- **Piglins:** ouro como moeda, NÃO apenas jogar ouro e receber drop aleatório quando sistema ativado. Cada um oferece poucas opções sorteadas (ex. quatro) de catálogo amplo.
- **Bruxas:** escambo de drops e materiais (ex. olho de aranha por fungo, teia por pérola), sem moeda universal.
- **Pillagers:** negociação condicional à relação/facção; esmeraldas possíveis, moeda a confirmar.
- Possibilidade de comerciar, só conversar, ouvir rumores ou receber contratos; nem todo NPC tem trabalho no dia.

## Condutas culturais
Piglins abordam jogador sem ouro equipado, oferecem escolha: vestir, recusar, negociar; prometer sem cumprir gera hostilidade após prazo; falha e recusa não são tratadas como bug. Pillagers/bruxas podem exigir reputação ou condições futuras; não tornar todos hostis passivos permanentemente — preservar combate e sua IA quando diálogo indisponível.

## Item e conhecimento
Quando jogador porta/mostra pérola do Ender a bibliotecário, pode perguntar sua origem. Cabeça de Wither Skeleton: clérigo assusta-se/aciona guardas dependendo do contexto, bruxa se interessa, piglin pode ignorar. Lã com tecelão abre conversa sobre ofício. NPC só reage a item escondido no inventário **se o jogador abrir o diálogo/escolher assunto** (não é leitura mental automática).

## Memória
Dono de imóvel registra perturbações repetidas, abertura indevida de container e danos; advertência, reclamação, perda de reputação e guardas graduais. Guardas fazem parte de uma interface de proteção/ordem ligada ao Villages; avaliar integração opcional com mods de guarda, mas comportamento do Lux NPC independe deles.

## Dependências e aceite
Engine; Lore e Quests opcionais; Villages para lotes/endereços. Um NPC lembra sua necessidade/último diálogo após reload; dois jogadores veem comércio consistente; piglin respeita regra do ouro; diálogo nunca duplica itens/escapa custo; hostilidade regressa quando diálogo encerra.

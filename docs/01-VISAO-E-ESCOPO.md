# Visão, princípios e fronteiras

## Norte do produto

“Dar significado às coisas que já existem no Minecraft” é mais importante do que inflar o catálogo de mecânicas. As ideias vêm de modpacks, RPGs abertos e jogos com cidades vivas. O nome de um bioma, a cama de um ferreiro, o conhecimento de uma bruxa ou uma pérola do Ender devem importar.

## Regras de produto

- Preferir **profundidade percebida** a simulação cara. Exemplo: taverneiro com necessidade sorteada e persistida, em vez de calcular abastecimento econômico real.
- Nenhuma missão obrigatória para enfrentar o Ender Dragon; mecânicas vanilla continuam acessíveis.
- NPCs não devem saber tudo. A profissão sugere assuntos; personalidade e fatos anteriores alteram a reação.
- Conteúdo narrativo pode ser encontrado fora de ordem, ignorado ou se tornar indisponível depois de certos acontecimentos. Não tentar “corrigir” todas as oportunidades perdidas.
- A aldeia é uma sociedade autônoma. Casas e ruas pertencem a ela; o jogador não tem poder automático de cancelamento de manutenção ao vandalizar áreas públicas.
- Edifícios criados pelo jogador **fora** dos lotes/ruas públicos reservados não podem ser demolidos pelas rotinas da vila.
- Nada de valores visuais ou mecânicas intrusivas sem opção de desativação.
- Não presumir que uma categoria de mod instalada permite acesso a toda a sua API. Usar integração por adaptadores e detecção de disponibilidade.

## Escolhas já acordadas

- Lux Engine conecta módulos Lux, sem impor gameplay novo por si só.
- Lux Villages nasce em um tamanho/tier e **permanece nesse tier na v1**; população pode mudar e prédios podem ser restaurados/deteriorados.
- Necessidades são por NPC, em janelas de 2–3 dias do jogo, sorteadas em revisão diária e compartilhadas no multiplayer; reabrir conversa NÃO sorteia outra vez.
- HUD RPG clean, sem indicadores de sede/temperatura, com modos e autoocultação.
- Lux Inventory tem 10 níveis; melhora por bancada ou coureiro, e melhorias especializadas são serviços de NPC sem encaixes obrigatórios.
- Lux Food v1 inclui bebidas e alimentos prontos, inclusive exclusivos das tavernas, **sem receitas novas**.
- Lux Materials, Lux Launcher e crescimento automático de tiers ficam em standby.

## Propostas ainda não fechadas

- Loader/versão definitiva (candidato: NeoForge 1.21.1, Java 21).
- Preços exatos das dez melhorias e porcentagem final de economia do coureiro.
- Distribuição de níveis de comerciantes (amostragem sem raridade forçada vs personalização por modpack).
- Como representar virtualmente grupos de 128 e slots além dos 27 sem quebrar crafting de terceiros.
- Política de mundo compartilhado quando Ender Dragon já morreu mas um jogador novo chega.
- Limites para guardas, invasão e proteção contra grief em servidor público.
- Formato de pacotes e recursos dinâmicos do Lux Items.

## Critério para admitir uma feature

Ela deve aumentar a variedade da experiência, não destruir o equilíbrio e ter teste/telemetria local reproduzível. Cada requisito recebe: dono, gatilho, estado persistido, regras multiplayer, casos extremos e critério de aceite. Se não couber no primeiro release, registrar explicitamente em backlog.

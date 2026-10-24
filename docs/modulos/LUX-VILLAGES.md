# Lux Villages — Comunidades autônomas

## Visão e limite da v1
Aldeias com arquitetura por bioma/profissão, topografia coerente, nomes persistentes, moradores reais com cama e emprego atribuídos e manutenção autônoma. Cada vila nasce com tier/tamanho determinado e **não sobe de tier nem ergue novos bairros na v1**. População, ocupação, abandono e conservação podem mudar naturalmente.

## Geração inicial
- Priorizar planícies, desertos, campos gelados e terrenos amplos/menos inclinados.
- Permitir vila em colina/montanha quando houver platô/vale/encosta viável: fundações, terraços, caminhos, escadas/passarelas, acessibilidade de portas e prevenção de blocos flutuantes.
- Edifícios variantes por profissão: ferreiro, bibliotecário, fazendeiro, clérigo, residências e praça; casas pequenas/médias/grandes coerentes com tier sorteado.
- Tier 1: praça/templo/oficina básicos e nem todas as profissões necessariamente presentes; tier 2 mais edifícios e catálogo de profissões; tiers maiores como templates iniciais se forem viáveis, sem upgrade automático posterior.
- Geração substitui/expande estruturas em **chunks novos**; não reescrever vilas já existentes automaticamente no MVP.

## Território, propriedade e construção
Cada edifício tem ID estável, mask/lote 3D, blueprint de blocos esperados (inclusive ar livre), proprietários e camas. Ruas e praças são propriedades coletivas da vila, com rotinas de manutenção próprias. Exterior sem lote/rua é terreno livre e deve respeitar construção do jogador, mesmo próximo da vila.

**Decisão corrigida e central:** se um jogador colocou bloco na porta de uma propriedade municipal, ou quebrou parede, o morador PODE remover/reconstruir. A autoria jogador **não** impede manutenção dentro da propriedade; ao contrário, provoca reclamação e possíveis guardas. Enderman/Creeper/incêndio também causam reparos. Não invadir nem reconstruir casa de jogador fora de área municipal.

O algoritmo de manutenção precisa distinguir:
- planta esperada + posição dentro da máscara;
- discrepância/obstáculo detectada com causa conhecida quando possível;
- entidade/contêiner/valor do bloco que seria alterado;
- permissão administrativa do mundo;
- prioridade e custo da tarefa.

Nunca sobrescrever baús com conteúdo, block entities desconhecidos, blocos de proteção de terceiros ou entidades. Em caso duvidoso, registrar conflito para diagnóstico, não apagar silenciosamente.

## Residência, camas e emprego
Uma biblioteca com cinco camas gera/capacita cinco bibliotecários iniciais; ferreiro com duas camas, dois ferreiros. Atribuir cama individual e local de trabalho de forma persistente. Não permitir cama aleatória se existe vínculo válido. Exemplos de agenda: trabalhar de dia, socializar, retornar ao próprio quarto de noite. Rotinas vanilla precisam de integração, não somente atribuição inicial de profissão.

## População dinâmica sem upgrade de tier
Nascimentos, crescimento vanilla e mortes podem alterar ocupação; vagas são preenchidas por descendentes que aprendem profissão ou imigração eventual. Nenhum villager novo simplesmente spawn infinito ao morrer outro. Limite populacional vem da capacidade de camas/estabelecimentos. Negociação não eleva tier profissional; experiência inicial e eventual sucessão são conceitos separados.

## Conservação e abandono
Prédio vazio/sem profissionais por vários dias ganha fases de deterioração: sinais visuais, teias, musgo, problemas estruturais pontuais. Quando novo morador assume, pode restaurar gradualmente. Não destruir conteúdo de jogadores em baús nem impedir reocupação. Parâmetros por prédio, dias e efeitos são configuráveis.

## Reparos autônomos
Quando creeper explodir ou jogador bloquear acesso, o responsável recebe ticket. Morador aproxima-se, realiza animação de trabalho e altera poucos blocos válidos a cada etapa; ao concluir ticket retorna à rotina. Outro morador pode substituir responsável indisponível. Caminhos públicos têm responsáveis coletivos.

## Infrações e guardas
Dentro de propriedade: dano, obstrução repetida, uso indevido de baú e roubo podem gerar memória de infração, diálogo de reclamação, pedido de retirada, reputação ruim e acionamento de guardas conforme gravidade. Criar interface própria de guardas; suporte opcional a Guard Villagers somente com versão compatível. Não pressupor que um mod de guardas terá todas as APIs necessárias. Guardas devem evitar perseguição eterna, loops de agressão e punição de jogadores inocentes.

## Desempenho e persistência
WorldRegistry armazena dados por ID; chunk-loaded executa IA e alteração de blocos de forma limitada; settlements off-chunk avançam somente estados agregados como datas, vagas ou abandono por avaliação preguiçosa ao carregar, sem tick de NPC ausente. Integrar com spawn/death/respawn mantendo segurança de migração.

## Casos de teste aceitos
(1) Planície gera vila acessível. (2) Montanha não gera casa torta/pairando. (3) Ferreiro usa cama da forja. (4) Bloco colocado por jogador na porta do bibliotecário é removido sem tocar em casa vizinha do jogador. (5) Casa fora do lote fica intacta. (6) Forja vazia se deteriora e pode ser restaurada. (7) Tier inicial não muda.

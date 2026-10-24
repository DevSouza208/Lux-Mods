# Lux Progression — Níveis e perks

## Objetivo
Dar evolução permanente ao personagem sem transformar XP de encantamento em recurso conflituoso. O jogador decide como gastar pontos de habilidade.

## Regra proposta
- **XP vanilla continua**: níveis/orbes consumidos em encantamento, bigorna e Remendo conforme jogo.
- **XP de personagem é separado**: ganho por atividades configuráveis, determina level RPG, não é gasto.
- Subir nível RPG concede pontos de perk; pontos ficam persistidos por jogador, com validação no servidor.
- Atributos exemplares: combate (dano/defesa), exploração (velocidade/fôlego), sobrevivência (mineração/coleta). Valores no protótipo são ilustrativos, sujeitos a testes.
- Progressão sobrevive à morte, salvo regra por servidor alterada explicitamente.

## Considerações de equilíbrio
Não dar velocidade ou dano tão altos que eliminem o risco. Evitar bônus escondidos multiplicativos de mods externos. Impor teto por perk e respeitar efeitos vanilla. Configurar recompensas para objetivos de exploração sem forçar quests.

## Interfaces
Uma árvore com abas, custo, pré-requisitos, histórico de desbloqueios e opção de redefinir build se o administrador habilitar. HUD pode mostrar level RPG; encantar continua mostrando XP vanilla claramente.

## Dados
PlayerSkillState { level, totalXp, unspentPoints, acquiredPerks[], schemaVersion }. Idempotência em eventos (evitar XP duplo por kill/crafting/travel). API expõe somente efeitos autorizados.

## Fora da v1
Classes fixas, árvore gigantesca, magia de combate autoral, PvP competitivo balanceado, integração com todo mod de arma.

## Aceite
Encantamento usa XP vanilla normalmente; XP RPG persiste sem ser gasto; morte respeita config; atributos aplicados uma vez; reset não gera pontos infinitos.

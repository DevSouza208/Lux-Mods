# Lux Quests — Contratos e grafos de objetivos

## Proposta
Missões não são a narrativa principal. São oportunidades locais: um taverneiro pede carvão/poções, uma bruxa pede ingredientes reais de uma receita, um piglin oferece contrato para reconstruir estátua de ouro. Cada objetivo pode ser ignorado.

## Duas categorias
- **Contratos contextuais:** templates por profissão, sorteados para **NPC específico** com validade medida no dia de jogo; não dependem de economia real.
- **Missões escritas/descobertas:** grafos de etapas e condições opcionais, com ramificações e consequências que só disparam quando regras são satisfeitas.

## Regra central de disponibilidade
Necessidade pertence ao NPC, não à conversa. Sorteio/revisão em gatilho do ciclo de dia/noite do Minecraft, com prazo 2–3 dias (configurável). Durante vigência, todos os jogadores veem a MESMA oferta, quantidade, preço e prazo. Reabrir diálogo NUNCA rerrola. Se chunk ficou descarregado, ao carregar compara gameDay e agenda avaliação limitada; não executa milhares de eventos retroativos.

Ex.: Alberto taverneiro, dia 12, precisa de 24 carvões; dias 13 e 14 oferta permanece; ao dia 15, se não reservada, sistema decide manter, substituir ou encerrar. Contrato aceito não desaparece por outro sorteio; reserva compartilhada pode ser individual/cooperativa conforme tipo e mantém deadline separado.

## Grafo de nós
Tipos iniciais: TriggerDialogue, HasItem, DeliverItems, VisitLocation, KillEntity, WorldFact, WaitGameDays, BuildStructure, Branch, SetFact, Reward, Complete, Fail/Expire. Não permitir ciclos infinitos sem guardrails. Identificadores persistentes por instância, schemaVersion e migração.

## Exemplo — estátua piglin
Ao conversar com líder em povoado, recebe blueprint de estrutura. Projeto na mão mostra ghost de blocos, mira no terreno, segurar direito constrói progressivamente usando itens reais e valida permissão. Construção manual também é reconhecida. Conclusão verifica estado real dos blocos e paga uma só vez, nunca só contador. Regras de world modification e server-side.

## Coop e segurança
Necessidade disponível pública; aceitação individual ou grupo deve ser explícita. Dois jogadores entregando simultaneamente não duplicam recompensa. Itens devem ser efetivamente consumidos antes do commit da recompensa. Prazo em dias Minecraft; suspensão de time ao mundo parar segue game time.

## Não objetivos
Economia de abastecimento real, NPC esperando passivamente para sempre, missões atreladas obrigatoriamente ao End, geração de quests sem validação do contexto.

## Aceite
Offer estável multiplayer, revisão por dia sem reroll de diálogo, persistência após restart, contrato expira corretamente, pagamento único, missão opcional não bloqueia jogo base, grafo suporta decisões sem corrupção.

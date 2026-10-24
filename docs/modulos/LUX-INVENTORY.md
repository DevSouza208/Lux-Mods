# Lux Inventory — GDD 1.0

## Objetivo
Mochila é o próprio armazenamento do jogador, não um container separado. Hotbar mantém 9 slots. Interface usa até 27 slots de inventário vanilla com disponibilidade progressiva, depois abre storage adicional. Crafting comum deve acessar os itens carregados; integradores externos são caso a caso.

## Capacidade: dez níveis

| Nível | Slots armazenagem | Hotbar | Total |
|---|---:|---:|---:|
| 1 | 6 | 9 | 15 |
| 2 | 9 | 9 | 18 |
| 3 | 12 | 9 | 21 |
| 4 | 15 | 9 | 24 |
| 5 | 21 | 9 | 30 |
| 6 | 27 | 9 | 36 |
| 7 | 36 | 9 | 45 |
| 8 | 45 | 9 | 54 |
| 9 | 54 | 9 | 63 |
| 10 | 63 | 9 | 72 |

Nível inicial gratuito; nove melhorias para atingir 10. Os números são **capacidade proposta**, sujeitos a validação em runtime. Níveis 7–10 exigem expansão física além dos 36 vanilla.

## Aquisição de capacidade
- O próprio jogador pode melhorar na bancada usando mais materiais, sem exigir NPC.
- Coureiro oferece serviço especializado: economia sugerida ~30–40% dos materiais em troca de esmeraldas. Nível/qualidade do coureiro pode influenciar custo; manter preços configuráveis.
- Recursos iniciais comuns (couro/linha/ferro), recursos difíceis somente para níveis avançados; evitar forçar Nether para recuperar capacidade vanilla.
- Melhorias são persistidas no estado da mochila vinculado ao personagem.

## Melhorias funcionais — aplicadas à mochila, SEM slots de módulos
1. Empilhamento: clérigo oferece bênção para compatíveis até 64; bruxa pode habilitar **agrupamento virtual 128**. Não colocar 128 reais no ItemStack vanilla; representar quantidades com storage gerido pelo Lux e operações atômicas.
2. Magnetismo: ferreiro apresenta 2× gratuitamente uma vez por jogador; 4× por serviço pago. Usuário pode selecionar modo desligado/2×/4× após desbloquear.
3. Reposição de blocos: pedreiro permite reabastecer hotbar de pilha equivalente quando acabar.
4. Reposição de ferramentas: ferramenteiro permite substituir ferramenta quebrada por compatível (e opção de não usar item raro/encantado).
Não permitir empilhar indiscriminadamente armas, armaduras e stacks com NBT/components distintos. Alimentação automática fica fora da v1.

## UI
Layout único: barra hotbar usual; slots de armazenagem exibidos como mochila, parte bloqueada por ícone de cadeado. Bloqueios não são itens físicos. Aba de melhorias com estados desbloqueados e toggles. Slots adicionais seguem a mesma interface sem obrigar abrir tela de baú.

## Contratos técnicos
- Capacidade 1–6: usar storage vanilla. Bloquear no GUI e nos handlers de inserção/coleta e shift-click server-side; slots sem direito não aceitam item.
- Capacidade 7–10: inventory handler extra persistente com adaptador comum do Lux, sincronização, crafting vanilla + recipe book suportados via ponte.
- Um mod externo que só consulta PlayerInventory vanilla não verá automaticamente slots extras; não prometer compat universal. Matriz de compat é responsabilidade do modpack final.
- Magnetismo não atravessa paredes por padrão, não ignora proteções/idade de pickup, não rouba drops reservados de outros players; budget por scan.
- Em caso de upgrade/downgrade não apagar stacks; upgrades padrão só ampliam. Redução forçada requer mover/soltar itens com confirmação e política de segurança.

## Morte e keepInventory
Decisão v1: upgrade de capacidade e funcionalidades pertencem ao progresso da mochila vinculado ao personagem; perda dos itens carregados segue keepInventory vanilla; os itens extras caem corretamente sem dupe. Mochila não é removível como baú portátil na v1. Testar exatamente com servidores e outros mods de tombstone; conflito declarado em vez de corrupção.

## Validação rigorosa
Testes de pickup, swap, quick-move, crafting manual/receita, slot de arma, baú, cross-dimension, network lag, itens com components, logout, death/respawn. Simulações automáticas de invariantes: soma de quantidades pré/pós-movimento igual salvo consumo/drop legítimos. Logs de transação em debug.

## Não objetivos
Baú portable equipável, magnetismo que funciona através de paredes, alimentação automática, filtros/descarte na primeira entrega, duplicação ilimitada ou acesso irrestrito a qualquer mod externo.

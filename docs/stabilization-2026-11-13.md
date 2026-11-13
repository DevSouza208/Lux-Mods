# Lux Mods — rodada de estabilização, 13/11/2026

## Alterações enviadas nesta rodada (ainda NÃO compiladas/testadas)
- **Atlas**: cache de terrenos por mundo e dimensão; persistência em gzip local; coleta passiva de amostras de blocos próximos enquanto o jogador anda (somente chunks carregados); zoom inicial mais detalhado; sombreamento de relevo; cache LRU limitado; marcadores com posições baseadas no mundo; botão esquerdo seleciona, botão direito move o mapa. Não gera chunks.
- **HUD**: efeitos ativos redesenhados como cartões verticais legíveis fora do topo das missões; avisos de perigo como vinhetas leves de borda vermelha (vida), âmbar (fome) e escura (ar), sem novos medidores no rodapé. Mantém o painel e a câmera existente.
- **Quests**: coletar o mesmo item repetidamente após jogar no chão não deve aumentar o progresso; a missão agora compara a maior quantidade válida presente no inventário.
- **Lux Rest / Healing**: a cura passiva perto de fogueira e ao sentar foi removida; sono restaura vida; item `lux_hud:bandage` restaura 2 corações com cooldown; receita 1 papel + 1 linha + 1 frasco de mel gera 2 curativos; sprite provisório usa textura de papel vanilla.
- **Regeneração natural**: nova opção de servidor `disableNaturalHealing=true`; ao entrar no mundo, a configuração desativa a gamerule naturalRegeneration, impedindo cura vanilla por comida/saturação. Efeitos e curativos ainda curam.
- **Câmera**: não alterada. O usuário já confirmou que F5 normal + Shoulder Surfing funciona quando inicia em primeira pessoa.

## Pendências reais, sem fingir que já foram implementadas
- **Thirst Was Taken 2.1.5**: a sede NÃO foi movida para o painel superior porque falta confirmar API de leitura e cancelamento/posição do overlay da versão instalada. Não esconder o indicador atual sem alternativa funcional. XP continua no HUD até essa integração; futura aba Upgrades conterá XP.
- **Lanterna na mão**: bug de transform do item com possíveis interferências entre Punchy, First Person Model, Not Enough Animations, Better Lanterns e renderers. No slot Curios/Lamb Lanterns a lanterna funciona. Não foi alterada a câmera nem remoção automática de mods.
- **Estamina** e **Lux Inventory**: ainda não implementados.
- **Validação da compilação**: o ambiente atual não tem acesso ao repositório completo e dependências Gradle para build Minecraft. O usuário deve executar seu atualizador somente após revisar o status, e enviar logs se `BUILD FAILED`.
- **Dados do mapa**: o novo cache é local ao cliente. Tem limites e deve ser testado em mundos diferentes, mudança de dimensão e depois de fechar/reabrir.
- **UI de efeitos**: exige revisão visual para sobreposição com card de missões e escala F8.

## Testes após compilar (em mundo cópia)
1. Checar criação do item e receita da bandagem; `/give @s lux_hud:bandage`.
2. Comer cenoura com vida baixa: não deve regenerar. Curativo recupera 4 HP, sono restaura tudo.
3. Abrir atlas, andar longe, reabrir e voltar, confirmar persistência do mapa; clicar/mover e avaliar marcadores.
4. Verificar vinhetas no limiar de vida, fome e ar. Verificar cartões dos efeitos em 60%/75%/100%.
5. Coletar tronco, largar, recolher: não deve avançar a missão novamente.
6. Testar lanterna na mão: comparação com o First Person Model desativado, depois Punchy, em cópia da instância. No Curios manter por enquanto.

## Rodada adicional
- Cache Atlas: corrigido erro no caminho de salvamento com parâmetro `kind`.
- Coordenadas Atlas: a grade usa células de 2 pixels. Conversões de clique, arraste, posição do jogador e marcadores agora aplicam `step / 2` corretamente. A discrepância anterior era a causa principal da aparente movimentação de marcadores.
- Persistência passiva: grava a cada ~4 segundos de descoberta, reduzindo perda em sessões curtas.
- Cartões de efeitos: quantidade calculada pela altura disponível conforme escala F8 para não ultrapassar a tela.
- CI: compilação separada do job `pure-java`; o GitHub Actions ainda falha imediatamente sem registrar etapas, portanto continua sem prova de build.
- Sede opcional experimental: leitura reflexiva e somente de leitura do objeto `ThirstBarRenderer.PLAYER_THIRST` e `getThirst()` com janela de inicialização por mundo. Se o port NeoForge 1.21.1 conservar essa API, terceira barra vira sede e overlay `thirst:*` é cancelado. Se não, mantemos XP na terceira barra e a barra original do Thirst Was Taken intacta. Não supor que esta integração esteja validada.
- Lanterna na mão: continua dependente de identificar qual mod altera a transformação de item; não alterar code da câmera aprovada nem desinstalar mods automaticamente.

### Observações sobre a próxima compilação
Sem runners ativos e sem fontes NeoForge completas locais nesta sessão, as mudanças não passaram por um build verificado.
Conferir especificamente: importações / assinaturas das classes opcionais; novo item `lux_hud:bandage`; cache Atlas; HUD de efeitos; troca da barra sede/XP após 3 segundos de mundo carregado.

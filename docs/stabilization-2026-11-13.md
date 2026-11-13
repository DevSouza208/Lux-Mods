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

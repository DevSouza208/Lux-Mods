# Lux HUD — MVP reconstruído

Escopo: **HUD de jogador, sem novos sistemas de gameplay**. Preserva os módulos já existentes de descanso, bandagem, montaria e equipe sem reescrever seus dados nesta etapa.

## Exibição
- Modo RPG: retrato + nome + **nível real de XP**, vida e fome. Sem sede, sem barra XP vanilla.
- Submerso: o indicador de **ar (segundos restantes)** ocupa temporariamente a posição da vida. Dano de afogamento permanece vanilla.
- Hotbar: Minecraft desenha os 9 slots e os itens reais; Lux desenha só a moldura opcional por cima.
- Modo CLASSIC: indicadores vanilla preservados.
- F8: escala do painel. Configurações locais existentes permanecem compatíveis.

## Limites
- **Thirst Was Taken foi desinstalado**: não consumir/mostrar seus dados.
- Nenhum mod de combate pode ser ocultado globalmente. A seta de Combat Roll deve ser desligada na configuração desse mod.
- Os elementos AppleSkin podem ser desenhados por camada própria ou por evento global: se restarem ícones de saturação, desativar os overlays na própria configuração do AppleSkin.
- O HUD não deve alterar a pose/estado de natação do personagem. O antigo listener HudAutoSwim fica sem registro.
- Nada de adicionar missões ou interfaces de upgrades à HUD.

## Validação local antes de passar para o próximo módulo
1. Gradle: `assembleTestPack` compila pelo atualizador já usado no Windows, sem GitHub Actions.
2. Mundo de teste: modo RPG mostra 2 barras, XP apenas como número, sem sede.
3. Comer: valor de fome atualiza; não resta silhueta de fome vanilla.
4. Mergulhar/emergir: ar substitui vida e a vida volta ao sair da água.
5. Pausa/inventário: nenhuma camada vanilla escondida invade a interface; hotbar mantém os itens.
6. Modo Classic: volta a HUD original.
7. Sem alteração de mundos, inventários, progressão ou instalador.

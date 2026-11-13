# Lux Rest — regras de sobrevivência atualizadas

## Recuperação de vida
- **NÃO** regenerar vida por comida, saturação, ficar parado, ficar em pé ao lado da fogueira ou sentar longe do fogo.
- **PODE** regenerar com curativos, poções ou outros itens explicitamente medicinais.
- **PODE** recuperar toda a vida depois de dormir tempo suficiente numa cama.
- **PODE** regenerar lentamente enquanto estiver **sentado E perto de fogueira acesa**: 1 ponto de HP (meio coração) a cada 6 segundos, em raio de 4 blocos.
- Ao levantar, apagar a fogueira ou afastar-se, o contador de descanso é reiniciado.
- Sem estamina por enquanto.
- A opção de servidor `disableNaturalHealing=true` desativa a gamerule vanilla `naturalRegeneration` ao entrar no mundo.

## Atenção ao mod Sit!
A implementação considera uma entidade de assento identificável pelo caminho `seat`/`sitting` ou namespace `sit`/`sitmod`. A identidade precisa do assento gerado pela versão instalada do Sit! deve ser testada dentro do jogo. Outros mods que implementam apenas pose sentada podem exigir uma integração específica.

Status: código enviado ao GitHub, **não compilado nem testado in-game**. A integração com alimentos de outros mods capazes de aplicar efeitos de Regeneration/Instant Health ainda exige auditoria; a gamerule só impede regeneração natural.

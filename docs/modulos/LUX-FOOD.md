# Lux Food — Alimentos e bebidas

## Escopo estrito da 1.0
Pequena coleção bonita de itens comestíveis/bebíveis já prontos: refrigerantes, cervejas fictícias ou genéricas, sucos, bolinhos, sanduíches, pão na chapa e especialidades da taverna. **Sem crafting, novas plantações, panelas, receitas ou cozinha simulada na primeira versão.**

## Gameplay
- Itens restauram fome/saturação conforme dados de consumo (saturação existe na lógica vanilla, mas a HUD exibe barra de fome).
- Bebidas podem devolver recipiente quando fizer sentido; efeitos especiais opcionais, sem obrigar mecânica de embriaguez/temperatura.
- Taverneiros têm cardápio com ofertas autorais fixas e algumas opções variáveis por NPC; algumas especialidades são exclusivas da taverna por não existir receita no jogo.
- Não confundir “fome/saturação de item” com reintroduzir indicador visual de saturação. A HUD segue sem barra de saturação.

## Conteúdo e aparência
Sprites bem trabalhados, modelos pequenos para itens quando apropriado, embalagem visual consistente. Usar nomes e arte autorais; não copiar assets de Stardew Valley. Primeira coleção pode ter ~15–20 itens como meta artística, não obrigação funcional.

## Dependências
Lux Items para registro, Lux NPC para comércio (ou fallback de obtenção por comandos/criativo para testar isoladamente). Se Food for módulo autônomo, permitir obtenção de itens mesmo sem NPC via modo criativo.

## Evolução futura — standby
Ingredientes novos, cultivos, culinária à la Stardew Valley, receitas e bancadas. Novas receitas podem ser adicionadas sem alterar IDs de comidas já existentes.

## Aceite
Consumível anima, restaura valores esperados, recipiente devolvido corretamente, conteúdo sobrevive a save/reload, sem receitas adicionadas em v1, comerciantes vendem quando NPC está instalado.

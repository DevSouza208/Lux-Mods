# Lux Rest — regras confirmadas para testes

## Cura
- Comida e saturação não restauram vida. O Lux HUD aplica `naturalRegeneration=false` ao entrar no mundo quando `disableNaturalHealing=true`.
- Parado longe da fogueira: sem regeneração.
- Perto de fogueira **acesa**, parado por pelo menos 1 segundo: **meio coração a cada 10 segundos**.
- Sentado numa entidade de assento reconhecida, perto da fogueira acesa: **meio coração a cada 6 segundos**.
- Passar correndo, saltar, andar, usar veículos ou apenas aproximar-se: não cura.
- Depois de levar dano, a fogueira só volta a curar após **8 segundos sem receber novos golpes**.
- Sair de perto, apagar a fogueira, movimentar-se ou entrar em combate interrompe e reinicia o contador da cura por fogueira.
- Dormir por 80 ticks (4 segundos) restaura toda a vida.
- Curativos e poções continuam recuperando vida imediatamente, fora da restrição da fogueira.

## Implementação
`modules/lux-hud/src/main/java/dev/luxmods/hud/server/HudRestServerEvents.java`

O reconhecimento de assento identifica entidades cujo nome no registro contém `seat` ou `sitting`, ou cujos namespaces são `sit`/`sitmod`. É necessário conferir se o Sit! instalado registra esse tipo de entidade. Se não registrar, o comportamento sentado pode cair na recuperação lenta ou não ser reconhecido.

## Verificação pendente
- Compilar JAR NeoForge 1.21.1.
- Testar recuperação parado, sentado, correndo, após dano e em várias escalas de dificuldade.
- Auditar alimentos de outros mods que apliquem efeitos de cura diretamente: a gamerule `naturalRegeneration` não bloqueia `instant_health` nem `regeneration` concedidos explicitamente por algum mod.

Estado: alteração enviada ao GitHub; ainda não validada no jogo.

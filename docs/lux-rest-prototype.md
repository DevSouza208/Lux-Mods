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

## Sono e desmaio — Lux Rest (13/11/2026)
- Medição no servidor em tempo **do Minecraft** (1 dia = 24.000 ticks de ciclo de dia), não horas reais; tempo desconectado não conta.
- Antes de 48.000 ticks acordado: normal.
- De 48.000 a 72.000: vinheta escura progressiva e três letras Z discretas no canto superior direito.
- Aos 72.000 ticks: **um desmaio breve de 5 segundos**, com escurecimento visual, efeito de lentidão forte e cegueira curta; não causa dano e não obriga a dormir.
- Se continuar sem dormir, um novo desmaio pode ocorrer somente após outro dia completo, não a cada tick.
- Dormir por 80 ticks consecutivos reinicia o cansaço. Sentar na fogueira descansa a vida, mas **não** zera o sono.
- Sem barra permanente de sono, sem exaustão por fome/sede e sem restrição cotidiana de caminhada.
- O cliente recebe `HudSleepSnapshotPayload` periodicamente; não inventa contador baseado em tempo local.
- Possível conflito com phantoms e outros mods de sono; testar.
- **Pendente:** build real e teste in-game. O comportamento do tempo pulado por `/time set` também deve ser avaliado.

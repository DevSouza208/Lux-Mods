# Lux Rest — Protótipo 0.1 (Minecraft 1.21.1 / NeoForge)

## Experiência aprovada
- Comida restaura a fome, não a vida; usar `/gamerule naturalRegeneration false` para testar até que Lux Healing controle isso com segurança.
- Dormir por tempo suficiente (mínimo de 80 ticks de sono) e acordar restaura toda a vida. A estamina deve ser restaurada quando existir o módulo.
- Fogueira acesa a até 4 blocos: +0,5 coração a cada 6 segundos.
- Sentado por entidade de assento (não montarias vivas): +0,5 coração a cada 4 segundos.
- Sentado perto de fogueira: +0,5 coração a cada 3 segundos.
- Fome zero bloqueia a recuperação gradual. A cama recupera a vida integralmente.
- Poções continuam funcionando. Sem fraturas, doenças, sede ou outras penalidades.

## Atenção à compatibilidade com Sit
Essa primeira implementação reconhece apenas assentos que transformam o jogador em passageiro de uma entidade não viva.
Mods que só executam animação de sentar **não** são detectados nesta versão. Quando o nome e a versão
exata do mod Sit estiverem disponíveis, implementar integração opcional e testar no jogo.

## Estamina (próximo desenvolvimento)
- Caminhar não gasta; correr, nadar, saltar e combate gastam.
- Caminhar/parar recupera; sentar e fogueira aceleram; dormir restaura 100%.
- Estamina baixa causa lentidão e bloqueia corrida; fome baixa não causa lentidão diretamente.
- Não criar HUD fictício antes de implementar rede, estado persistente e eventos no servidor.

## Segurança
- Não altera automaticamente game rules dos mundos existentes.
- Não impõe regeneração nem efeitos em jogadores no criativo ou espectador.
- A lógica ainda precisa de compilação Gradle e teste no jogo; não deve ser anunciada como validada.

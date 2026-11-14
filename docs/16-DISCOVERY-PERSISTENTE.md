# Lux Discovery — protocolo de descoberta persistente (implementação experimental)

**Estado:** branch \`feat/engine-discovery-bootstrap\`; aguarda build NeoForge e testes no jogo.

## Fluxo
1. \`PlayerTickEvent.Post\`, no servidor, observa o bioma do jogador carregado.
2. \`DiscoveryDebouncer\` segura fronteiras por 35 ticks e cooldown por 120 ticks.
3. Identificador de descoberta: \`dimensionId|biomeId\` (mod namespace).
4. Servidor consulta \`DiscoveryAttachments.SEEN_BIOMES\`, uma lista de strings serializada para Set via Codec, pertencente ao player.
5. Se ainda não visitou: grava novo set com \`setData\` (evita mutação sem marcar dirty), e envia payload **somente àquele jogador**.
6. Cliente traduz o nome e apresenta o título, salvo preferência de desativação visual.
7. Attachment \`copyOnDeath\` preserva descobertas quando o player morre.

## Segurança
- Cliente não escolhe novos biomas; nenhum payload C2S de descoberta.
- Servidor decide estado. IDs pertencem ao mundo/arquivo do jogador.
- Um jogador não recebe descoberta de outro. O payload anuncia somente título.
- O histórico registrado continua crescendo mesmo com títulos desativados no cliente.

## Limitações e testagem necessária
- Validação de compilação Gradle e jogo real **ainda não efetuada** devido GitHub Actions falhar antes do job e ambiente local sem downloads externos.
- Títulos são via overlay vanilla, não UI cinematográfica própria.
- Contagem de biomas/estruturas adicionais, naming de vilas e IDs de POIs ficam para Atlas/Villages.
- Alterar configurações server exige seguir sync/arquivo de mundo do NeoForge; valor único por servidor.
- Multiplayer: testar 2 jogadores, logout/relogin, morte, troca de dimensão, mundos distintos, dedicated server.
- Se a linguagem for diferente, fallback do nome é um humanize simples em inglês.
- \`discovered\` dentro do Debouncer apenas evita repetir validação numa sessão; **Attachment** persiste entre sessões e é a autoridade.

## Referências
- https://docs.neoforged.net/docs/1.21.1/datastorage/attachments/
- https://docs.neoforged.net/docs/1.21.1/networking/payload/

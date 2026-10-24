# Plano de qualidade, testes e critérios de aceite

## Pirâmide de verificação

1. **Unitários:** seleção de necessidade, grafo de quests, ocupação de slots, regras de propriedade, progressão.
2. **Integração server-side:** carga e migração de estado, filas de reparo, transferências de inventário, geração de vila.
3. **Jogo real:** client NeoForge, servidor dedicado, multiplayer 2–4 jogadores e smoke de distribuição.
4. **Conteúdo/UX:** português legível, nomes, diálogos, bibliotecas, HUD em diferentes resoluções/GUI scales.
5. **Performance:** TPS, tempo por tick, memória, FPS, carga de chunks, intervalo entre avaliações e tamanho do save.

## Cenários críticos

### Inventory
- Nível 1 libera 6 slots mais hotbar; nível 6 chega aos 27 originais; nível 10 chega a 63 de armazenagem, sem cortar pilhas.
- Pickup com slots bloqueados; shift-click em baú, funil e craft; comandos; item descartado.
- Livro de receitas e crafting manual com itens de slots internos e extras.
- Upgrade 64/128: merge/split, stack com metadata/enchantments, cliente de rede lento, morte, logout, save reload, troca de dimensão.
- Falha de sync NÃO duplica e NÃO apaga. Build rejeita dupe detectado.

### NPC, Lore e Quests
- Dois jogadores veem a mesma demanda do NPC no mesmo dia de jogo; abrir conversa várias vezes mantém oferta.
- Dormir/pular noites e descarregar chunks respeita revisão sem processar infinidade de dias passados.
- Dois jogadores entregam último item ao mesmo tempo: uma única conclusão/recompensa.
- Mostrar pérola ao bibliotecário, perguntar ao clérigo/bruxa/piglin por cabeça de Wither Skeleton gera respostas diferentes.
- Narrativa opcional do Fim: dragon vanilla livre de quest, fallback quando já derrotado, livros e contexto coerentes.
- NPC hostil pode dialogar sem desarmar todo perigo; pigmentação de armadura de ouro para piglin respeitada.

### Villages
- Vilas planas com caminhos e portas acessíveis; montanha com fundações e sem edifícios suspensos.
- Cada cama reservada a um morador válido; profissão e local de trabalho coerentes.
- Bloco colocado pelo jogador na porta de imóvel municipal é removido/reagido; parede quebrada é restaurada.
- Construção de jogador fora dos lotes públicos não é danificada; sem “reparo” fora da máscara.
- Morte dos dois ferreiros permite deterioração gradual; NPC substituto pode recuperar sem trocar tier da vila.
- Atacar morador aciona guardas de acordo com regra; NPC não persegue indefinidamente em dimensões distintas.

### HUD/Discovery/Atlas
- Fade sem piscar ao cruzar biomas repetidamente; mapa não marca local inexistente.
- HUD desaparece quando exploração tranquila e reaparece em dano/troca item/combate.
- Barra superior mostra cabeça da skin, saúde, FOME (não saturação) e XP/nível.
- Painel de party só mostra jogadores autorizados; não expor localização de outros jogadores sem regra.

### Items/Studio/Food
- Manifesto válido/ inválido; caminho malicioso rejeitado; item registrado só na inicialização.
- Render de ícone, primeira/terceira pessoa, chão e colocação quando tipo suportar.
- Alimentos prontos não possuem receita em v1; compra por NPC aplica item correto; servidores/clientes concordam em pack hash.

## Gate de publicação

Nenhum bug crítico de corrupção/dupe; dedicated server inicia e salva; UI tem modo desligado; módulos não usados não causam erros; instalação e atualização documentadas; world backup testado. Registrar versões suportadas e dependências exatas antes do release.

## Procedimentos

- Comandos de teste e métricas serão preenchidos quando o workspace de desenvolvimento existir.
- Não declarar “testado” sem evidência: log, comando executado, commit, cenário e resultado.
- Testar migração de mundo em cópia. Nunca rodar protótipos destrutivos em saves pessoais sem backup.

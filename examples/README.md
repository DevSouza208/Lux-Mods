# Exemplos de dados — CONTRATOS ILUSTRATIVOS

Os arquivos desta pasta representam protótipos de schemas para orientar o desenvolvimento de Engine, Items, Lore e Quests. **Nenhum deles é executado pelo Minecraft ainda** e os nomes de campos podem mudar após o primeiro parser ser implementado.

- pack de espada simples: examples/item-pack/manifest.json e items/espada_prata.json;
- falas condicionais de bibliotecário: examples/npc/dialogue-bibliotecario.json;
- demanda persistente de NPC: examples/quests/need-taverneiro.json;
- grafo de contrato de construção: examples/quests/estatua-piglin.json;
- livros e fatos do End: examples/lore/olhos-antigos.json;
- configuração separada client/server: examples/config/.

Regras: IDs estáveis, estado autoritativo no servidor, strings localizáveis, validação rigorosa, nunca carregar código arbitrário de pack de conteúdo.

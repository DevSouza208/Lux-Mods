# Lux Studio — App web de produção de assets

## Visão
Ferramenta web pensada desde o início para Minecraft. Não tentar reconstruir Aseprite e Blockbench completos antes de conseguir exportar um único item. O diferencial é **preview contextual correto**, não apenas desenhar pixels.

## Áreas de trabalho
1. Editor 2D pixel art: canvas com grid, palette, lápis, seleção, borracha, layers, undo/redo, transparência, tamanho correto de textura e animação em frames.
2. Editor 3D voxel/cubos: seleção de cubos, pivôs, UV, transforms, textura, transparência, iluminação e animações simples.
3. Painel de propriedades: item vs bloco, categoria e behavior template, escala/posição na mão, chão, moldura, UI; para blocos suportados, colocação piso/parede/teto, collision shape, luminosidade 0–15, opacidade e orientação.
4. Preview sandbox: inventário/hotbar, mão em 1P/3P, item no chão, modelo no corpo para Curios, block placement em parede/teto/chão. Distinguir “suportado pelo jogo” de “apenas preview experimental”.
5. Exportador: manifesto e pasta/ZIP versionada compatível com schema de Lux Items, com validação antes de exportar.

## UX
Desktop-first, grid snap, zoom, pan, live preview, presets de modelos vanilla-like, salvar projeto local, importação de PNG/model JSON. Comparar aspecto na GUI com escala do Minecraft. Permitir especificar rig de acessório Curios em relação à skin.

## Fases
- POC 0: upload PNG + prévia inventário/hotbar com escala.
- POC 1: editor pixel art básico, export de item simples.
- POC 2: display transforms mão/chão/moldura + validação de manifesto.
- POC 3: voxel 3D com preview realista e bloco colocável.
- POC 4: animações, wearables/Curios e distribuição de pacotes.

## Riscos
Browser 3D não reproduz exatamente renderer do Minecraft (shaders, lighting, recurso modded), então oferecer “preview aproximado” e testes in-game antes de aprovação. Não prometer hot-reload de registros de novos itens. Evitar upload de assets para nuvem sem necessidade: local-first por padrão.

## Aceite inicial
Criar textura 16x16, conferir no inventário e na mão, exportar pack, carregar pelo Lux Items, aparência consistente em screenshot in-game. Sem configurar build Java para cada item.

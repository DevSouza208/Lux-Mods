# Lux HUD — Interface dinâmica de RPG

## Layout padrão decidido

- **Canto superior esquerdo:** quadrado com a cabeça da skin do próprio jogador; barras de VIDA, FOME vanilla e XP, com nível visível.
- **Abaixo do painel pessoal:** aliados/grupo em escala menor, cada um com rosto, vida e nível (não listar todos os players do servidor indiscriminadamente).
- **Parte inferior:** somente hotbar e bússola minimalista do Atlas, sem replicar vida/fome/XP vanilla.
- **Rastreador de missão:** retângulo opcional com nome/objetivo/progresso, modo compacto (ícone+barra), ou oculto.
- **Cruz de mira:** configurável e exibida conforme estado.
- **Saturação não é a barra pretendida.** Não adicionar sede/frio/calor na v1.

## Modos configuráveis

Clássico (menos alteração), RPG (painéis visíveis), Cinematográfico (autoocultação), Personalizado (posições/escalas/opacidade). Todas as regras de UI são preferências locais, exceto dados que exigem sincronização servidor.

## Gatilhos de reaparecimento
Troca de item → hotbar temporariamente; ataque/recebimento de dano → HUD completa temporariamente; vida criticamente baixa → vida permanece visível; progresso de quest → rastreador aparece; abrir inventário/menu não deve deixar elementos “presos” ao fechar. Caminhada/exploração tranquila → HUD some progressivamente após delay.

## Segurança visual
Evitar fade rápido/piscadas, respeitar combate contínuo, legibilidade em fundos claros, escalas de GUI e acessibilidade. Modo sempre visível para jogadores que não gostam da autoocultação.

## Integrações
- Sem outros módulos: exibir skin, saúde, fome e XP vanilla.
- Com Progression: mostrar nível RPG como identificação principal e indicar XP vanilla de encantamento separadamente de forma acessível (não misturar números).
- Com Atlas: bússola na hotbar.
- Com Quests: missão selecionada, progresso e atualizações.
- Com Engine/Party: grupo com consentimento e dados sincronizados de vida e nível.

## Multiplayer
Dados de aliados vêm do servidor, com escopo por grupo/proximidade e privacidade. Painéis menores abaixo do principal, layout scroll/compact quando lotado, restrição de latência e permissões.

## Aceite
Movimento tranquilo oculta; interação traz de volta; HUD não esconde alertas críticos; FOME e XP não se confundem; panel de aliados válido em servidor dedicado; overlays de mods ausentes não quebram.

## Implementação experimental — 24/10/2026

Código de `lux-hud` criado na branch de desenvolvimento e adicionado ao pacote de quatro JARs. O primeiro incremento tem modo RPG (skin pessoal; vida/fome/XP; efeitos reais perto da vida; barras vanilla ocultadas quando cobertas), modo CLASSIC (preserva HUD vanilla), quadro de missões à direita com estado honesto vazio e colegas menores abaixo (até quatro do mesmo time vanilla, vida/nível sincronizados pelo servidor). A equipe do placar é uma solução temporária, **não um sistema formal de convites Lux Party**.

**Pendente:** compilação real NeoForge e playtest dessa versão, servidor dedicado, UI responsiva, animações, autoocultação dinâmica e ligação à Lux Quests/Progression. Enquanto não houver dados reais de missão, não apresentar dados fictícios. A PR permanece draft.

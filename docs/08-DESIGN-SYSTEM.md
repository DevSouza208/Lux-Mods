# Guia de UI/UX, estilo e acessibilidade

## Identidade visual
Minecraft autoral com acabamento de RPG, não clone de menus de Skyrim/Witcher/Elden Ring. Diálogo em painel escuro com bordas discretas bronze/dourado, foco no NPC visível em cena e opções na parte inferior; comércio numa coluna à direita. Use tipografia com boa legibilidade, inclusive em GUI scale alta/baixa.

## Diretrizes de HUD
- Minimizar ruído visual; não colocar todos os sistemas na tela ao mesmo tempo.
- Superior esquerdo: foto de cabeça real da skin, HP, FOME, XP e nível.
- Abaixo: amigos/grupo com avatar, saúde e nível em miniaturas.
- Inferior: hotbar + bússola do Atlas.
- Quests: painel de objetivo à esquerda, ícone+barra ou invisível, configurado pelo usuário.
- Estado Cinematográfico: HUD desaparece explorando tranquilo; reaparece ao receber dano, lutar, trocar item, atualizar quest; manter feedback crítico visível.
- Inclua configurações de opacidade, posição, escala, transições, contrastes e desativação de cada overlay. Evitar flicker em eventos repetidos.

## Diálogos
- Foco no rosto/NPC ou corpo renderizado dentro do cenário, sem cutscene que pausa servidor.
- Mouse e teclado navegáveis. Botão de sair/escape sempre funciona, inclusive sob ataque.
- Vocabulário regional/faccional consistente; escrita em português natural, sem excesso de frases idênticas.
- Quando um NPC não sabe algo, permitir “Não faço ideia” em vez de inventar lore infalível.

## Títulos de descoberta
Pouco texto, fade suave, linha dourada discreta, no máximo 2 níveis de hierarquia. Título nunca cobre permanentemente combate, inventário ou chat.

## Conteúdo visual
Lux Food: bebidas, bolinhos, salgados e pratos com pixel art polida; prever sprites de GUI e uso. Studio deve pré-visualizar formas do inventário, render de chão, mão e acessórios; mark diferenças de preview.

## Critérios UX
Testar 720p, 1080p, GUI scale variado, daltonismo/contraste, mods que ocupam HUD, gamepad se suportado por outro mod, chat e multiplayer com latência. Preferir padrões do jogo e caminhos de teclado consistentes.

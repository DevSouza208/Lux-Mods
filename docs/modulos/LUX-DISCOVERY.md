# Lux Discovery — Nome dos lugares

## Fantasia
Ao entrar numa região ou encontrar vila, exibir título cinematográfico discreto como “Planícies” ou “Vila de Pedra Alta”, sem impedir movimento.

## MVP
- Detectar transição estável de bioma, dimensão e entrada em estruturas/vilas suportadas.
- UI com fade in/out, variação Cinematográfica / Clássica / Minimalista e opção desativar.
- Configurar primeira descoberta vs sempre ao reentrar, som opcional, duração e intervalo mínimo.
- Nomes estáveis de localidades: quando Lux Villages instalado, consumir ID e nome persistido; sem ele, usar nome descritivo genérico.
- Escrever descoberta individual para integração com Atlas e Lore; descoberta não deve criar quest automaticamente.

## UX
Exemplos:
- “VOCÊ ENTROU EM — Planícies”
- “LOCAL DESCOBERTO — Vila de Carvalho / Povoado das Planícies”
- “LOCAL DESCOBERTO — Fortaleza do Nether”

Notificação não é um modal, não captura mouse nem interrompe combate. Não repetir título ao cruzar repetidamente fronteira por poucos blocos.

## Regras
Debounce por permanência mínima numa área, limiar para bioma limítrofe e cooldown por local/dimensão. Priorizar notificações de estruturas/vilas sobre biomas; enfileirar ou descartar títulos menos importantes sob combate. Persistir chaves de primeira descoberta por jogador.

## Compatibilidade
Biomas registrados por terceiros são suportados por nome de tradução, quando disponíveis. Estruturas modded exigem resolução/integração específica. Sem afirmar que qualquer estrutura pode ser detectada com exatidão.

## Primeiro protótipo recomendado
Subir client/singleplayer e receber título “Planícies” ao entrar num bioma adequado, com debounce e config de ocultação. Este é um experimento visual pequeno para validar stack e Engine.

## Aceite
Sem spam de título; nomes corretos e português seguro/fallback; saves individuais; ordem estável de prioridade; sem impacto relevante de renderização.

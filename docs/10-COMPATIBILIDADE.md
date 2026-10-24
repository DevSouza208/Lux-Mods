# Estratégia de compatibilidade e modpack

## Matriz obrigatória por release
Nome do mod | versão Minecraft | loader | versão do mod | API disponível | conflito de worldgen/UI/inventory | integração necessária | status teste.

Sem lista final de modpack, NÃO declarar compatibilidade universal.

## Principais pontos
- **Inventário:** mods que consultam PlayerInventory vanilla podem não ver storage Lux >27. Criar adapter quando pack definitivo estiver definido; crafting vanilla e recipe book são foco da v1.
- **Curios:** validar addon/versionamento por loader, usar integração opcional.
- **Villages e IA:** outros mods de estruturas e villagers podem substituir worldgen/POI/entidades; fallback configurável, teste conjunto e ordem de carregamento.
- **Guardas:** avaliar integração com Guard Villagers, mas crimes/ordem pertencem aos serviços do Engine/NPC.
- **Mapa/HUD:** evitar duplicar overlays de Xaero/JourneyMap/outros, keybinds M conflitantes e barras sobrepostas; oferecer opção de desligar.
- **Quest/lore:** JSON/arquivos de conteúdo data-driven com namespace e prioridade; evitar alterar receitas ou drops vanilla sem decisão explícita.
- **Itens:** packs requerem mesma identificação e assets em client/server; invalidar IDs duplicados com erro claro.
- **Materials:** apenas depois da seleção de mods; estudar Almost Unified antes de desenvolver unificador próprio.

## Política de testes
Teste com nenhum outro mod → pares de mods Lux → suite completa Lux → subset externo prioritário → modpack final; dedicated server obrigatório.

## Fallback
Quando falta integração, desligar apenas a função integrada, não o módulo inteiro. Documentar incompatibilidades em issues com logs de versão/sistema e reprodução mínima.

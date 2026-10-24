# Lux Materials — Ideia em standby (não iniciar agora)

## Intenção
Unificar geração e uso de minérios, lingotes, pós e materiais duplicados entre mods do modpack. Administrador aperta tecla configurável (ideia inicial K) e escolhe, por categoria de material, qual provedor oficial prefere: cobre, ouro, prata, estanho etc. Pode escolher minério do Mod A, lingote do Mod B, pó do Mod C. Regras globais com exceções por material.

## Recursos imaginados
Detecção de tags/registries, seleção de ore/block/ingot/dust, prioridades de provedores, ajuste de receitas e output de máquinas, ocultação de duplicatas e preferência de worldgen. Perfis compartilháveis por modpack. Mudanças que exigem reinício informadas. Sem apagar IDs registrados por outros mods.

## Riscos conhecidos
Reescrever geração vanilla/modded e receitas nem sempre funciona genericamente; alguns mods usam sistemas privados de processamento. Chunks já existentes não serão retroativamente substituídos automaticamente. É necessária lista exata de mods, loader, versão e integrações, antes de implementar.

## Estado
**Standby até o modpack definitivo estar selecionado.** Avaliar Almost Unified e soluções equivalentes antes de construir engine de unificação própria.

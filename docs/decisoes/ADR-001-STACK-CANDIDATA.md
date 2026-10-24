# ADR-001 — Stack candidata, não fixada

**Status:** proposta a validar · **Data:** 2026-10-24

## Contexto
Projeto tem IA/geração em servidor e overlays em client, assets/pacotes dinâmicos e apps web independentes. Necessita integração com Curios e eventualmente outros mods. Minecraft Java tem ecossistemas NeoForge/Fabric/Forge.

## Decisão proposta
Começar com Minecraft Java **1.21.1 + NeoForge + JDK 21**, repositório Gradle multi-project e módulos compiláveis separadamente. Lux Studio como app web. Confirmar versões exatas apenas após MDK build e testes de módulos essenciais; reavaliar se lista final do modpack exigir outra versão/loader.

## Alternativas
Fabric: API e tooling diferentes, ecossistema variado. Forge: compatibilidade específica com legado. Desenvolvimento para dois loaders simultaneamente tem custo alto e fica fora do MVP.

## Evidências
Documentação oficial para NeoForge 1.21.1: https://docs.neoforged.net/docs/1.21.1/gettingstarted/ (JDK 21) e https://docs.neoforged.net/docs/1.21.1/items/ (registries).

## Critério de confirmação
runClient e build; servidor dedicado; teste de Curios real; confirmar lista de mods-alvo e APIs necessárias; registrar versões reais no build. Até lá, não chamar “decisão de stack final”.

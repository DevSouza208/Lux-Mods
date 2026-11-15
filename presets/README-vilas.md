# Vilas em terreno suave — preset Structurify

Escopo: **somente as cinco vilas vanilla** que Luki's Grand Capitals 1.1.3 modifica; não altera construções, pool, loot ou o mundo inteiro.

## O que instalar
- **Structurify 2.0.40 para Minecraft 1.21.1 NeoForge**: https://modrinth.com/mod/structurify/version/neoforge-2.0.40%2Bmc1.21.1
- Verificar dependência YACL (YetAnotherConfigLib), se solicitada pelo loader.

## Como ativar
1. **Backup do mundo** antes de alterar a geração de estruturas.
2. Instale Structurify na instância `lux-mods` e abra/feche o Minecraft uma vez para criar a config.
3. Faça backup de `%APPDATA%\.sklauncher\instances\lux-mods\config\structurify.json`, caso exista.
4. Copie `presets/structurify-vilas-terreno-suave.json` para essa pasta e salve como `structurify.json`. **Não sobrescreva uma config com ajustes personalizados sem mesclar os dados.**
5. Reinicie o jogo e teste em **um novo mundo de teste**. Vilas em chunks já gerados NÃO mudam.

## Regras
- **Limite de desnível inicial: 12 blocos** por região ocupada pelas peças de cada vila.
- Alvos: `minecraft:village_plains`, `village_desert`, `village_savanna`, `village_snowy`, `village_taiga`.
- Não restringe a plains nem altera globalmente outros tipos de estruturas.
- Um filtro de **aceitar/rejeitar** não consegue deslocar uma vila para o ponto plano mais próximo. A consequência é que **menos vilas** poderão gerar. Ajustar para 16 ou 20 se a geração ficar rara.
- Só age sobre futuras gerações; não conserta vilas existentes.
- Depende do Structurify carregado. Sem ele, este preset não tem efeito.
- **O atualizador Lux Mods não instala Structurify nem copia configurações de terceiros.** Por segurança, o preset é opt-in e não substitui arquivos locais automaticamente.

Fontes técnicas: https://github-wiki-see.page/m/Faboslav/structurify/wiki/how-structure-checks-work e https://github-wiki-see.page/m/Faboslav/structurify/wiki/configuration-file

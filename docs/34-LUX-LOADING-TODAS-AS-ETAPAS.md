# Lux Loading em todas as etapas — arquitetura e compatibilidade

**Objetivo do usuário:** galeria de fotos aleatórias com crossfade e `CARREGANDO...` nos carregamentos, incluindo a tela vermelha. Não inventar barra ou percentual; não atrasar entrada no jogo; respeitar todas as imagens enquanto houver tempo.

## 1. NeoForge/FML — janela vermelha inicial (boot antecipado)

Essa janela é criada **antes do Minecraft carregar mods usuais e resources do Lux Menu**. Por isso nosso `ScreenEvent.Render.Post` NÃO tem acesso a ela.

**Solução comprovadamente existente:** mod público [`Simple Custom Early Loading`](https://modrinth.com/mod/simple-custom-early-loading) tem releases NeoForge que declaram suporte a Minecraft 1.21.1. Config: instalar o JAR cliente e configurar o provedor em `config/fml.toml` com `earlyWindowProvider = "SimpleCustomEarlyLoading"`. O código fonte é LGPL-2.1-or-later; **não copiar nem redistribuir o JAR sem seguir a licença**. A versão NeoForge 2.1 (arquivo de 19/01/2026) confirma compatibilidade 1.21.1, PNG/JPG/APNG e escolha aleatória entre múltiplos arquivos; **a seleção aleatória está documentada para a imagem inicial, não para um slideshow infinito com crossfade**. Para atender literalmente à troca contínua nesse estágio, estudar extensão própria do provedor ou gerar APNG animado otimizado — o que exige benchmarking de RAM na fase de boot.

Links:
- https://www.curseforge.com/minecraft/mc-mods/simple-custom-early-loading/files/7489293
- https://github.com/lukaskabc/SimpleCustomEarlyLoading
- https://github.com/neoforged/FancyModLoader/issues/415

**Risco:** em NeoForge 1.21.1, `earlyWindowProvider` com id inválido pode não voltar ao padrão (FancyModLoader issue #415). A instalação da dependência deve ser manual/opt-in e ter script de restauração com o Minecraft fechado. Nosso atualizador de JARs **NÃO modifica** `fml.toml` nem instala provedores externos. Não criar config sem validação.

Também existe [`Customize Early Loading Window`](https://www.curseforge.com/minecraft/mc-mods/configurable-early-loading-window-neoforge), divulgado em outubro/2026 para 1.21.1, mas não foi testado; não assumir compatibilidade com nosso slideshow.

## 2. Logo Mojang / `LoadingOverlay` / recarga de recursos

É uma camada de renderização **distinta** das telas normais do Minecraft, frequentemente usada no boot tardio e reload de recursos. Uma implementação própria que substitua ou personalize esse overlay precisa de injeção/mixin específica e testes de compatibilidade com outros mods de UI. Não inserir mixin no pacote atual sem ambiente com build/cliente e rollback claro. `Simple Splash Screen` é referência pública para o conceito, mas não é prova de compatibilidade simultânea com outros provedores.

## 3. Carregar/entrar em mundos

Implementado no Lux Menu via `LuxLoadingSlideshow` + `SlideShuffle` (telas `LevelLoadingScreen`, `ReceivingLevelScreen`, `ProgressScreen` e `GenericMessageScreen` apenas para `menu.savingLevel`, `menu.savingChunks` e `selectWorld.resource_load` quando presentes). Galeria de imagens embaralhada e crossfade automático contínuo enquanto a Screen existir. Foto original nunca altera progresso real. Assim que Minecraft terminar, a tela some, sem tempo mínimo.

## 4. Respawn, teleporte e dimensões

Se o Minecraft usar uma das Screens observadas, o tema aparece. Quando houver transição sem Screen padrão, a implementação não inventa carregamento nem bloqueia gameplay. Revisar eventos específicos posteriormente.

## Caminho de implementação

Fase A — **feita em código / ainda não testada no jogo:** removeu teto de oito, recursou fotos no ResourceManager, embaralhamento não repetitivo e crossfade suave nas telas comuns.
Fase B — **próxima integração opt-in:** protótipo do provedor antecipado FML com dependência conhecida e instruções de recuperação, sem automatizar instalação até passar teste.
Fase C — **futura:** sincronização visual com Mojang/recarga de recursos, que requer módulo/gancho separado e teste de diferentes launchers.

**Status:** 'todas as etapas' não está pronto. A preparação de FML e overlays é documentada; o slideshow do Lux Menu cobre as telas normais de mundo.

## 5. Fotos no salvamento e preparação de recursos (novo, pendente)

Os prints de 24/10 mostraram a caixa preta "Saving world" e "Preparing Resources..." sobre panorama desfocado. A extensão do handler de `ScreenEvent.Render.Post` detecta **apenas** `GenericMessageScreen` com essas chaves de tradução, cobre a tela com a galeria e usa os textos animados "SALVANDO MUNDO..." e "PREPARANDO RECURSOS...". O visual original é coberto no fim do frame, mas a operação verdadeira continua comandada pelo Minecraft. **Não** intercepta, cancela ou força a conclusão do save.

A classificação por tradução evita cobrir mensagens de erro ou confirmação. O código foi publicado sem validação no jogo após essa alteração; não afirmar que toda transição foi resolvida. O boot vermelho FML e o LoadingOverlay Mojang permanecem fora dessa cobertura.

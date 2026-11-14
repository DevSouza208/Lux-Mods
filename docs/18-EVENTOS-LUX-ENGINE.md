# Eventos entre módulos Lux — contrato mínimo 0.1

**Estado:** implementação em código Java puro, teste isolado JDK 21 executado; ainda aguardando compilação NeoForge e runtime dedicado.

## Para que serve
O Lux Engine mantém um barramento local de eventos tipados com ID namespaced. Um módulo publica um fato **depois de persistido pelo servidor**; outros módulos opcionais se inscrevem sem exigir que o jogador instale todo o ecossistema.

Exemplo em código: quando Discovery grava bioma novo no attachment do jogador, emite `lux_discovery:first_biome` com `(UUID jogador, dimensão, bioma)`. Atlas poderá marcar locais; Quests poderá acompanhar objetivos opcionais.

## API

```java
var type = LuxDiscoveryEvents.FIRST_BIOME;
var subscription = LuxEngine.EVENTS.subscribe(type, event -> {
    // event.playerId(), event.dimensionId(), event.biomeId()
});
subscription.close(); // desinscrever quando não for mais necessário
```

- `LuxEventType<T>`: ID e classe de payload; tipo e namespace validados.
- `LuxEventBus#subscribe`: devolve Subscription AutoCloseable.
- `LuxEventBus#publish`: devolve a quantidade de listeners executados **com sucesso**.
- ID igual com contrato diferente é erro de programação, nunca converte silenciosamente.
- Sem inscritos? Publicar retorna zero.
- Falha em callback? O Engine **registra o erro e continua chamando os demais**; um addon defeituoso não impede a notificação visual nem derruba a transação após salvar. Não captura `Error` de JVM.

## Limites
Este barramento é **síncrono e local**. Não transmite pela rede: o payload NeoForge é separado. A assinatura de eventos tem que ocorrer no lado lógico certo (tipicamente servidor). Callbacks devem ser curtos, não iniciar tick loops e não criar requisições externas. Não usar isso como sistema de transação, permissão ou rollback; o estado do servidor é a autoridade. A lista de contratos públicos evoluirá com versões.

## Verificação
Testado em Java 21 isolado: inscrição e cancelamento, múltiplos assinantes, conflito de tipo, publicação sem assinantes e observador que lança exceção seguido de outro observador. Testes JUnit equivalentes estão no código para rodar quando Gradle funcionar.

**Falta:** compilar e rodar Minecraft 1.21.1/NeoForge com ambos os mods, testar cliente e servidor dedicado. Veja docs/17-VALIDACAO-BUILD.md.

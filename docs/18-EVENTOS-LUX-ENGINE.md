# Eventos entre módulos Lux — contrato mínimo 0.1

**Estado:** implementado em código Java puro nesta branch, ainda aguardando compilação NeoForge e runtime dedicado. Não há garantia de compatibilidade estável até o primeiro release.

## Para que serve
O Lux Engine mantém um barramento local de eventos **tipados e com ID namespaced**. Um módulo publica um fato após confirmação server-side; outros módulos opcionais se inscrevem sem forçar o jogador a instalar todos.

Exemplo real de integração escrito: quando Discovery grava um bioma novo no attachment do jogador, publica `lux_discovery:first_biome` com `(UUID jogador, dimensão, bioma)`. Atlas poderá usar a mesma informação para futuras marcações; Quests pode usá-la como condição.

## APIs

```java
var key = LuxDiscoveryEvents.FIRST_BIOME;
var subscription = LuxEngine.EVENTS.subscribe(key, event -> {
    // evento contém playerId(), dimensionId(), biomeId()
});

// Fechar no unload / quando serviço não for mais necessário:
subscription.close();
```

- `LuxEventType<T>`: ID de evento + classe de payload; rejeita IDs incorretos.
- `LuxEventBus#subscribe`: devolve `Subscription` AutoCloseable.
- `LuxEventBus#publish`: retorna quantidade de listeners executados.
- Dois contratos Java diferentes com o mesmo ID são tratados como erro.
- Ausência de listeners não é erro. Listeners podem ser removidos e registrados dinamicamente.

## Regras importantes

- É um barramento **local e síncrono** no processo atual. NÃO sincroniza servidor/cliente automaticamente. Eventos de gameplay devem ser publicados no thread correto do servidor.
- O servidor continua sendo a fonte de verdade; um evento não concede direitos/autorizações.
- O evento é emitido **depois** de atualizar o attachment persistente e **antes** da mensagem visual enviada ao cliente.
- Exceção em listener interrompe a publicação e é propagada: consumidores precisam ser seguros; a política de tolerância/falha poderá ser revista antes do release.
- Mod IDs e interfaces públicas têm que ser versionados em futuras releases. Não importar classes de UI/client no barramento.
- Eventos não são mecanismo de transação nem substituem eventos de lifecycle do NeoForge.

## Qualidade

Foi compilado e executado um teste de lógica Java 21 isolado contendo `LuxEventType`, `LuxEventBus` e payload de `LuxDiscoveryEvents`. Testou evento sem inscrito, dois inscritos, remoção de um, contrato incompatível. O script smoke do repositório agora cobre o barramento também.

**Falta:** validar build real NeoForge, anexar logs client/dedicated e comparar as APIs modded compiladas. Docs: `docs/17-VALIDACAO-BUILD.md`.

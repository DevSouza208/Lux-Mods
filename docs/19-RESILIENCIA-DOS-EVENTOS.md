# Resiliência do barramento de eventos Lux

**Estado:** proteção implementada e testada em Java 21 puro. Ainda requer build NeoForge e teste no Minecraft.

## Por que existe

Módulos independentes podem reagir a eventos de outros módulos. Se um addon publica novamente o mesmo evento ao processá-lo, há risco de um ciclo A → B → A e de `StackOverflowError`. Para limitar cascatas inesperadas, cada instância `LuxEventBus` aplica **profundidade máxima de 16 publicações síncronas aninhadas, por thread**.

## Regras

1. O primeiro `publish` começa com profundidade 1.
2. Em cada `publish` recursivo o contador aumenta até 16.
3. Na tentativa seguinte, o barramento lança `IllegalStateException`.
4. Se isso ocorrer dentro de um listener, a exceção é registrada e **o barramento tenta notificar os demais listeners**.
5. `finally` restaura o contador, inclusive depois de exceções; eventos futuros continuam funcionando.
6. Um listener só é considerado bem-sucedido quando retorna normalmente. O número retornado por `publish` é a contagem de listeners bem-sucedidos na chamada.

## Limitações

- Isso **não** evita um addon que dispare novos eventos em ciclos futuros; monitoramento por tick fica para uma fase posterior.
- `Error` de JVM não é capturado; a proteção cobre `RuntimeException` como problemas comuns de addon.
- Observadores ainda rodam síncronos na mesma thread; não fazer IO nem cálculos pesados.
- Este componente **não** sincroniza dados automaticamente entre cliente e servidor.

## Evidências

Compilação com `javac --release 21` e execução local de `LuxEventDepthSmoke`: passou, com 16 notificações de observador não recursivo e retorno normal. Testes independentes de serviços/Discovery e falha de observer também passaram.

Scripts no repositório: `scripts/smoke-pure-java.bat` (Windows), `scripts/smoke-pure-java.sh` (Linux). Teste JUnit correspondente em `modules/lux-engine/src/test/java/.../LuxEventBusDepthTest.java`.

A compilação Gradle/NeoForge e execução em servidor dedicado **permanecem pendentes**.

# ADR-005 — Execução conjunta do Engine e Discovery no desenvolvimento

**Status:** implementado em configuração; aguarda execução do Gradle e smoke Minecraft.

## O problema

Lux Discovery declara `lux_engine` como dependência obrigatória em `META-INF/neoforge.mods.toml`. A compilação `implementation project(':lux-engine')` coloca a API do Engine no classpath do Discovery, mas o run do ModDevGradle usa uma lista de módulos declarados em `neoForge.mods`. Antes desta decisão, o projeto Discovery listava apenas o próprio código em `mods`; executar `:lux-discovery:runClient` poderia iniciar sem registrar corretamente o mod `lux_engine`, apesar de ambos os códigos estarem no classpath.

## Solução

Manter os dois subprojetos e os dois JARs **independentes**. Para o run de desenvolvimento **do Discovery**, registrar ambos os source sets:

```groovy
project(':lux-discovery') {
    dependencies {
        implementation project(':lux-engine')
    }
    neoForge {
        mods {
            lux_engine {
                sourceSet project(':lux-engine').sourceSets.main
            }
        }
    }
}
```

Com o próprio `lux_discovery` já registrado no `subprojects`, o ModDevGradle passa a ter dois modelos de mod no run do Discovery. Esta API de associar um `sourceSet` de subprojeto é demonstrada pelo projeto de testes oficial ModDevGradle, em `neoForge { mods { ... } }`.

## Validar

```powershell
.\gradlew.bat :lux-discovery:runClient
```

Após carregar, o log do Lux Engine deve mostrar **lux_engine e lux_discovery**. Confirmar menu de mods e título de bioma.

```powershell
.\gradlew.bat :lux-discovery:runServer
```

O servidor dedicado também deve carregar ambos sem exceção de classes client-only. Verificar se execução do Engine isolado ainda funciona (`:lux-engine:runServer`).

## Limitações

Esta é uma mudança fundamentada no modelo ModDevGradle, **não validada em execução**: GitHub Actions registra `startup_failure` com 0 jobs e o ambiente local de análise não alcança repositórios Gradle/Maven. Se a API em `2.0.148` apresentar incompatibilidade, ajustar com logs do primeiro build, sem mesclar o código antes.

Referência: https://github.com/neoforged/ModDevGradle — seção `ModDevGradle/testproject/build.gradle`, registro de outro `sourceSet` no bloco `mods`.

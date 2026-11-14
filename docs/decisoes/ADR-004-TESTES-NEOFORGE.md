# ADR-004 — Gradle e testes de módulos no ambiente NeoForge

**Status:** configurado, aguardando execução real.

## Problema encontrado na revisão

O repositório já tinha testes JUnit, mas o plugin ModDevGradle exige ativar explicitamente o suporte de testes (`neoForge.unitTest.enable()`) e selecionar o mod testado (`testedMod`). Apenas declarar `testImplementation`, `testRuntimeOnly` e `useJUnitPlatform()` não configura o ambiente NeoForge para unit tests.

## Correção proposta

Cada subprojeto registra o próprio `testedMod`:
- `:lux-engine` testa `lux_engine`;
- `:lux-discovery` testa `lux_discovery`.

Comandos de validação:

```powershell
.\gradlew.bat :lux-engine:test :lux-discovery:test --stacktrace
.\gradlew.bat :lux-engine:build :lux-discovery:build --stacktrace
```

Os módulos geram JARs separados, sem incluir todas as funcionalidades no Lux Engine. O Discovery declara o Engine como dependência obrigatória no `neoforge.mods.toml`. A compatibilidade do classpath durante `runClient` e `runServer` ainda precisa ser confirmada na compilação real.

## Critérios de aceite

- Gradle executa e informa o resultado dos JUnit, em vez de apenas finalizar task sem descoberta de testes.
- Arquivos de relatório `modules/*/build/test-results/test/` são produzidos.
- Configuração não tenta carregar classes client-only em testes e servidor dedicado.
- Build limpo gera dois JARs inspecionáveis por `python scripts/verify_jars.py`.

## Limitações

Esta alteração foi feita com base no exemplo oficial do ModDevGradle, mas **não foi possível executar Gradle/NeoForge neste ambiente** por ausência de acesso aos repositórios externos. Não afirmar build verde até rodar num PC/runner apto.

Fonte: https://docs.neoforged.net/toolchain/docs/plugins/mdg/#unit-testing-with-junit

# Primeiro teste de jogo: pacote isolado

Este guia transforma o resultado do Gradle em dois arquivos .jar colocados juntos, sem tocar nos arquivos pessoais do Minecraft.

## No Windows

1. Clone o projeto e entre na branch `feat/engine-discovery-bootstrap`.
2. Garanta que `java -version` e `javac -version` apontam para o JDK 21.
3. Execute:

```powershell
.\gradlew.bat assembleTestPack --stacktrace
```

A tarefa **compila/testa cada módulo** (`:lux-engine:build`, `:lux-discovery:build`) e, se tudo passar, copia os JARs gerados para:

`build/lux-testpack/mods/`

É **uma cópia para teste**, não uma instalação. Abra uma instância isolada do launcher Minecraft 1.21.1 + NeoForge compatível com versão de projeto e copie os dois JARs para a pasta mods dessa instância. Não reutilize o seu save principal.

Para confirmar os metadados dos arquivos compilados, com Python 3.11 ou superior:

```powershell
python scripts/verify_jars.py
```

## Cenário de teste

- Abra o menu de Mods: conferir Lux Engine e Lux Discovery.
- Entre num novo mundo de testes e permaneça aproximadamente 2 segundos nas Planícies. Deve aparecer uma notificação de local descoberto.
- Explore uma floresta e volte às Planícies: a descoberta anterior não deve ser reanunciada.
- Feche, reabra o jogo, visite o mesmo bioma e verifique se não é anunciado novamente.
- Troque de dimensão e teste morte/renascimento.
- Se possível, inicie um servidor dedicado NeoForge 1.21.1 com os dois JARs e dois jogadores de teste.

## Resultado esperado

O teste NÃO está marcado como concluído: o build Gradle/NeoForge ainda não executou neste ambiente por falta de dependências externas, e o GitHub Actions segue com `startup_failure` antes de iniciar jobs. O primeiro run local precisa produzir logs para confirmar que `assembleTestPack` e os JARs realmente funcionam.

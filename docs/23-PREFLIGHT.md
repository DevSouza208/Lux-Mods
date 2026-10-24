# Pré-verificação offline do Lux Mods

Para verificar a estrutura do repositório mesmo sem Minecraft, Maven ou Gradle:

    python3 scripts/preflight.py

Esta auditoria cobre presença de módulos, identificadores Java, metadados TOML, dependências entre mods, wrapper, ordem de avaliação do Gradle, teste com os dois mods carregados, recursos de idioma e imports client-only fora dos diretórios de cliente.

Para executar os testes do verificador:

    python3 -m unittest discover -s scripts -p 'test_preflight.py' -v

O teste entrou no job pure-java da CI, antes do smoke de Java. Não substitui nem certifica build, jogo ou servidor dedicado.

O Gradle agora declara que Discovery depende da avaliação do projeto Engine antes de acessar `sourceSets` do Engine. Isto acompanha o exemplo oficial em https://github.com/neoforged/ModDevGradle/blob/main/testproject/build.gradle.

Enquanto GitHub Actions retornar `startup_failure` sem jobs, validar localmente com `gradlew.bat assembleTestPack` e consultar a PR #14.

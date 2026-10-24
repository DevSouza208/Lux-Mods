# Verificações locais do Lux Mods

Windows: abrir PowerShell na raiz do projeto e executar:

    powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-environment.ps1

O script verifica Java/Javac, wrapper, faz o teste de lógica sem internet, tenta compilar os dois módulos por Gradle e imprime os arquivos JAR.

Etapas individuais no Windows:

    scripts\smoke-pure-java.bat
    .\gradlew.bat :lux-engine:build :lux-discovery:build --no-daemon --stacktrace

Linux/macOS:

    bash scripts/smoke-pure-java.sh
    ./gradlew :lux-engine:build :lux-discovery:build --no-daemon --stacktrace

Não instala os mods na pasta do usuário e não altera mundos. Para ver Discovery em ação, depois do build use uma instância isolada Minecraft Java 1.21.1 NeoForge com os dois JARs em mods/.

GitHub Actions apresenta startup_failure com zero jobs neste repositório; isso não equivale a um build Gradle reprovado. Ver issue #15.

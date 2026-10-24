# Lux Mods — compilação e instalação

## Instalação fácil no SKlauncher

No Windows, com JDK 21 e a instância `teste` fechada, use o atalho **Atualizar Lux Mods.bat**.
Ele faz `git pull` e chama `scripts/install-to-sklauncher.ps1`, que roda `assembleTestPack` e
copia **Lux Engine, Lux Discovery, Lux Atlas e Lux HUD** para `%APPDATA%\.sklauncher\instances\teste\mods`
somente se o build e a verificação dos JARs passarem. Faz backup externo em
`%APPDATA%\.sklauncher\instances\teste\lux-mods-backup`.

Manual no PowerShell:

```powershell
cd "$env:USERPROFILE\LuxModsDev"
git pull --ff-only
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\install-to-sklauncher.ps1
```

## Testes individuais

Windows:

```powershell
scripts\smoke-pure-java.bat
.\gradlew.bat assembleTestPack --no-daemon
python scripts/verify_jars.py
```

Linux/macOS:

```bash
bash scripts/smoke-pure-java.sh
./gradlew assembleTestPack --no-daemon
python3 scripts/verify_jars.py
```

Smoke Java testa lógica pura, mas **não comprova** renderização de GUI NeoForge. O Lux HUD e a bússola do Atlas
precisam ser testados dentro do cliente e o carregamento em servidor dedicado permanece pendente.

O GitHub Actions apresentou `startup_failure` com zero jobs; ver issue #15.


## Lux HUD — teste funcional

O modo RPG exibe personagem à esquerda (skin/vida/fome/XP), colegas menores abaixo apenas se estiverem no mesmo time de scoreboard e missões no canto direito. Sem Lux Quests, o rastreador mostra um estado vazio — nunca inventa missões. Ícones de efeitos ativos ficam perto da vida. O HUD tem um modo `CLASSIC` para voltar ao vanilla e toggles de barras/efeitos/colegas/missões na configuração CLIENT.

Para experimentar colegas em multiplayer, use os comandos vanilla ` /team add luxparty`, `/team join luxparty Jogador1` e `/team join luxparty Jogador2` (com OP/permissões). Até existir Lux Party, o scoreboard team é a origem dos membros e o servidor sincroniza a vida de até quatro deles.

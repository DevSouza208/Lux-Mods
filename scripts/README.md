# Lux Mods — compilação e instalação

## Atualização local no SKlauncher

Com Minecraft fechado, execute a partir da cópia local do repositório:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\install-to-sklauncher.ps1
```

O script usa por padrão `%APPDATA%\.sklauncher\instances\lux-mods\mods` (ou `-Instance nome`), atualiza a `main` via `git pull --ff-only origin main`, exige JDK 21, compila localmente `assembleTestPack` e instala os nove JARs: engine, discovery, compass, quests, upgrades, hud, menu, borderless e dialogues. Remove versões antigas desses módulos e `lux-atlas-*.jar`, faz backup e verifica SHA256 de cada cópia. Se houver erro antes da cópia, não instala; se houver erro durante a cópia, tenta restaurar o backup.

**Atalho Windows:** o arquivo `Atualizar Lux Mods.bat` não está versionado neste repositório. Portanto, não é possível garantir que um atalho já existente no computador invoque este instalador. Confira o campo *Destino* do atalho e se ele aponta para o script acima, no checkout correto. O script `TESTAR-PACOTE-LUX.bat` apenas reúne evidências; não instala mods. `build-testpack-windows.ps1` apenas gera ZIP e hashes; não copia para a instância.

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

## Lux Menu — quinto mod experimental

O menu inicial e ESC (PauseScreen) mantêm todas as ações vanilla e recebem botões com tema Clean Fantasy e assinatura discreta. Config cliente em `config/lux_menu-client.toml`: `enabled`, `styleButtons`, `showBranding`. Isso **não** modifica o splash/loading NeoForge vermelho. Para desligar somente o efeito visual sem remover o mod, defina `enabled=false`; para isolar problema de inicialização, retire somente `lux-menu-0.1.0-SNAPSHOT.jar` da instância. Para reativar o conjunto todo, use o atalho existente `Atualizar Lux Mods.bat`.


## Lux Boot — próximo incremento experimental, não instalado

Foi preparado `scripts/lux-boot-theme.ps1` para a **instância teste somente**, com `-Action Preview` como padrão sem alteração e os comandos explícitos `-Action Apply` e `-Action Restore`. Ele **não desenha a UI Lux de loading**: ao aplicar, define `darkMojangStudiosBackground:true` em `options.txt`, que o **FancyModLoader 1.21.1 também lê** para escolher fundo preto em sua janela antecipada. **Não** altera `earlyWindowControl`: as barras reais de progresso do FML permanecem. O sistema avançado de themes JSON da branch main do FancyModLoader não existe da mesma forma na branch 1.21.1. Consulte `docs/32-LUX-BOOT-CARREGAMENTO.md` e não execute por padrão no instalador de mods.


## Prévia de Lux Boot (apenas navegador)

Abra `experimental/lux-boot-preview/index.html`. A tela mostra arte gráfica Clean Fantasy e progresso **simulado**, não se conecta ao loader real nem muda seus mods. Esse protótipo é independente do script opcional `lux-boot-theme.ps1` e não será empacotado em nenhum JAR. Tudo será testado na sessão integrada futura; detalhes em docs/32-LUX-BOOT-CARREGAMENTO.md.

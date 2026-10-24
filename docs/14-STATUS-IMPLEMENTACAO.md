# Implementação — estado verificável

Atualização: 2026-10-24, PR #14, branch `feat/engine-discovery-bootstrap`.

## Código publicado
- Workspace Gradle multi-project para NeoForge 1.21.1 / JDK 21, com dois módulos independentes (`lux_engine` e `lux_discovery`).
- Lux Engine: catálogo de módulos detectados, config, registro tipado de serviços e **barramento tipado de eventos** com unsubscribe e isolamento de erros de observers.
- Lux Discovery: título de bioma vanilla client-side, debounce/cooldown e salvamento de histórico por jogador via NeoForge attachment (Codec + copyOnDeath).
- No momento de salvar uma descoberta nova, Discovery envia um evento `lux_discovery:first_biome` no servidor; outros módulos podem assinar sem depender da implementação interna.
- Payload NeoForge servidor→cliente envia somente o bioma ao jogador que descobriu.
- Scripts `scripts/smoke-pure-java.sh` e `.bat` e PowerShell de diagnóstico; JUnit em ambos projetos; CI com jobs separados.

## Evidências positivas
- Em 2026-10-24 foi executada **compilação javac --release 21 e execução Java 21 de classes de lógica isoladas** (serviços, debouncer, ledger, event bus e event type/payload), com saída `LUX SMOKE PASSOU`.
- Teste separado `EventSmoke`: registro, 2 listeners, unsubscribe, contrato incompatível — `LUX EVENTS PASSOU`.
- Teste separado `FailureSmoke`: listener lança RuntimeException de propósito, evento registra erro, próximo listener continua — `LUX FAILURE ISOLATION PASSOU`.

## Ainda NÃO validado
- **Nenhum build completo Gradle/NeoForge ainda foi executado com êxito.** Este ambiente não possui acesso de rede ao Maven/NeoForge; o GitHub Actions continua sem iniciar jobs (`startup_failure`, issue #15).
- Recursos NeoForge (config, event listeners, attachments, networking, mod metadata) ainda precisam de compilação e execução no jogo real.
- Falta confirmar loading de dois JARs na mesma instância, dedicated server, relog e morte, tradução/render em Minecraft real.
- API pública de eventos e serviços é pré-release, sujeita a ajustes.

## Validar no Windows
Na raiz do repositório após selecionar branch:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-environment.ps1
```

Se Gradle compilar, validar JARs em instância NeoForge isolada, nunca em save pessoal sem backup. Não mesclar a PR ou anunciar release antes de testes de client/dedicated.

## Referências
- `docs/17-VALIDACAO-BUILD.md`
- `docs/16-DISCOVERY-PERSISTENTE.md`
- `docs/18-EVENTOS-LUX-ENGINE.md`
- Issues #1, #2, #3, #15, #16 e PR #14.

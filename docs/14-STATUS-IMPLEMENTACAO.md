# Implementação — estado verificável

Atualização: 2026-10-24, PR #14, branch feat/engine-discovery-bootstrap.

## Código presente
- Gradle multi-project NeoForge 1.21.1 com JDK 21 (configuração importada/adaptada do MDK oficial).
- Dois módulos JAR: Lux Engine e Lux Discovery.
- Engine: log de módulos conhecidos instalados, config e API mínima de serviço tipado e opcional.
- Discovery: título vanilla no primeiro contato com um bioma por sessão e dimensão, debounce, cooldown, fallback de nome, pt-BR.
- Client agora monitora a sessão por conexão, sem resetar descobertas ao cruzar dimensões.
- Smoke test Java puro (sem JUnit/Gradle): scripts/smoke-pure-java.bat e scripts/smoke-pure-java.sh.
- Testes JUnit para catálogo, registro de serviços, debouncer.

## O que foi ou não foi testado
- Smoke Java puro pode ser executado com JDK 21 sem dependências remotas. O script valida os componentes de lógica sem APIs Minecraft.
- **Build NeoForge completo e execução em Minecraft ainda pendentes.** Não chamar release jogável antes da compilação e validação real.
- GitHub Actions registrou startup_failure sem jobs; status documentado na issue #15.
- Persistência de descoberta APÓS desconectar continua **não implementada**; o controle é por sessão.
- API de serviços é ponto de extensão inicial, não barramento completo de eventos.

## Testar agora
No Windows, raiz do repositório:
~~~powershell
scripts\smoke-pure-java.bat
gradlew.bat build --stacktrace
~~~
Se o build passar: colocar os dois JARs na pasta mods de uma instância NeoForge 1.21.1 ou usar runClient do subprojeto e confirmar presença dos dois módulos. Iniciar servidor dedicado também.

## Checklist antes de merge
- [ ] Compilar Gradle completo, depuração de event listener/translations se necessário.
- [ ] Testar módulos independentemente e em conjunto.
- [ ] Testar cliente + dedicated server.
- [ ] Fechar GH Actions #15 ou anexar build local reprodutível.
- [ ] Atualizar GDD/roadmap com requisitos efetivamente validados.

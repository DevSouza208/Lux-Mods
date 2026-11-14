# CI e validação do diário LXJ1 — 14/11/2026

O commit `ce0ac24b971dc860aa19a98a725c121055a8bf46` disparou o workflow `Lux Mods CI`, execução **43557574886**, que terminou em **failure**.

Os dois jobs retornaram falha:
- `Compilar JARs NeoForge` (job 132333780102)
- `Testes de lógica sem Minecraft` (job 132333780158)

**Não foi possível determinar a causa**: o serviço de logs retornou `BlobNotFound` (404) para ambos e a consulta de etapas retornou listas vazias. Portanto não há evidência para atribuir a falha ao código ou afirmar que o Gradle sequer iniciou.

Próximas verificações ao ter logs acessíveis:
1. Conferir disponibilidade e quota do runner GitHub Actions.
2. Conferir Java 21 e `scripts/smoke-pure-java.sh` incluindo `LuxAtlasJournalSmoke`.
3. Compilar os módulos NeoForge 1.21.1 e corrigir os erros reais identificados.
4. Testar recuperação de arquivo LXJ1 após interrupção abrupta com backups, incluindo resistência a corrupção e ordem de gravação.

**Situação: código integrado na branch, CI falhou e nenhum JAR está aprovado para instalação.**

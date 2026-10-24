# Primeiro build real do Minecraft — retorno em 24/10/2026

O teste Windows executado na instância `teste` do SKlauncher confirmou JDK 21.0.12.1, Gradle 9.2.1, download e transformação do Minecraft 1.21.1 + NeoForge 21.1.264.

A compilação terminou em `:lux-discovery:compileJava` com **três mensagens correspondentes a duas causas**:

1. `net.neoforged.fml.DistExecutor` não existe nessa classpath. A rede agora usa o lado físico `FMLEnvironment.dist` e encaminha o handler visual através de uma classe aninhada, que só deve ser resolvida no cliente.
2. `AttachmentType.builder(Set::<String>of)` é ambíguo entre `Supplier` e `Function`. A construção passou a usar uma lambda sem parâmetros `() -> Set.<String>of()`, selecionando `Supplier`.

**Não é resultado de novo build**: estes fixes precisam ser compilados de novo na máquina Windows, e o servidor dedicado ainda deve ser testado.

Para o usuário do SKlauncher, com o checkout `%USERPROFILE%\LuxModsDev` e Java 21:

```powershell
cd "$env:USERPROFILE\LuxModsDev"
git pull --ff-only
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\install-to-sklauncher.ps1
```

O instalador só copia JARs após `assembleTestPack`, contagem de arquivos, verificação de ZIP+metadados/classes; preserva backups fora de `mods`. Em caso de erro de compilação, não instala.

Aguardar os erros seguintes de compilação/execução, caso ocorram, antes de sair do estado draft da PR #14.

Referências: https://docs.neoforged.net/docs/1.21.1/networking/payload/ e https://docs.neoforged.net/docs/1.21.1/concepts/sides/

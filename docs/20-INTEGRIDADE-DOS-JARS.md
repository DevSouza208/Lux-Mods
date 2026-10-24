# Integridade dos JARs — Lux Engine e Discovery

O build só é útil se gerar dois arquivos reconhecidos pelo NeoForge. Além de compilar, agora existe um verificador automático independente de Minecraft.

Com Python 3.11+ (sem bibliotecas extras), depois do Gradle:

    python scripts/verify_jars.py

O verificador inspeciona o ZIP/JAR real: META-INF/neoforge.mods.toml, modLoader javafml, IDs lux_engine e lux_discovery, versão expandida, classes principais .class e dependências declaradas. Discovery deve declarar lux_engine como obrigatória. O script imprime SHA256 de cada JAR.

Testes do próprio verificador, sem Minecraft:

    python -m unittest discover -s scripts -p "test_verify_jars.py" -v

Os testes criam JARs fictícios em diretórios temporários e verificam casos de sucesso e falha. O sucesso dos testes fictícios NÃO valida os JARs do Lux Mods.

Na CI, o validador só roda depois do build Gradle. Enquanto GitHub Actions apresentar startup_failure com zero jobs, essa etapa também não executa. Isso não é evidência de erro nem de sucesso de compilação.

Para liberar a PR #14 ainda é necessário: Gradle gerar os dois JARs reais; rodar verificador; iniciar Minecraft 1.21.1 com os dois mods; e validar servidor dedicado, logout, morte e persistência.

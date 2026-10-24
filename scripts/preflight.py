#!/usr/bin/env python3
"""Static preflight: no Gradle, no Minecraft. Must not be mistaken for a build."""
import json
import re
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MODULES = {
    "lux-engine": ("lux_engine", "LuxEngine", "dev/luxmods/engine/LuxEngine.java"),
    "lux-discovery": ("lux_discovery", "LuxDiscovery", "dev/luxmods/discovery/LuxDiscovery.java"),
    "lux-atlas": ("lux_atlas", "LuxAtlas", "dev/luxmods/atlas/LuxAtlas.java"),
}


def audit(root=ROOT):
    errors = []

    def read(rel):
        path = root / rel
        if not path.is_file():
            errors.append(f"Arquivo ausente: {rel}")
            return ""
        return path.read_text(encoding="utf-8")

    settings = read("settings.gradle")
    build = read("build.gradle")
    props = read("gradle.properties")
    for project, (modid, klass, filename) in MODULES.items():
        if f"project(':{project}').projectDir" not in settings:
            errors.append(f"Subprojeto não encontrado: {project}")
        source = read(f"modules/{project}/src/main/java/{filename}")
        if f"MOD_ID = " not in source or f'"{modid}"' not in source:
            errors.append(f"Mod ID inválido para {project}")
        if f"@Mod({klass}.MOD_ID)" not in source:
            errors.append(f"Classe principal do mod divergente: {project}")
        metadata = read(f"modules/{project}/src/main/templates/META-INF/neoforge.mods.toml")
        if 'modLoader="javafml"' not in metadata or "[[mods]]" not in metadata:
            errors.append(f"Manifesto de mod incompleto: {project}")
        if f"[[dependencies.{modid}]]" not in metadata:
            errors.append(f"Dependências de mod ausentes: {project}")
        if project in ("lux-discovery", "lux-atlas") and 'modId="lux_engine"' not in metadata:
            errors.append(f"{project} não declara Engine como dependência")

    if "evaluationDependsOn(':lux-engine')" not in build:
        errors.append("Falta ordem de avaliação entre projetos")
    if "project(':lux-engine').sourceSets.main" not in build:
        errors.append("Engine não registrado no run do Discovery")
    if "loadedMods = [mods.lux_atlas, mods.lux_engine]" not in build:
        errors.append("Testes Atlas não carregam Engine")
    if "loadedMods = [mods.lux_discovery, mods.lux_engine]" not in build:
        errors.append("Unit tests não carregam os dois mods")
    if "testedMod = mods." not in build:
        errors.append("Unit tests sem testedMod")
    if "tasks.register('assembleTestPack'" not in build:
        errors.append("Falta tarefa assembleTestPack")
    if "minecraft_version=1.21.1" not in props:
        errors.append("Versão Minecraft não é 1.21.1")
    if "neo_version=" not in props:
        errors.append("NeoForge não configurado")

    wrapper = root / "gradle/wrapper/gradle-wrapper.jar"
    if not wrapper.is_file() or not zipfile.is_zipfile(wrapper):
        errors.append("Wrapper jar ausente/inválido")
    if "distributionUrl=" not in read("gradle/wrapper/gradle-wrapper.properties"):
        errors.append("Falta distributionUrl")
    for project in MODULES:
        java_root = root / "modules" / project / "src/main/java"
        for path in java_root.rglob("*.java"):
            if "/client/" in "/" + path.relative_to(java_root).as_posix():
                continue
            if re.search(r"^import\s+net\.minecraft\.client\.", path.read_text(encoding="utf-8"), re.M):
                errors.append(f"Import client-only em classe comum: {path}")
    for language in ("pt_br", "en_us"):
        path = root / "modules/lux-discovery/src/main/resources/assets/lux_discovery/lang" / (language + ".json")
        try:
            values = json.loads(path.read_text(encoding="utf-8"))
            if "message.lux_discovery.biome" not in values:
                errors.append(f"Tradução ausente: {language}")
        except (FileNotFoundError, ValueError, UnicodeDecodeError) as exc:
            errors.append(f"Tradução inválida: {language}: {exc}")
    return errors


if __name__ == "__main__":
    problems = audit()
    for problem in problems:
        print("ERRO:", problem)
    if problems:
        print("LUX PREFLIGHT FALHOU")
        sys.exit(1)
    print("LUX PREFLIGHT PASSOU — estrutura apenas; Gradle/Minecraft NÃO testados.")

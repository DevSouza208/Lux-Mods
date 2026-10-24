#!/usr/bin/env python3
"""Inspect Lux Engine and Discovery JARs after Gradle build (Python 3.11+)."""
import hashlib
import sys
import tomllib
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MODULES = {
    "lux-engine": ("lux_engine", "dev/luxmods/engine/LuxEngine.class", set()),
    "lux-discovery": ("lux_discovery", "dev/luxmods/discovery/LuxDiscovery.class", {"lux_engine"}),
    "lux-atlas": ("lux_atlas", "dev/luxmods/atlas/LuxAtlas.class", {"lux_engine"}),
    "lux-hud": ("lux_hud", "dev/luxmods/hud/LuxHud.class", {"lux_engine"}),
}


class InvalidArtifact(Exception):
    pass


def validate_jar(path, expected_id, expected_class, extra_deps=()):
    path = Path(path)
    if not path.is_file():
        raise InvalidArtifact(f"Arquivo ausente: {path}")
    try:
        with zipfile.ZipFile(path) as jar:
            corrupt = jar.testzip()
            if corrupt is not None:
                raise InvalidArtifact(f"{path}: entrada ZIP danificada: {corrupt}")
            entries = set(jar.namelist())
            name = "META-INF/neoforge.mods.toml"
            if name not in entries:
                raise InvalidArtifact(f"{path}: {name} ausente")
            if expected_class not in entries:
                raise InvalidArtifact(f"{path}: classe obrigatória ausente: {expected_class}")
            manifest = tomllib.loads(jar.read(name).decode("utf-8"))
    except (OSError, zipfile.BadZipFile, UnicodeDecodeError, tomllib.TOMLDecodeError) as exc:
        raise InvalidArtifact(f"{path}: arquivo JAR ou TOML inválido: {exc}") from exc

    if manifest.get("modLoader") != "javafml":
        raise InvalidArtifact(f"{path}: loader diferente de javafml")
    mods = manifest.get("mods")
    if not isinstance(mods, list) or len(mods) != 1:
        raise InvalidArtifact(f"{path}: esperado um bloco [[mods]]")
    if mods[0].get("modId") != expected_id:
        raise InvalidArtifact(f"{path}: modId incorreto: {mods[0].get('modId')}")
    version = mods[0].get("version")
    if not isinstance(version, str) or not version or "$" in version:
        raise InvalidArtifact(f"{path}: versão não expandida ou vazia")
    declared = manifest.get("dependencies", {}).get(expected_id)
    if not isinstance(declared, list):
        raise InvalidArtifact(f"{path}: lista de dependências ausente")
    required = {d.get("modId") for d in declared if str(d.get("type")).lower() == "required"}
    missing = ({"minecraft", "neoforge"} | set(extra_deps)) - required
    if missing:
        raise InvalidArtifact(f"{path}: dependências faltando: {sorted(missing)}")
    if any("$" in str(manifest.get(key, "")) for key in ("loaderVersion", "license")):
        raise InvalidArtifact(f"{path}: placeholders não expandidos")

    return {"mod_id": expected_id, "version": version,
            "sha256": hashlib.sha256(path.read_bytes()).hexdigest()}


def artifact_for(project):
    folder = ROOT / "modules" / project / "build" / "libs"
    jars = sorted(p for p in folder.glob("*.jar")
                  if not p.stem.endswith(("-sources", "-javadoc", "-dev")))
    if len(jars) != 1:
        raise InvalidArtifact(f"{folder}: esperado exatamente 1 JAR compilado; encontrados {len(jars)}")
    return jars[0]


def main():
    try:
        for name, (mod_id, class_path, deps) in MODULES.items():
            result = validate_jar(artifact_for(name), mod_id, class_path, deps)
            print(f"OK: {mod_id} {result['version']} SHA256={result['sha256']}")
    except InvalidArtifact as exc:
        print(f"FALHA NA VERIFICAÇÃO: {exc}", file=sys.stderr)
        return 1
    print("Os quatro JARs parecem bem formados; ainda precisam de teste no jogo.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

#!/usr/bin/env python3
"""Validate all seven Lux Mods JARs and the assembled test pack (Python 3.11+)."""
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
    "lux-menu": ("lux_menu", "dev/luxmods/menu/LuxMenu.class", {"lux_engine"}),
    "lux-borderless": ("lux_borderless", "dev/luxmods/borderless/LuxBorderless.class", set()),
    "lux-dialogues": ("lux_dialogues", "dev/luxmods/dialogues/LuxDialogues.class", {"lux_engine"}),
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
            bytecode = jar.read(expected_class)
            # Minecraft 1.21.1 uses Java 21 (class file major version 65).
            if (len(bytecode) < 8 or bytecode[:4] != b"\\xca\\xfe\\xba\\xbe"
                    or int.from_bytes(bytecode[6:8], "big") != 65):
                raise InvalidArtifact(f"{path}: classe principal não é bytecode Java 21 válido")
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


def validate_test_pack(artifacts, pack_dir=None):
    """Make sure the distribution folder contains precisely the validated JARs."""
    pack_dir = Path(pack_dir) if pack_dir is not None else ROOT / "build/lux-testpack/mods"
    if not pack_dir.is_dir():
        raise InvalidArtifact(f"Pacote ausente: {pack_dir}")
    expected = {path.name for path in artifacts.values()}
    found = {path.name for path in pack_dir.glob("*.jar")}
    if expected != found:
        raise InvalidArtifact(
            f"Pacote incompleto ou com JARs extras: faltando={sorted(expected - found)}, "
            f"extras={sorted(found - expected)}"
        )
    for path in artifacts.values():
        packaged = pack_dir / path.name
        if hashlib.sha256(packaged.read_bytes()).digest() != hashlib.sha256(path.read_bytes()).digest():
            raise InvalidArtifact(f"JAR diferente no pacote: {path.name}")


def main():
    try:
        artifacts = {}
        for name, (mod_id, class_path, deps) in MODULES.items():
            artifact = artifact_for(name)
            result = validate_jar(artifact, mod_id, class_path, deps)
            artifacts[name] = artifact
            print(f"OK: {mod_id} {result['version']} SHA256={result['sha256']}")
        validate_test_pack(artifacts)
    except InvalidArtifact as exc:
        print(f"FALHA NA VERIFICAÇÃO: {exc}", file=sys.stderr)
        return 1
    print("Sete JARs e pacote unificado verificados; teste Minecraft ainda necessário.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

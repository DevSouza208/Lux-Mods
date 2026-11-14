"""Offline unit tests for the JAR structure validator (synthetic fixtures)."""
import tempfile
import unittest
import zipfile
from pathlib import Path
from verify_jars import InvalidArtifact, validate_jar, validate_test_pack, MODULES


def metadata(mod_id, deps):
    header = (
        'modLoader="javafml"\nloaderVersion="[1,)"\nlicense="All Rights Reserved"\n'
        + '[[mods]]\nmodId="' + mod_id + '"\nversion="0.1.0-SNAPSHOT"\n'
    )
    for dep in deps:
        header += (
            "\n[[dependencies." + mod_id + "]]\n"
            + 'modId="' + dep + '"\ntype="required"\nversionRange="[0,)"\n'
        )
    return header


class JarValidatorTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.path = Path(self.temp.name) / "file.jar"

    def tearDown(self):
        self.temp.cleanup()

    def fixture(self, mod="lux_engine", classname="dev/luxmods/engine/LuxEngine.class",
                deps=("minecraft", "neoforge"), override=None):
        with zipfile.ZipFile(self.path, "w") as jar:
            jar.writestr("META-INF/neoforge.mods.toml",
                         metadata(mod, deps) if override is None else override)
            if classname:
                jar.writestr(classname, b"fake class - structure only")

    def test_valid_engine(self):
        self.fixture()
        info = validate_jar(self.path, "lux_engine", "dev/luxmods/engine/LuxEngine.class")
        self.assertEqual("lux_engine", info["mod_id"])

    def test_valid_discovery(self):
        self.fixture("lux_discovery", "dev/luxmods/discovery/LuxDiscovery.class",
                     ("minecraft", "neoforge", "lux_engine"))
        result = validate_jar(self.path, "lux_discovery",
                              "dev/luxmods/discovery/LuxDiscovery.class", {"lux_engine"})
        self.assertEqual("lux_discovery", result["mod_id"])

    def test_valid_atlas(self):
        self.fixture("lux_atlas", "dev/luxmods/atlas/LuxAtlas.class",
                     ("minecraft", "neoforge", "lux_engine"))
        result = validate_jar(self.path, "lux_atlas",
                              "dev/luxmods/atlas/LuxAtlas.class", {"lux_engine"})
        self.assertEqual("lux_atlas", result["mod_id"])

    def test_atlas_must_require_engine(self):
        self.fixture("lux_atlas", "dev/luxmods/atlas/LuxAtlas.class")
        with self.assertRaises(InvalidArtifact):
            validate_jar(self.path, "lux_atlas",
                         "dev/luxmods/atlas/LuxAtlas.class", {"lux_engine"})

    def test_valid_hud(self):
        self.fixture("lux_hud", "dev/luxmods/hud/LuxHud.class",
                     ("minecraft", "neoforge", "lux_engine"))
        result = validate_jar(self.path, "lux_hud",
                              "dev/luxmods/hud/LuxHud.class", {"lux_engine"})
        self.assertEqual("lux_hud", result["mod_id"])

    def test_hud_requires_engine(self):
        self.fixture("lux_hud", "dev/luxmods/hud/LuxHud.class")
        with self.assertRaises(InvalidArtifact):
            validate_jar(self.path, "lux_hud",
                         "dev/luxmods/hud/LuxHud.class", {"lux_engine"})

    def test_valid_menu(self):
        self.fixture("lux_menu", "dev/luxmods/menu/LuxMenu.class",
                     ("minecraft", "neoforge", "lux_engine"))
        result = validate_jar(self.path, "lux_menu",
                              "dev/luxmods/menu/LuxMenu.class", {"lux_engine"})
        self.assertEqual("lux_menu", result["mod_id"])

    def test_menu_requires_engine(self):
        self.fixture("lux_menu", "dev/luxmods/menu/LuxMenu.class")
        with self.assertRaises(InvalidArtifact):
            validate_jar(self.path, "lux_menu",
                         "dev/luxmods/menu/LuxMenu.class", {"lux_engine"})

    def test_valid_borderless(self):
        self.fixture("lux_borderless", "dev/luxmods/borderless/LuxBorderless.class")
        result = validate_jar(self.path, "lux_borderless",
                              "dev/luxmods/borderless/LuxBorderless.class")
        self.assertEqual("lux_borderless", result["mod_id"])

    def test_valid_dialogues(self):
        self.fixture("lux_dialogues", "dev/luxmods/dialogues/LuxDialogues.class",
                     ("minecraft", "neoforge", "lux_engine"))
        result = validate_jar(self.path, "lux_dialogues",
                              "dev/luxmods/dialogues/LuxDialogues.class", {"lux_engine"})
        self.assertEqual("lux_dialogues", result["mod_id"])

    def test_dialogues_requires_engine(self):
        self.fixture("lux_dialogues", "dev/luxmods/dialogues/LuxDialogues.class")
        with self.assertRaises(InvalidArtifact):
            validate_jar(self.path, "lux_dialogues",
                         "dev/luxmods/dialogues/LuxDialogues.class", {"lux_engine"})

    def test_manifest_covers_seven_modules(self):
        self.assertEqual(7, len(MODULES))
        self.assertIn("lux-borderless", MODULES)
        self.assertIn("lux-dialogues", MODULES)

    def test_pack_rejects_missing_jar(self):
        with tempfile.TemporaryDirectory() as directory:
            package_dir = Path(directory) / "mods"
            package_dir.mkdir()
            with self.assertRaises(InvalidArtifact):
                validate_test_pack({"engine": self.path}, package_dir)

    def test_pack_rejects_modified_jar(self):
        self.fixture()
        with tempfile.TemporaryDirectory() as directory:
            package_dir = Path(directory) / "mods"
            package_dir.mkdir()
            (package_dir / self.path.name).write_bytes(b"modified")
            with self.assertRaises(InvalidArtifact):
                validate_test_pack({"engine": self.path}, package_dir)

    def test_pack_accepts_identical_jar(self):
        self.fixture()
        with tempfile.TemporaryDirectory() as directory:
            package_dir = Path(directory) / "mods"
            package_dir.mkdir()
            (package_dir / self.path.name).write_bytes(self.path.read_bytes())
            validate_test_pack({"engine": self.path}, package_dir)

    def test_missing_dependency(self):
        self.fixture("lux_discovery", "dev/luxmods/discovery/LuxDiscovery.class")
        with self.assertRaises(InvalidArtifact):
            validate_jar(self.path, "lux_discovery",
                         "dev/luxmods/discovery/LuxDiscovery.class", {"lux_engine"})

    def test_missing_class(self):
        self.fixture(classname=None)
        with self.assertRaises(InvalidArtifact):
            validate_jar(self.path, "lux_engine", "dev/luxmods/engine/LuxEngine.class")

    def test_wrong_id(self):
        self.fixture(mod="wrong")
        with self.assertRaises(InvalidArtifact):
            validate_jar(self.path, "lux_engine", "dev/luxmods/engine/LuxEngine.class")

    def test_unexpanded_version(self):
        self.fixture(override=metadata("lux_engine", ("minecraft", "neoforge"))
                     .replace("0.1.0-SNAPSHOT", "$mod_version"))
        with self.assertRaises(InvalidArtifact):
            validate_jar(self.path, "lux_engine", "dev/luxmods/engine/LuxEngine.class")

    def test_corrupt_archive(self):
        self.path.write_bytes(b"not a ZIP file")
        with self.assertRaises(InvalidArtifact):
            validate_jar(self.path, "lux_engine", "dev/luxmods/engine/LuxEngine.class")


if __name__ == "__main__":
    unittest.main()

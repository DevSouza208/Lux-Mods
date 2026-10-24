"""Test the preflight auditor against bad repository fixtures."""
import tempfile
import unittest
from pathlib import Path
from preflight import audit


class PreflightTests(unittest.TestCase):
    def test_empty_workspace_reports_missing_modules_and_wrapper(self):
        with tempfile.TemporaryDirectory() as tmp:
            errors = audit(Path(tmp))
            self.assertTrue(any("Arquivo ausente" in error for error in errors))
            self.assertTrue(any("Wrapper jar" in error for error in errors))

    def test_partial_workspace_detects_missing_crossproject_evaluation(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            (root / "settings.gradle").write_text("")
            (root / "build.gradle").write_text("mods { lux_discovery {} }")
            (root / "gradle.properties").write_text("minecraft_version=1.21.1")
            self.assertTrue(any("Falta ordem de avaliação" in e for e in audit(root)))


if __name__ == "__main__":
    unittest.main()

from io import StringIO
from pathlib import Path
import shutil
import unittest
from src.install import process_move_file, Status
from src.install_types import MoveFile
import src.install_utils as install_utils


class UtilTest(unittest.TestCase):
    def test_get_platform(self):
        self.assertEqual(install_utils.get_platform(), install_utils.Platform.MACOS)

    def test_get_effective_userid(self):
        self.assertEqual(install_utils.get_effective_user_id(), 501)

    def test_has_same_file_contents__same_file_contents(self):
        file1 = StringIO(
            "This is file.\nThere are many files like this.\nBut this file is mine."
        )
        file2 = StringIO(
            "This is file.\nThere are many files like this.\nBut this file is mine."
        )
        self.assertTrue(install_utils._have_same_file_contents(file1, file2))

    def test_has_same_file_contents__line_has_change(self):
        file1 = StringIO(
            "This is file.\nThere are many files like this.\nBut this file is mine."
        )
        file2 = StringIO(
            "This is my file.\nThere are many files like this.\nBut this file is mine."
        )
        self.assertFalse(install_utils._have_same_file_contents(file1, file2))

    def test_has_same_file_contents__first_file_shorter(self):
        file1 = StringIO("This is file.\nThere are many files like this.")
        file2 = StringIO(
            "This is file.\nThere are many files like this.\nBut this file is mine."
        )
        self.assertFalse(install_utils._have_same_file_contents(file1, file2))

    def test_has_same_file_contents__first_file_longer(self):
        file1 = StringIO(
            "This is file.\nThere are many files like this.\nBut this file is mine."
        )
        file2 = StringIO("This is file.\nThere are many files like this.")
        self.assertFalse(install_utils._have_same_file_contents(file1, file2))

    def test_has_same_directory_contents__same_directories(self):
        d1 = Path("./install_test_data/dir")
        self.assertTrue(install_utils.have_same_directory_contents(d1, d1))

    def test_has_same_directory_contents__extra_dir(self):
        d1 = Path("./install_test_data/dir")
        d2 = Path("./install_test_data/dir_with_extra_dir")
        self.assertFalse(install_utils.have_same_directory_contents(d1, d2))

    def test_has_same_directory_contents__missing_dir(self):
        d1 = Path("./install_test_data/dir")
        d2 = Path("./install_test_data/dir_with_extra_dir")
        self.assertFalse(install_utils.have_same_directory_contents(d2, d1))

    def test_has_same_directory_contents__extra_file(self):
        d1 = Path("./install_test_data/dir")
        d2 = Path("./install_test_data/dir_with_extra_file")
        self.assertFalse(install_utils.have_same_directory_contents(d1, d2))

    def test_has_same_directory_contents__missing_file(self):
        d1 = Path("./install_test_data/dir")
        d2 = Path("./install_test_data/dir_with_extra_file")
        self.assertFalse(install_utils.have_same_directory_contents(d2, d1))

    def test_has_same_directory_contents__file_has_difference(self):
        d1 = Path("./install_test_data/dir")
        d2 = Path("./install_test_data/dir_with_file_differences")
        self.assertFalse(install_utils.have_same_directory_contents(d1, d2))

    def test_process_pair(self):
        test_dir = Path("./testing")
        if test_dir.exists():
            shutil.rmtree(test_dir)
        test_dir.mkdir()

        try:
            src = Path("./install_test_data/sample.conf")
            dest = test_dir / "sample.conf"

            pair = MoveFile(src, dest)
            self.assertEqual(process_move_file(pair), Status.PASSED)
            self.assertTrue(install_utils.have_same_file_contents(src, dest))

        finally:
            # Clean up
            if test_dir.exists():
                shutil.rmtree(test_dir)


if __name__ == "__main__":
    unittest.main()

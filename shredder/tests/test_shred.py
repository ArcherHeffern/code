from pathlib import Path
from unittest import TestCase
from shredder import shred, shred_dir
from utils.testing import create_directory_with_data
from utils.common import prompt_yn


class TestShred(TestCase):
    def test_shred(self):
        with create_directory_with_data() as d:
            self.assertTrue(prompt_yn(f"Was the directory {str(d)} created? "))
            shred_dir(d, remove=False)
            self.assertTrue(prompt_yn("Was the directory shredded? "))

        # f = Path("TESTING")
        # f.write_text("Hello world")
        # shred(f, remove=False)
        # self.assertTrue(prompt_yn("Was the file shredded? "))
        # f.unlink()

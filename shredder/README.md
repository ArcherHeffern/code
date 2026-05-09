# Description
This is a python library for securely deleting (shredding) files and directories. The code calls into [rozbb's](github.com/rozbb) [rust-shred](github.com/rozbb/rust-shred) library using Py03 and maturin. I merely modernized the code (it was written >10 years ago) and made it callable from Python

# Why Shredding? 
Computers typically "delete" files by removing it's file system reference. They do NOT overwrite the file's data. This means disk recovery tools can possibly find and recover the file.  

So why do we overwrite multiple times?  

Each time a transistor is toggled, slight remenants of the previous states remain. This means parts of files can still be recovered using state of the art tools. Using multiple passes effectively burns out all traces of the old state, the more passes, the better.

# Warning
There are cases where data is duplicated which this deletion method doesn't detect and handle. Examples are the file system journal and overprovisioned SSD's.  

I suggest reading [NIST Special Publication 800 NIST SP 800-88r2](https://doi.org/10.6028/NIST.SP.800-88r2) section '3.1.1. Clear Sanitization Method' for more information.  

If privacy is a true goal of yours, You'd be far better off switching to an operating system like TAILS.  


# Example Usage
```python
from test_stuff import shred
from pathlib import Path


f = Path("./secrets")
f.write_text("My password is foo123!")
shred(
    path: f, # Which file are we shredding
    n_passes: 10, # How many times is this file overwritten with random bytes
    remove: True, # Is this file deleted after shredding
    size: None, # How many bytes we want to shred. If None, the entire file is shredded.
    exact: True, # If true, delete exactly size bytes, otherwise delete size rounded up to the next page table boundary
    zero: True, # Do we do an additional pass to zero out the file.
    verbose: False, # Do we print debug information
)
```
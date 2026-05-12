# Description
This is a rust program for securely deleting (shredding) files and directories. The code calls into a somewhat modernized implementation [rozbb's](github.com/rozbb) [rust-shred](https://github.com/rozbb/rust-shred) library.

# Why Shredding? 
Computers typically "delete" files by removing it's file system reference. They do NOT overwrite the file's data. This means disk recovery tools can possibly locate and recover the file.  

So why do we overwrite multiple times?  

Each time a transistor is toggled, slight remenants of the previous states remain. This means parts of files can still be recovered using state of the art tools. Using multiple passes effectively burns out all traces of the old state, the more passes, the better.

# Warning
There are cases where data is duplicated which this deletion method doesn't detect and handle. Examples are the file system journal and overprovisioned SSD's.  

I suggest reading [NIST Special Publication 800 NIST SP 800-88r2](https://doi.org/10.6028/NIST.SP.800-88r2) section '3.1.1. Clear Sanitization Method' for more information.  

If privacy is a true goal of yours, You'd be far better off switching to operating systems like TAILS or Qubes. 


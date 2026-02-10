# This project
See github.com/rozbb/rust-shred

I merely modernized it, since the original code is from >10 years ago and some langauge features have broken. 

I also made it callable from python using Py03. 

## Example Usage
```python
from test_stuff import shred
from pathlib import Path


f = Path("./secrets")
f.write_text("My password is foo123!")
shred(
    path: f,
    n_passes: 3,
    remove: True,
    size: None,
    exact: True,
    zero: True,
    verbose: True,
)
```
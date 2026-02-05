# Usage
1. Insall Homebrew
2. Install dependencies `./install-packages`
3. Install `python3.13 ./install.py`
4. Install zoxide
[zoxide](https://github.com/ajeetdsouza/zoxide)
```bash
curl -sSfL https://raw.githubusercontent.com/ajeetdsouza/zoxide/main/install.sh | sh
```

5. Install Vundle

[Vundle](https://github.com/VundleVim/Vundle.vim)
```bash
git clone https://github.com/VundleVim/Vundle.vim.git ~/.vim/bundle/Vundle.vim
```
Launch vim and run `:PluginInstall`

6. Set custom new tab page
- Download 'New Tab Redirect' extension
- Turn on 'Allow access to file URLs' permission
- Set path to 'file://~./chrome-home.html' (But expand it)

# TODO
- Implement MoveDir
- Move install-packages to python script
- Combine AScripts with this
- Add version and hash to generated files. If hash matches and version is lesser - We can auto overwrite
- Split process_move_file into multiple functions
- create_var_www_dir is getting permissions errors. Can fix with either: 
    - Ability to run sudo programs occasionally but also know main users home directory.
        - Can create a main account detection system (Find all users then ask which is main user. Then cache this to be at top of list for all subsequent runs)
    - Create templating system for my plists to use non sudo file system.
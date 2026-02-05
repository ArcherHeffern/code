echo "Running .zprofile"

source ~/.archer_profile


eval "$(zoxide init zsh)"
bindkey -v
bindkey '^R' history-incremental-search-backward

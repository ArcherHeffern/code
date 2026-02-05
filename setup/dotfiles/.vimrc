""""""""""""""""
" Vundle Setup "
""""""""""""""""
set nocompatible              " be iMproved, required
filetype off                  " required

" set the runtime path to include Vundle and initialize
set rtp+=~/.vim/bundle/Vundle.vim
call vundle#begin()

Plugin 'VundleVim/Vundle.vim'
Plugin 'kopischke/vim-fetch'

call vundle#end()            " required
filetype plugin indent on    " required


"""""""""""""""""
" General Setup "
"""""""""""""""""

syntax on
set relativenumber
set sw=4 ts=4
noremap <C-d> <C-d>zz
noremap <C-u> <C-u>zz
noremap j jzz
noremap k kzz
noremap n nzz
noremap N Nzz
noremap <Space> :Ex<CR>
colorscheme tender
set path+=/usr/include/x86_64-linux-gnu
set autoindent
command! -nargs=+ -complete=shellcmd Run botright new | 0read !<args>

" Auto Closing Brackets: https://stackoverflow.com/questions/21316727/automatic-closing-brackets-for-vim
"inoremap " ""<left>
"inoremap ' ''<left>
"inoremap ( ()<left>
"inoremap [ []<left>
"inoremap { {}<left>
"inoremap {<CR> {<CR>}<ESC>O
"inoremap {;<CR> {<CR>};<ESC>O

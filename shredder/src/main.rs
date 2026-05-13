mod shredder;
use std::path::PathBuf;

use clap::Parser;

use crate::shredder::{shred, shred_dir};
use walkdir::DirEntry;

#[derive(Parser, Debug)]
#[command(version, about, long_about = None)]
struct Args {
    /// Name of the person to greet
    paths: Vec<PathBuf>,

    #[arg(short, long, default_value_t = true)]
    remove: bool,

    #[arg(short, long, default_value_t = false)]
    verbose: bool,

    #[arg(short, long, default_value_t = false)]
    recursive: bool,
}
fn main() {
    let args = Args::parse();

    if args.paths.len() == 0 {
        eprintln!("No paths provided :(")
    }

    for p in args.paths {
        if !p.exists() {
            eprintln!("Path {p:?} does not exist");
        } else if p.is_dir() {
            println!("Shredding directory {p:?}");
            if !shred_dir(p.clone(), args.remove, args.verbose, args.recursive) {
                eprintln!("Failed to shred directory {p:?}");
            }
        } else if p.is_file() {
            println!("Shredding file {p:?}");
            if !shred(p.clone(), 10, args.remove, None, true, true, args.verbose) {
                eprintln!("Failed to shred file {p:?}");
            }
        } else {
            eprintln!("Unknown file type of {p:?}")
        }
    }
}

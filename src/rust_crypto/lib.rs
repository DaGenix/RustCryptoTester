#[link(name = "rust-crypto",
       vers = "0.1-pre",
       uuid = "e8b901bb-dcef-4d06-8e78-1ff2872822dc",
       url = "https://github.com/DaGenix/rust-crypto/src/rust-crypto")];
#[license = "MIT/ASL2"];
#[crate_type = "lib"];

extern mod extra;

mod checkedcast;
mod cryptoutil;
pub mod digest;
pub mod hmac;
pub mod mac;
pub mod md5;
pub mod pbkdf2;
pub mod scrypt;
pub mod sha1;
pub mod sha2;
mod vec_util;

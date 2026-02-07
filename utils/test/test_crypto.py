import utils.crypto as crypto

if __name__ == "__main__":
    message = b"hello"
    not_message = b"jello"
    pub, priv = crypto.create_key_pair_for_signing()
    signature = crypto.sign(message, priv)
    print(crypto.verify(signature, message, pub))
    print(crypto.verify(signature, not_message, pub))

    pb, prv = crypto.create_key_pair_for_encryption()
    ciphertext = crypto.encrypt(message, pb)
    print(crypto.decrypt(ciphertext, prv))

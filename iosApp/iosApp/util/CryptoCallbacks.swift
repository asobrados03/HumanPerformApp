import Foundation
import CommonCrypto
import Security
import shared

enum CryptoError: Error {
    case keyError
    case cryptError(status: CCCryptorStatus)
}

class CryptoCallbacks {
    private static let keyTag = "com.humanperformcenter.cryptoKey" // identificador en Keychain
    private static let keySize = kCCKeySizeAES256

    static func register() {
        EncryptionHandler.shared.registerEncryptor { input in
            let plain = input.toData()
            do {
                let key = try getOrCreateKey()
                let iv = randomIV()
                let encrypted = try aesCrypt(operation: CCOperation(kCCEncrypt),
                                             data: plain,
                                             key: key,
                                             iv: iv)
                // Mensaje = IV + ciphertext
                return (iv + encrypted).toKotlinByteArray()
            } catch {
                NSLog("Encryption failed: \(error)")
                return KotlinByteArray(size: 0)
            }
        }

        EncryptionHandler.shared.registerDecryptor { input in
            let all = input.toData()
            guard all.count >= kCCBlockSizeAES128 else {
                NSLog("Cipher too short")
                return KotlinByteArray(size: 0)
            }

            let iv = all.prefix(kCCBlockSizeAES128)
            let body = all.dropFirst(kCCBlockSizeAES128)

            do {
                let key = try getOrCreateKey()
                let decrypted = try aesCrypt(operation: CCOperation(kCCDecrypt),
                                             data: Data(body),
                                             key: key,
                                             iv: Data(iv))
                return decrypted.toKotlinByteArray()
            } catch {
                // No abortamos: devolvemos array vacío
                NSLog("Decryption failed: \(error)")
                return KotlinByteArray(size: 0)
            }
        }
    }

    // MARK: - Keychain helpers

    private static func getOrCreateKey() throws -> Data {
        let query: [String: Any] = [
            kSecClass as String: kSecClassKey,
            kSecAttrApplicationTag as String: keyTag,
            kSecAttrKeySizeInBits as String: keySize * 8,
            kSecReturnData as String: true
        ]

        var keyRef: CFTypeRef?
        let status = SecItemCopyMatching(query as CFDictionary, &keyRef)

        if status == errSecSuccess, let data = keyRef as? Data {
            return data
        }

        // No existe, generamos una clave nueva
        var keyData = Data(count: keySize)
        let result = keyData.withUnsafeMutableBytes {
            SecRandomCopyBytes(kSecRandomDefault, keySize, $0.baseAddress!)
        }
        guard result == errSecSuccess else { throw CryptoError.keyError }

        let addQuery: [String: Any] = [
            kSecClass as String: kSecClassKey,
            kSecAttrApplicationTag as String: keyTag,
            kSecAttrKeySizeInBits as String: keySize * 8,
            kSecValueData as String: keyData
        ]
        SecItemAdd(addQuery as CFDictionary, nil)

        return keyData
    }

    private static func randomIV() -> Data {
        var iv = Data(count: kCCBlockSizeAES128)
        let status = iv.withUnsafeMutableBytes {
            SecRandomCopyBytes(kSecRandomDefault, kCCBlockSizeAES128, $0.baseAddress!)
        }
        precondition(status == errSecSuccess, "IV generation failed")
        return iv
    }

    // MARK: - AES helper

    private static func aesCrypt(operation: CCOperation, data: Data, key: Data, iv: Data) throws -> Data {
        var outLength: size_t = 0
        let options = CCOptions(kCCOptionPKCS7Padding)
        var outData = Data(count: data.count + kCCBlockSizeAES128)

        let status: CCCryptorStatus = outData.withUnsafeMutableBytes { outBytes in
            guard let outBase = outBytes.baseAddress else { return CCCryptorStatus(kCCMemoryFailure) }
            return data.withUnsafeBytes { dataBytes in
                guard let dataBase = dataBytes.baseAddress else { return CCCryptorStatus(kCCMemoryFailure) }
                return key.withUnsafeBytes { keyBytes in
                    guard let keyBase = keyBytes.baseAddress else { return CCCryptorStatus(kCCMemoryFailure) }
                    return iv.withUnsafeBytes { ivBytes in
                        guard let ivBase = ivBytes.baseAddress else { return CCCryptorStatus(kCCMemoryFailure) }
                        return CCCrypt(operation,
                                       CCAlgorithm(kCCAlgorithmAES),
                                       options,
                                       keyBase, key.count,
                                       ivBase,
                                       dataBase, data.count,
                                       outBase, outBytes.count,
                                       &outLength)
                    }
                }
            }
        }

        guard status == kCCSuccess else {
            throw CryptoError.cryptError(status: status)
        }

        outData.removeSubrange(outLength..<outData.count)
        return outData
    }
}

// MARK: - Utilidades para interoperabilidad

private extension Data {
    static func +(lhs: Data, rhs: Data) -> Data {
        var data = lhs
        data.append(rhs)
        return data
    }

    func toKotlinByteArray() -> KotlinByteArray {
        let byteArray = KotlinByteArray(size: Int32(self.count))
        for (i, b) in self.enumerated() {
            byteArray.set(index: Int32(i), value: Int8(bitPattern: b))
        }
        return byteArray
    }
}

private extension KotlinByteArray {
    func toData() -> Data {
        var array = [UInt8](repeating: 0, count: Int(self.size))
        for i in 0..<Int(self.size) {
            array[i] = UInt8(bitPattern: self.get(index: Int32(i)))
        }
        return Data(array)
    }
}

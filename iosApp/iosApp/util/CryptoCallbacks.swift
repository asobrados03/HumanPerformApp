import Foundation
import CommonCrypto
import Security
import shared

private let keyString = "01234567890123456789012345678901" // 32-byte key for AES-256
private let keyData = keyString.data(using: .utf8)!

class CryptoCallbacks {
    static func register() {
        EncryptionHandler.shared.registerEncryptor { input in
            let plain = input.toData()
            let iv = randomIV()
            let encrypted: Data
            do {
                encrypted = try aesCrypt(operation: CCOperation(kCCEncrypt), data: plain, key: keyData, iv: iv)
            } catch {
                fatalError("Encryption failed: \(error)")
            }
            return (iv + encrypted).toKotlinByteArray()
        }

        EncryptionHandler.shared.registerDecryptor { input in
            let allData = input.toData()
            let iv = allData.prefix(kCCBlockSizeAES128)
            let data = allData.dropFirst(kCCBlockSizeAES128)
            do {
                let decrypted = try aesCrypt(operation: CCOperation(kCCDecrypt), data: Data(data), key: keyData, iv: Data(iv))
                return decrypted.toKotlinByteArray()
            } catch {
                fatalError("Decryption failed: \(error)")
            }
        }
    }

    private static func randomIV() -> Data {
        var iv = Data(count: kCCBlockSizeAES128)
        _ = iv.withUnsafeMutableBytes { SecRandomCopyBytes(kSecRandomDefault, kCCBlockSizeAES128, $0.baseAddress!) }
        return iv
    }

    private static func aesCrypt(operation: CCOperation, data: Data, key: Data, iv: Data) throws -> Data {
        var outLength: size_t = 0
        let options = CCOptions(kCCOptionPKCS7Padding)
        
        // Reservamos suficiente espacio (data.count + tamaño de bloque extra por padding)
        var outData = Data(count: data.count + kCCBlockSizeAES128)
        
        let status: CCCryptorStatus = outData.withUnsafeMutableBytes { outBytes in
            guard let outBase = outBytes.baseAddress else {
                return CCCryptorStatus(kCCMemoryFailure)
            }
            
            return data.withUnsafeBytes { dataBytes in
                guard let dataBase = dataBytes.baseAddress else {
                    return CCCryptorStatus(kCCMemoryFailure)
                }
                
                return key.withUnsafeBytes { keyBytes in
                    guard let keyBase = keyBytes.baseAddress else {
                        return CCCryptorStatus(kCCMemoryFailure)
                    }
                    
                    return iv.withUnsafeBytes { ivBytes in
                        guard let ivBase = ivBytes.baseAddress else {
                            return CCCryptorStatus(kCCMemoryFailure)
                        }
                        
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
            throw NSError(domain: "CryptoCallbacks", code: Int(status), userInfo: nil)
        }
        
        // Ajustamos el tamaño final según lo que realmente se cifró/descifró
        outData.removeSubrange(outLength..<outData.count)
        return outData
    }

}

private extension Data {
    static func +(lhs: Data, rhs: Data) -> Data {
        var data = lhs
        data.append(rhs)
        return data
    }

    func toKotlinByteArray() -> KotlinByteArray {
        let byteArray = KotlinByteArray(size: Int32(self.count))
        for (index, byte) in self.enumerated() {
            byteArray.set(index: Int32(index), value: Int8(bitPattern: byte))
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


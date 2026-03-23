import Foundation
import shared

extension Data {
    func asKotlinByteArray() -> KotlinByteArray {
        let byteArray = KotlinByteArray(size: Int32(count))
        for (index, byte) in enumerated() {
            byteArray.set(index: Int32(index), value: Int8(bitPattern: byte))
        }
        return byteArray
    }
}

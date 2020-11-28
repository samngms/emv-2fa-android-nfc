package emv.mfa

import kotlin.experimental.and

object TlvEncoder {
    fun encode(input: TlvData): ByteArray {
        val tag = if ( input.tag <= 0xFF ) {
            byteArrayOf(input.tag.toByte())
        } else {
            byteArrayOf(((input.tag shl 8) and 0xFF).toByte(), (input.tag and 0xFF).toByte())
        }
        return if (input.data.count() == 0) {
            tag + byteArrayOf(0x00)
        } else {
            tag + lenToByteArray(input.data.count()) + input.data
        }
    }

    fun lenToByteArray(len: Int): ByteArray {
        return if ( len <= 127 ) {
            byteArrayOf(len.toByte())
        } else {
            var lenArray = Util.toByteArray(len)
            if (lenArray.count() == 1 && (lenArray[0] and 0x80.toByte()) == 0.toByte()) {
                lenArray
            } else {
                byteArrayOf((0x80 + lenArray.count()).toByte()) + lenArray
            }
        }
    }
}
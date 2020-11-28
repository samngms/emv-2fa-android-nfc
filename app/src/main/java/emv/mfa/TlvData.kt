package emv.mfa

class TlvData(val tag: Int, val data: ByteArray) {
    fun isConstructed(): Boolean {
        return if (tag >= 0xFF) {
            (tag and 0b0010_0000_0000_0000) != 0
        } else {
            (tag and 0b0010_0000) != 0
        }
    }
}
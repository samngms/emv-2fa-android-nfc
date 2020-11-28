package emv.mfa

class CommandApdu(val cla: Int, val ins: Int, val p1: Int, val p2: Int) {
    var lc = 0x00
    var data: ByteArray? = null
        set(value) {
            lc = value!!.size
            field = value
        }
    var le : Int = 0x00
        set(value) {
            field = value
            this.leUsed = true
        }
    var leUsed = false


    fun toBytes(): ByteArray {
        var length = 4 // CLA, INS, P1, P2
        if (data != null && data!!.size != 0) {
            length += 1 // Lc
            length += data!!.size // DATA
        }
        if (leUsed) {
            length += 1 // LE
        }
        val apdu = ByteArray(length)
        var index = 0
        apdu[index] = cla.toByte()
        index++
        apdu[index] = ins.toByte()
        index++
        apdu[index] = p1.toByte()
        index++
        apdu[index] = p2.toByte()
        index++
        if (data != null && data!!.size != 0) {
            apdu[index] = lc.toByte()
            index++
            System.arraycopy(data!!, 0, apdu, index, data!!.size)
            index += data!!.size
        }
        if (leUsed) {
            var b = apdu[index] + le.toByte()
            apdu[index] = b.toByte() // LE
        }
        return apdu
    }
}

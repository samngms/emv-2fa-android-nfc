package emv.mfa

object Util {
    // only works if value is 0 to 99
    fun to4bitNumbers(value: Int): Byte {
        val p1 = value % 10
        val p10 = (value / 10) % 10
        return (((p10 and 0x0F) shl 4) or (p1 and 0x0F)).toByte()
    }

    fun toByteArray(value: Int): ByteArray {
        val count = if ( value <= 0xFF ) {
            1
        } else if (value <= 0xFFFF) {
            2
        } else if (value <= 0xFF_FFFF) {
            3
        } else {
            // max is 4
            4
        }
        val result = ByteArray(count)
        var tmp = value
        for(i in 0 until count) {
            result[count-1-i] = (tmp and 0xFF).toByte()
            tmp = tmp shr 8
        }
        return result
    }

    fun toInt(data : ByteArray, offset: Int=0): Int {
        if(data.size <= offset) throw IllegalArgumentException("can't convert to integer value")
        var value = 0
        var i = offset
        do {
            value = value shl 8
            value += (data[i++].toInt() and 0xFF)
        } while(i < data.size || i>=offset+4)
        return value
    }

    fun toHex(data: ByteArray): String {
        val buf = StringBuilder()
        for (b in data) {
            buf.append(String.format("%02X", b))
        }
        return buf.toString()
    }
}
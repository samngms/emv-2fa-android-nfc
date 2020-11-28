package emv.mfa

// see EMV book 3 Annex B Rules for BER-TLV Data Objects
object TlvDecoder {
    fun decodeAll(input: ByteArray): Array<TlvData> {
        return decode(input, true)
    }

    fun decode(input: ByteArray, decodeAll: Boolean=false, ignoreValue: Boolean=false): Array<TlvData> {
        var output = ArrayList<TlvData>()
        var i = 0
        var tag = 0
        var state = 0 // 0=read type, 1=read length
        while(i < input.size) {
            if (0 == state) {
                // reading type
                tag = input[i++].toInt() and 0xFF
                if ( (tag and 0b1_1111) == 0b1_1111 ) {
                    // need to read next byte, I can support more than 1 next byte if needed
                    var count = 0
                    do {
                        if ( ++count > 2 ) throw IllegalArgumentException("Invalid TLV type")
                        var tmp = input[i++].toInt() and 0xFF
                        tag = (tag shl 8) + tmp
                    } while((tmp and 0b1000_0000) != 0)
                }
                state = 1
            } else {
                // reading length
                var len = input[i++].toInt() and 0xFF
                if ((len and 0b1000_0000) != 0) {
                    var count = (len and 0b0111_1111)
                    if (count > 4) throw java.lang.IllegalArgumentException("number of length byte too large")
                    len = 0
                    do {
                        len = (len shl 8) + (input[i++].toInt() and 0xFF)
                    } while(--count>0)
                }
                val tlvData = if (ignoreValue) {
                    TlvData(tag, ByteArray(len))
                } else {
                    val tmp = i
                    i += len
                    TlvData(tag, input.copyOfRange(tmp, tmp+len))
                }
                output.add(tlvData)
                if (!ignoreValue && decodeAll && tlvData.isConstructed()) {
                    val sublist = decodeAll(tlvData.data)
                    output.addAll(sublist)
                }
                state = 0
            }
        }
        return output.toTypedArray()
    }
}
package emv.mfa

class AppFileLocator(val sif: Byte, val start: Byte, val end: Byte, val auth: Byte) {
     constructor(data: ByteArray) : this(data[0], data[1], data[2], data[3]) { }
}
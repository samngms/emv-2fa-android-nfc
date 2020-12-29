package emv.mfa

import kotlin.experimental.and

// See EMV 4.3 Book 3 C1 Application Interchange Profile
class AppInterchangeProfile(private val byte1: Byte, private val byte2: Byte) {
    // byte1
    // bit 8: RFU
    // bit 7: SDA supported
    // bit 6: DDA supported
    // bit 5: Cardholder verification is supported
    // bit 4: Terminal risk management is to be performed
    // bit 3: Issuer authentication is supported 19
    // bit 2: RFU
    // bit 1: CDA supported

    // byte2
    // bit 8: Reserved for use by the EMV Contactless Specifications
    // bit 7: RFU
    // bit 6: RFU
    // bit 5: RFU
    // bit 4: RFU
    // bit 3: RFU
    // bit 2: RFU
    // bit 1: RFU

    fun isDDA(): Boolean {
        return byte1 and 0b0010_0000.toByte() != 0.toByte()
    }
}
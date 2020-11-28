package emv.mfa

// refer to https://www.eftlab.com/the-use-of-ctqs-and-ttqs-in-nfc-transactions/
// also see US-VSDC-Contact-and-Contactless-Acquirer-Implementation-Guide-V3-June-2020_Accessible
// Chip – General term for VSDC which can be used to represent contact-chip functionality, contactless-chip functionality, or both.
// Contact – The contact functionality of a chip terminal. Also referred to as “contact VSDC” or “contact chip.”
// Contactless – The contactless functionality of a chip terminal. Also referred to as “contactless chip” or “qVSDC.”
class TerminalTransactionQualifiers {
    // 8	True	Contact MSD supported, MSD = Magnetic Stripe Data <-- eftlab said it is contactless but I believe it is contract
    // 7	True	Contact VSDC supported, VSDC = Visa Smart Debit/Credit <-- eftlab said it is contactless but I believe it is contract
    // 6	True	Contactless qVSDC supported, qVSDC = Quick Visa Smart Debit/Credit (=Visa payWave??)
    // 5	True	EMV contact chip supported
    // 4	True	Offline-only reader
    // 3	True	Online PIN supported
    // 2	True	Signature supported
    // 1	True	Offline Data Authentication (ODA) for Online Authorizations supported.
    var byte1: Byte = 0b0001_1000

    // 8	True	Online cryptogram required
    // 7	True	CVM required
    // 6	True	(Contact Chip) Offline PIN supported
    // 5	N/A	    RFU
    // 4	N/A	    RFU
    // 3	N/A	    RFU
    // 2	N/A	    RFU
    // 1	N/A	    RFU
    var byte2: Byte = 0

    // 8	True	Issuer Update Processing Supported
    // 7	True	Mobile functionality supported (Consumer Device CVM)
    // 6	N/A	    RFU
    // 5	N/A	    RFU
    // 4	N/A	    RFU
    // 3	N/A	    RFU
    // 2	N/A	    RFU
    // 1	N/A	    RFU
    var byte3: Byte = 0

    fun getBytes() : ByteArray {
        return byteArrayOf(byte1, byte2, byte3)
    }
}
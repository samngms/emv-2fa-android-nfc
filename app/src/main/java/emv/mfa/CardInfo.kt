package emv.mfa

import java.math.BigInteger
import java.security.MessageDigest
import java.util.*

class CardInfo {
    var cardHolderName: String? = null
    var caIndex : ByteArray? = null
    var caCert : RootCert? = null

    var issuerCertData : ByteArray? = null // this is actually "encrypted" cert data (signed to be even more accurate)
    var issuerPubKeyRemainder : ByteArray? = null
    var issuerPubExp : ByteArray? = null
    var issuerCert : PublicCert? = null

    var iccCertData : ByteArray? = null // this is actually "encrypted" cert data (signed to be even more accurate)
    var iccPubKeyRemainder : ByteArray? = null
    var iccPubExp : ByteArray? = null
    var iccCert : PublicCert? = null

    fun parseCertificates() {
        if (null != caCert && null != issuerCertData && null != issuerPubExp) {
            issuerCert = parseCert(caCert!!, true, issuerCertData!!, issuerPubKeyRemainder, issuerPubExp!!)
        }

        if (null != issuerCert && null != iccCertData && null != iccPubExp) {
            iccCert = parseCert(issuerCert!!, false, iccCertData!!, iccPubKeyRemainder, iccPubExp!!)
        }
    }

    // the parser is according to EMV 4.3 Book 2, section 6.3, Table 13 and section 6.4, Table 14
    private fun parseCert(parentCert: BaseCert, paringIssuerData: Boolean, signedCertData : ByteArray, pubKeyRemainder: ByteArray?, exponent: ByteArray) : PublicCert {
        val mod = BigInteger(parentCert.modulus, 16)
        val exp = BigInteger(parentCert.exponent, 16)
        val m = BigInteger(1, signedCertData)
        // raise signedCertData to the power of exp again (mod modulus), it will be the original data again
        val certData = m.modPow(exp, mod).toByteArray()

        var leftMostPubKeyLen = certData.size - (if (paringIssuerData) 36 else 42)

        var i = 0
        if (0x6A != (certData[i++].toInt() and 0xFF)) throw IllegalArgumentException("Invalid cert data header")
        if (paringIssuerData) {
            if (0x02 != (certData[i++].toInt() and 0xFF)) throw IllegalArgumentException("Invalid cert format")
        } else {
            if (0x04 != (certData[i++].toInt() and 0xFF)) throw IllegalArgumentException("Invalid cert format")
        }

        val cert = PublicCert()

        if (paringIssuerData) {
            // the identifier is the first few digits of the credit card, we don't need it
            val identifier = toInt(certData, i)
            i += 4
        } else {
            var applicationPan = certData.sliceArray(i until i+10)
            i += 10
        }

        val expires = toInt(certData, i, 2)
        cert.expires = expiryHexToDate(expires)
        i += 2

        cert.serial = toInt(certData, i, 3)
        i += 3

        cert.hashAlgo = certData[i++].toInt() and 0xFF
        cert.pubKeyAlgo = certData[i++].toInt() and 0xFF

        val pubKeyLen = certData[i++].toInt() and 0xFF
        val pubKeyExpLen = certData[i++].toInt() and 0xFF

        var modulus = ByteArray(0)
        if (null != pubKeyRemainder) {
            modulus = certData.sliceArray(i until i+leftMostPubKeyLen) + pubKeyRemainder
            if (modulus.size != pubKeyLen) {
                throw IllegalArgumentException("Invalid public key remainder")
            }
        } else {
            if ( leftMostPubKeyLen < pubKeyExpLen ) {
                throw IllegalArgumentException("Invalid public key remainder")
            }
            modulus = certData.sliceArray(i until i+pubKeyLen)
        }
        cert.modulus = Util.toHex(modulus)
        i += leftMostPubKeyLen

        if (pubKeyExpLen != exponent.size) {
            throw IllegalArgumentException("Invalid exponent length")
        }
        cert.exponent = Util.toHex(exponent)

        val hash = certData.sliceArray(i until i+20)
        i += 20

        if (0xBC != (certData[i++].toInt() and 0xFF)) throw IllegalArgumentException("Invalid cert data trailer")

        return cert
    }

    fun authenticate(challenge: ByteArray, ddaRespSigned: ByteArray) : Boolean {
        val mod = BigInteger(iccCert?.modulus, 16)
        val exp = BigInteger(iccCert?.exponent, 16)
        val m = BigInteger(1, ddaRespSigned)
        // raise signedCertData to the power of exp again (mod modulus), it will be the original data again
        val ddaResp = m.modPow(exp, mod).toByteArray()

        val md = MessageDigest.getInstance("SHA-1")
        val xx = ddaResp.sliceArray(1..ddaResp.size-22)
        md.update(xx)
        md.update(challenge)
        val expected = md.digest()

        val hash = ddaResp.sliceArray(ddaResp.size-21..ddaResp.size-2)
        return expected != hash
    }

    private fun toInt(data : ByteArray, index: Int, maxCount: Int=4): Int {
        var i = index
        if(data.size <= i) throw IllegalArgumentException("can't convert to integer value")
        var value = 0
        do {
            value = value shl 8
            value += (data[i++].toInt() and 0xFF)
        } while(i < data.size && i<index+maxCount)
        return value
    }

    private fun expiryHexToDate(expiry: Int) : Date {
        val m0 = (expiry and 0b1111_0000_0000_0000) shr 12
        val m1 = (expiry and 0b0000_1111_0000_0000) shr 8
        val month = m0*10 + m1
        val y0 = (expiry and 0b0000_0000_1111_0000) shr 4
        val y1 = (expiry and 0b0000_0000_0000_1111)
        val year = y0*10 + y1

        val cal = GregorianCalendar(2000+year, month-1, 1)
        cal.add(Calendar.MONTH, 1)
        return cal.time
    }
}
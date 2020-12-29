package emv.mfa

import android.nfc.tech.IsoDep
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.util.*
import kotlin.collections.ArrayList
import emv.mfa.EmvTag.*
import java.math.BigInteger

class EmvReader(private val handle: IsoDep) {
    var aid: ByteArray = ByteArray(0)
    var label: String = ""
    var afsList = ArrayList<AppFileLocator>()
    val cardInfo = CardInfo()
    var pdol : TlvData? = null
    var aip = AppInterchangeProfile(0.toByte(), 0.toByte())

    fun process() {
        readAidAndLabel()
        selectAid(aid)
        getAfls()
        readFiles()
        dda()
    }

    fun readAidAndLabel() {
        // call select cmd with "2PAY.SYS.DDF01", which is the entry point
        // SELECT command
        val apdu = CommandApdu(0x00, 0xA4, 0x04, 0x00)
        // "2PAY.SYS.DDF01" is the entry point for PPSE, see Book B, Entry Point
        val b = "2PAY.SYS.DDF01".toByteArray()
        apdu.data = b
        apdu.le = 0
        val req = apdu.toBytes()
        val resp = handle.transceive(req);
        val bi = BigInteger(1, resp)
        val bis = bi.toString(16)
        assertStatus(resp)
        val data = TlvDecoder.decodeAll(resp.sliceArray(0..resp.size-3))
        val aidTlv = data.find { it.tag == AID.tag || it.tag == KERNEL_IDENTITY.tag }
        if (aidTlv != null) {
            aid = aidTlv.data
        }
        val labelTvl = data.find { it.tag == APP_LABEL.tag }
        if (labelTvl != null) {
            label = String(labelTvl.data, StandardCharsets.UTF_8)
        }

        val ddol = data.find { it.tag == DDOL.tag }
        val xx = 100
    }

    fun selectAid(_aid: ByteArray) {
        // SELECT command
        val apdu = CommandApdu(0x00, 0xA4, 0x04, 0x00)
        apdu.data = _aid
        apdu.le = 0
        val req = apdu.toBytes()
        val resp = handle.transceive(req)
        val bi = BigInteger(1, resp)
        val bis = bi.toString(16)
        assertStatus(resp)
        val data = TlvDecoder.decodeAll(resp.sliceArray(0..resp.size-3))

        pdol = data.find { it.tag == PDOL.tag }

        val ddol = data.find { it.tag == DDOL.tag }
        val xx = 100
    }

    fun getAfls() {
        var allData = ByteArray(0)
        if (null != pdol ) {
            val list = TlvDecoder.decode(pdol!!.data, decodeAll = true, ignoreValue = true)
            for(item in list) {
                EmvTerminal.handle(item)
                allData += item.data
            }
        }

        val send = TlvEncoder.encode(TlvData(0x83, allData))
        //val send = ByteArray(0)
        // GPO command
        val apdu = CommandApdu(0x80, 0xA8, 0x00, 0x00)
        apdu.data = send
        apdu.le = 0
        val req = apdu.toBytes()
        val resp = handle.transceive(req)
        val bi = BigInteger(1, resp)
        val bis = bi.toString(16)
        assertStatus(resp)
        val data = TlvDecoder.decodeAll(resp.sliceArray(0..resp.size-3))
        val ddol = data.find { it.tag == DDOL.tag }

        val aipTlv = data.find { it.tag == AIP.tag }
        if ( aipTlv != null && aipTlv?.data.count() == 2 ) {
            aip = AppInterchangeProfile(aipTlv.data[0], aipTlv.data[1])
            //if (!aip.isDDA()) {
            //    throw IllegalArgumentException("DDA not supported")
            //}
        }

        val aflTlv = data.find { it.tag == APP_FILE_LOCATOR.tag }
        if ( null != aflTlv ) {
            val count = aflTlv.data.size / 4
            for(i in 0..count-1) {
                afsList.add(AppFileLocator(aflTlv.data.sliceArray(i*4 until (i+1)*4)))
            }
        }
        val ddol2 = data.find { it.tag == DDOL.tag }
        val xx = 100
        val x = 100
    }

    fun readFiles() {
        val files = ArrayList<TlvData>()
        for(afl in afsList) {
            for(i in afl.start..afl.end) {
                // READ_RECORD command
                val apdu = CommandApdu(0x00, 0xB2, i, (afl.sif.toInt() or 0b100) and 0x00FF)
                apdu.le = 0
                val req = apdu.toBytes()
                val resp = handle.transceive(req)
                assertStatus(resp)
                val data = TlvDecoder.decodeAll(resp.sliceArray(0..resp.size-3))
                files.addAll(data)
            }
        }

        // to know what I am doing in here, see EMV 4.3 Book 2, 6.1.1, table 12

        var tmp = files.find { it.tag == CARDHOLDER_NAME.tag }
        if ( null != tmp ) {
            cardInfo.cardHolderName = String(tmp.data, StandardCharsets.UTF_8)
        }

        tmp = files.find { it.tag == CA_PUBLIC_KEY_INDEX.tag }
        if ( null != tmp ) {
            cardInfo.caIndex = tmp.data

            val rid = Util.toHex(aid).substring(0..9)
            val idx = Integer.parseInt(Util.toHex(tmp.data), 16)
            val now = Date()
            var ca = RootCA.list?.find {
                rid.equals(it.rid, true)
                        && idx == it.index
                        && (it.expires == null || now.before(it.expires))
                        && !"Test".equals(it.keyType, true)
            }
            cardInfo.caCert = ca
        }

        tmp = files.find { it.tag == ISSUER_PUBLIC_KEY_CERT.tag }
        if ( null != tmp ) {
            cardInfo.issuerCertData = tmp.data
        }

        tmp = files.find { it.tag == ISSUER_PUBLIC_KEY_REMAINDER.tag }
        if ( null != tmp ) {
            cardInfo.issuerPubKeyRemainder = tmp.data
        }

        tmp = files.find { it.tag == ISSUER_PUBLIC_KEY_EXP.tag }
        if ( null != tmp ) {
            cardInfo.issuerPubExp = tmp.data
        }

        tmp = files.find { it.tag == ICC_PUBLIC_KEY_CERT.tag }
        if ( null != tmp ) {
            cardInfo.iccCertData = tmp.data
        }

        tmp = files.find { it.tag == ICC_PUBLIC_KEY_REMAINDER.tag }
        if ( null != tmp ) {
            cardInfo.iccPubKeyRemainder = tmp.data
        }

        tmp = files.find { it.tag == ICC_PUBLIC_KEY_EXP.tag }
        if ( null != tmp ) {
            cardInfo.iccPubExp = tmp.data
        }

        cardInfo.parseCertificates()
        val x = 100
    }

    fun dda() {
        // DDA command
        val apdu = CommandApdu(0x00, 0x88, 0x00, 0x00)
        val random = ByteArray(4)
        SecureRandom.getInstanceStrong().nextBytes(random)
        apdu.data = random
        apdu.le = 0
        val req = apdu.toBytes()
        val resp = handle.transceive(req)
        assertStatus(resp)
        val data = TlvDecoder.decodeAll(resp.sliceArray(0..resp.size-3))
        // SIGNED_DYNAMIC_APPLICATION_DATA
        val ddaTlv = data.find { it.tag == SIGNED_DDA.tag }

        val ok = cardInfo.authenticate(apdu.data!!, ddaTlv?.data!!)
        val x = 100
    }

    fun assertStatus(input: ByteArray?, code: Int=0x9000) {
        if (null == input || input.size < 2) {
            throw IllegalArgumentException("Invalid status")
        }
        val sw = getStatus(input!!)
        if ( sw != code ) throw IllegalArgumentException("Invalid status: $sw")
    }

    fun getStatus(input: ByteArray) : Int {
        var sw = (input[input.size-2].toInt() and 0x00FF) shl 8
        sw += (input[input.size-1].toInt() and 0x00FF)
        return sw
    }
}
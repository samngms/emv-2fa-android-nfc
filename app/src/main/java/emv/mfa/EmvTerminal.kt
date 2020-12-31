package emv.mfa

import java.security.SecureRandom
import java.util.*

import emv.mfa.EmvTag.*

@Suppress("DEPRECATED_IDENTITY_EQUALS")
object EmvTerminal {
    fun handle(item: TlvData) {
        if (item.tag === TERMINAL_TRANSACTION_QUALIFIERS.tag) {
            TerminalTransactionQualifiers().getBytes().copyInto(item.data, 0)
        } else if (item.tag === TERMINAL_COUNTRY_CODE.tag) {
            // see EMV 4.3 Book 3 Table 33: Data Elements Dictionary
            // ISO 3166
            // format is n3 (2 bytes)
            byteArrayOf(Util.to4bitNumbers(CountryCode.US.code/100), Util.to4bitNumbers(CountryCode.US.code%100))
                    .copyInto(item.data)
        } else if (item.tag === TRANSACTION_CURRENCY_CODE.tag) {
            // see EMV 4.3 Book 3 Table 33: Data Elements Dictionary
            // ISO 4217
            // format is n3 (2 bytes)
            byteArrayOf(Util.to4bitNumbers(CurrencyCode.USD.code/100), Util.to4bitNumbers(CurrencyCode.USD.code%100))
                    .copyInto(item.data)
        } else if (item.tag === TRANSACTION_DATE.tag) {
            // see EMV 4.3 Book 3 Table 33: Data Elements Dictionary
            // format is YYMMDD n6 (3 bytes)
            val now = GregorianCalendar()
            val year = Util.to4bitNumbers(now.get(Calendar.YEAR) - 2000)
            val month = Util.to4bitNumbers(now.get(Calendar.MONTH) + 1)
            val day = Util.to4bitNumbers(now.get(Calendar.DAY_OF_MONTH))
            byteArrayOf(year, month, day).copyInto(item.data)
        } else if (item.tag === TRANSACTION_TYPE.tag) {
            // according to EMV 4.3 Book 3 Table 33: Data Elements Dictionary
            // it should be "the first two digits of the ISO 8583:1987 Processing Code"
            // https://en.wikipedia.org/wiki/ISO_8583#Processing_code
            // but still don't know what it should be, just set it to 0x00
            // expected 1 byte only
            byteArrayOf(0).copyInto(item.data)
        } else if (item.tag === AMOUNT_AUTHORISED_NUMERIC.tag) {
            // see EMV 4.3 Book 3 Table 33: Data Elements Dictionary
            // format is n12 (6 bytes)
            // we will set all to 0
            ByteArray(6).copyInto(item.data)
        } else if (item.tag === TERMINAL_TYPE.tag) {
            // see EMV 4.3 Book 3 A1 Terminal Type
            // annex E sample POS terminal
            // The coding of the terminal-related data for this example is the following:
            // * Terminal Type = '22'
            // * Terminal Capabilities, byte 1 = 'E0' (hexadecimal) byte 2 = 'A0' (hexadecimal) byte 3 = 'C0' (hexadecimal)
            // * Additional Terminal Capabilities, byte 1 = '50' (hexadecimal) byte 2 = '00' (hexadecimal) byte 3 = 'B0' (hexadecimal) byte 4 = 'B0' (hexadecimal) byte 5 = '01' (hexadecimal)
            byteArrayOf(0x22).copyInto(item.data)
        } else if (item.tag === TERMINAL_CAPABILITIES.tag) {
            // EMV 4.3 Book 4 A2 Terminal Capabilities
            // first byte: E0 = Manual key entry + magnetic strip + IC with contacts
            // second byte: A0 = Plaintext PIN for ICC verification + paper signature
            // third byte: C0 = SDA + DDA
            byteArrayOf(0xE0.toByte(), 0xA0.toByte(), 0xC0.toByte()).copyInto(item.data)
        } else if (item.tag === ADDITIONAL_TERMINAL_CAPABILITIES.tag) {
            // see EMV 4.3 Book 3 Table 33: Data Elements Dictionary
            // format is binary, total 5 bytes
            // also see EMV 4.3 Book 4 A3 Additional Terminal Capabilities
            // first byte: 50 = Goods + Cashback
            // second byte: 00
            // third byte: B0 = Numeric keys + Command keys + Function keys
            // forth byte: B0 = Print, attendant + Display, attendant + Display, cardholder
            // fifth byte: 01 = ??
            byteArrayOf(0x50.toByte(), 0, 0xB0.toByte(), 0xB0.toByte(), 0x01.toByte()).copyInto(item.data)
        } else if (item.tag === DS_REQUESTED_OPERATOR_ID.tag) {
            // `val` = BytesUtils.fromString("7345123215904501")
        } else if (item.tag === UNPREDICTABLE_NUMBER.tag) {
            SecureRandom().nextBytes(item.data)
        }
    }
}

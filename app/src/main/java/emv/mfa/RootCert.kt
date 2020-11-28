package emv.mfa

import emv.mfa.moshi.HexString
import com.squareup.moshi.Json


class RootCert : BaseCert() {
    @Json(name ="Issuer")
    var issuer : String? = null

    @Json(name ="RID Index")
    @HexString
    var index : Int = -1

    @Json(name ="RID List")
    var rid : String? = null

    @Json(name ="Key length")
    var keyLen: Int = -1

    @Json(name ="SHA1")
    var sha1: String? = null

    @Json(name ="Key Type")
    var keyType : String? = null
}
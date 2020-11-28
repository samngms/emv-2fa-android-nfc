package emv.mfa

import com.squareup.moshi.Json

class PublicCert : BaseCert() {
    @Json(name = "Serial Number")
    var serial = -1

    @Json(name = "Hash Algorithm")
    var hashAlgo = -1

    @Json(name = "Public Key Algorithm")
    var pubKeyAlgo = -1
}
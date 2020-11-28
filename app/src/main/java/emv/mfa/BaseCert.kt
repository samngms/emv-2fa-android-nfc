package emv.mfa

import emv.mfa.moshi.ExpiryDate
import com.squareup.moshi.Json
import java.util.*

open class BaseCert {
    @Json(name ="Exponent")
    var exponent : String? = null

    @Json(name ="Modulus")
    var modulus: String? = null

    @Json(name ="Expires")
    @ExpiryDate
    var expires : Date? = null
}
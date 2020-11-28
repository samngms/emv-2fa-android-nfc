package emv.mfa.moshi

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.text.SimpleDateFormat
import java.util.*

internal class ExpiryDateAdapter {
    // "31.12.2009"
    private val format = SimpleDateFormat("dd.MM.yyyy")

    @ToJson
    fun toJson(@ExpiryDate value: Date?): String {
        if (null == value ) return ""
        return format.format(value)
    }

    @FromJson
    @ExpiryDate
    fun fromJson(value: String): Date? {
        if ("N/A" == value || value.isBlank() ) return null
        return format.parse(value)
    }
}
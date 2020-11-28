package emv.mfa.moshi

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

internal class HexStringAdapter {
    @ToJson
    fun toJson(@HexString value: Int): String {
        return String.format("%04X", value)
    }

    @FromJson
    @HexString
    fun fromJson(value: String): Int {
        return Integer.parseInt(value, 16)
    }
}
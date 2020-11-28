package emv.mfa

// https://en.wikipedia.org/wiki/ISO_3166-1
enum class CountryCode(val code: Int) {
    GB(826),
    US(840)
}
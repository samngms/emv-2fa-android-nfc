package emv.mfa

// https://en.wikipedia.org/wiki/ISO_4217
enum class CurrencyCode(val code: Int) {
    EUR(978),
    GBP(826),
    USD(840)
}
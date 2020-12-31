package emv.mfa

// most of the data are from https://www.eftlab.com/knowledge-base/145-emv-nfc-tags/
// also see EMV 4.3 Book 1 B1 Data Elements by Name
enum class EmvTag(val tag: Int) {
    AID(0x4F),
    AIP(0x82), // Application Interchange Profile
    APP_LABEL(0x50),
    APP_FILE_LOCATOR(0x94),

    CARDHOLDER_NAME(0x5F20),

    CA_PUBLIC_KEY_INDEX(0x8F),

    ISSUER_PUBLIC_KEY_CERT(0x90),
    ISSUER_PUBLIC_KEY_REMAINDER(0x92),
    ISSUER_PUBLIC_KEY_EXP(0x9F32),

    ICC_PUBLIC_KEY_CERT(0x9F46),
    ICC_PUBLIC_KEY_REMAINDER(0x9F48),
    ICC_PUBLIC_KEY_EXP(0x9F47),

    KERNEL_IDENTITY(0x9F2A),
    PDOL(0x9F38), // Processing Options Data Object List (PDOL)
    DDOL(0x9F49), // Dynamic Data Authentication Data Object List (DDOL)
    DDOL2(0xDF25), // is it really DF25? https://idtechproducts.com/technical-post/emv-device-configuration-essentials/

    SIGNED_DDA(0x9F4B), // SIGNED_DYNAMIC_APPLICATION_DATA

    TERMINAL_TRANSACTION_QUALIFIERS(0x9F66),
    TERMINAL_COUNTRY_CODE(0x9F1A),
    TRANSACTION_CURRENCY_CODE(0x5F2A),
    TRANSACTION_DATE(0x9A),
    TRANSACTION_TYPE(0x9C),
    AMOUNT_AUTHORISED_NUMERIC(0x9F02),
    TERMINAL_TYPE(0x9F35),
    TERMINAL_CAPABILITIES(0x9F33),
    ADDITIONAL_TERMINAL_CAPABILITIES(0x9F40),
    DS_REQUESTED_OPERATOR_ID(0x9F5C),
    UNPREDICTABLE_NUMBER(0x9F37)
}
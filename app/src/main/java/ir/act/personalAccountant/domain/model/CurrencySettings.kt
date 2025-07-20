package ir.act.personalAccountant.domain.model

data class CurrencySettings(
    val currencyCode: String = "USD",
    val locale: String = "en_US"
) {
    companion object {
        val DEFAULT = CurrencySettings()
        
        val SUPPORTED_CURRENCIES = listOf(
            // Major currencies
            CurrencySettings("USD", "en_US"),
            CurrencySettings("EUR", "en_DE"),
            CurrencySettings("GBP", "en_GB"),
            CurrencySettings("JPY", "ja_JP"),
            CurrencySettings("CAD", "en_CA"),
            CurrencySettings("AUD", "en_AU"),
            CurrencySettings("CHF", "de_CH"),
            CurrencySettings("CNY", "zh_CN"),
            CurrencySettings("HKD", "zh_HK"),
            CurrencySettings("SGD", "en_SG"),
            CurrencySettings("NZD", "en_NZ"),
            CurrencySettings("SEK", "sv_SE"),
            CurrencySettings("NOK", "no_NO"),
            CurrencySettings("DKK", "da_DK"),
            CurrencySettings("PLN", "pl_PL"),
            CurrencySettings("CZK", "cs_CZ"),
            CurrencySettings("HUF", "hu_HU"),
            CurrencySettings("RUB", "ru_RU"),

            // Middle East & Africa
            CurrencySettings("AED", "ar_AE"),
            CurrencySettings("SAR", "ar_SA"),
            CurrencySettings("QAR", "ar_QA"),
            CurrencySettings("KWD", "ar_KW"),
            CurrencySettings("BHD", "ar_BH"),
            CurrencySettings("OMR", "ar_OM"),
            CurrencySettings("JOD", "ar_JO"),
            CurrencySettings("LBP", "ar_LB"),
            CurrencySettings("EGP", "ar_EG"),
            CurrencySettings("MAD", "ar_MA"),
            CurrencySettings("TND", "ar_TN"),
            CurrencySettings("ZAR", "en_ZA"),
            CurrencySettings("NGN", "en_NG"),
            CurrencySettings("KES", "en_KE"),
            CurrencySettings("GHS", "en_GH"),
            CurrencySettings("TRY", "tr_TR"),
            CurrencySettings("ILS", "he_IL"),

            // Asia Pacific
            CurrencySettings("INR", "hi_IN"),
            CurrencySettings("PKR", "ur_PK"),
            CurrencySettings("BDT", "bn_BD"),
            CurrencySettings("LKR", "si_LK"),
            CurrencySettings("NPR", "ne_NP"),
            CurrencySettings("BTN", "dz_BT"),
            CurrencySettings("MVR", "dv_MV"),
            CurrencySettings("THB", "th_TH"),
            CurrencySettings("VND", "vi_VN"),
            CurrencySettings("IDR", "id_ID"),
            CurrencySettings("MYR", "ms_MY"),
            CurrencySettings("PHP", "en_PH"),
            CurrencySettings("KRW", "ko_KR"),
            CurrencySettings("TWD", "zh_TW"),
            CurrencySettings("MMK", "my_MM"),
            CurrencySettings("KHR", "km_KH"),
            CurrencySettings("LAK", "lo_LA"),
            CurrencySettings("BND", "ms_BN"),
            CurrencySettings("MNT", "mn_MN"),
            CurrencySettings("KZT", "kk_KZ"),
            CurrencySettings("UZS", "uz_UZ"),
            CurrencySettings("KGS", "ky_KG"),
            CurrencySettings("TJS", "tg_TJ"),
            CurrencySettings("TMT", "tk_TM"),
            CurrencySettings("AFN", "fa_AF"),
            CurrencySettings("IRR", "fa_IR"),

            // Americas
            CurrencySettings("MXN", "es_MX"),
            CurrencySettings("BRL", "pt_BR"),
            CurrencySettings("ARS", "es_AR"),
            CurrencySettings("CLP", "es_CL"),
            CurrencySettings("COP", "es_CO"),
            CurrencySettings("PEN", "es_PE"),
            CurrencySettings("UYU", "es_UY"),
            CurrencySettings("BOB", "es_BO"),
            CurrencySettings("PYG", "es_PY"),
            CurrencySettings("VES", "es_VE"),
            CurrencySettings("GYD", "en_GY"),
            CurrencySettings("SRD", "nl_SR"),
            CurrencySettings("TTD", "en_TT"),
            CurrencySettings("JMD", "en_JM"),
            CurrencySettings("BBD", "en_BB"),
            CurrencySettings("BSD", "en_BS"),
            CurrencySettings("BZD", "en_BZ"),
            CurrencySettings("GTQ", "es_GT"),
            CurrencySettings("HNL", "es_HN"),
            CurrencySettings("NIO", "es_NI"),
            CurrencySettings("CRC", "es_CR"),
            CurrencySettings("PAB", "es_PA"),
            CurrencySettings("DOP", "es_DO"),
            CurrencySettings("HTG", "fr_HT"),
            CurrencySettings("CUP", "es_CU"),

            // Europe
            CurrencySettings("RON", "ro_RO"),
            CurrencySettings("BGN", "bg_BG"),
            CurrencySettings("HRK", "hr_HR"),
            CurrencySettings("RSD", "sr_RS"),
            CurrencySettings("BAM", "bs_BA"),
            CurrencySettings("MKD", "mk_MK"),
            CurrencySettings("ALL", "sq_AL"),
            CurrencySettings("MDL", "ro_MD"),
            CurrencySettings("UAH", "uk_UA"),
            CurrencySettings("BYN", "be_BY"),
            CurrencySettings("LTL", "lt_LT"),
            CurrencySettings("LVL", "lv_LV"),
            CurrencySettings("EEK", "et_EE"),
            CurrencySettings("ISK", "is_IS"),
            CurrencySettings("GEL", "ka_GE"),
            CurrencySettings("AMD", "hy_AM"),
            CurrencySettings("AZN", "az_AZ"),

            // Pacific
            CurrencySettings("FJD", "en_FJ"),
            CurrencySettings("PGK", "en_PG"),
            CurrencySettings("SBD", "en_SB"),
            CurrencySettings("VUV", "en_VU"),
            CurrencySettings("WST", "en_WS"),
            CurrencySettings("TOP", "en_TO"),
            CurrencySettings("XPF", "fr_PF")
        )
        
        fun getCurrencyDisplayName(currencyCode: String): String {
            return when (currencyCode) {
                // Major currencies
                "USD" -> "US Dollar ($)"
                "EUR" -> "Euro (€)"
                "GBP" -> "British Pound (£)"
                "JPY" -> "Japanese Yen (¥)"
                "CAD" -> "Canadian Dollar (C$)"
                "AUD" -> "Australian Dollar (A$)"
                "CHF" -> "Swiss Franc (CHF)"
                "CNY" -> "Chinese Yuan (¥)"
                "HKD" -> "Hong Kong Dollar (HK$)"
                "SGD" -> "Singapore Dollar (S$)"
                "NZD" -> "New Zealand Dollar (NZ$)"
                "SEK" -> "Swedish Krona (kr)"
                "NOK" -> "Norwegian Krone (kr)"
                "DKK" -> "Danish Krone (kr)"
                "PLN" -> "Polish Złoty (zł)"
                "CZK" -> "Czech Koruna (Kč)"
                "HUF" -> "Hungarian Forint (Ft)"
                "RUB" -> "Russian Ruble (₽)"

                // Middle East & Africa
                "AED" -> "UAE Dirham (د.إ)"
                "SAR" -> "Saudi Riyal (﷼)"
                "QAR" -> "Qatari Riyal (﷼)"
                "KWD" -> "Kuwaiti Dinar (د.ك)"
                "BHD" -> "Bahraini Dinar (ب.د)"
                "OMR" -> "Omani Rial (﷼)"
                "JOD" -> "Jordanian Dinar (د.ا)"
                "LBP" -> "Lebanese Pound (ل.ل)"
                "EGP" -> "Egyptian Pound (ج.م)"
                "MAD" -> "Moroccan Dirham (د.م.)"
                "TND" -> "Tunisian Dinar (د.ت)"
                "ZAR" -> "South African Rand (R)"
                "NGN" -> "Nigerian Naira (₦)"
                "KES" -> "Kenyan Shilling (KSh)"
                "GHS" -> "Ghanaian Cedi (₵)"
                "TRY" -> "Turkish Lira (₺)"
                "ILS" -> "Israeli Shekel (₪)"

                // Asia Pacific
                "INR" -> "Indian Rupee (₹)"
                "PKR" -> "Pakistani Rupee (₨)"
                "BDT" -> "Bangladeshi Taka (৳)"
                "LKR" -> "Sri Lankan Rupee (₨)"
                "NPR" -> "Nepalese Rupee (₨)"
                "BTN" -> "Bhutanese Ngultrum (Nu.)"
                "MVR" -> "Maldivian Rufiyaa (Rf)"
                "THB" -> "Thai Baht (฿)"
                "VND" -> "Vietnamese Dong (₫)"
                "IDR" -> "Indonesian Rupiah (Rp)"
                "MYR" -> "Malaysian Ringgit (RM)"
                "PHP" -> "Philippine Peso (₱)"
                "KRW" -> "South Korean Won (₩)"
                "TWD" -> "Taiwan Dollar (NT$)"
                "MMK" -> "Myanmar Kyat (K)"
                "KHR" -> "Cambodian Riel (៛)"
                "LAK" -> "Lao Kip (₭)"
                "BND" -> "Brunei Dollar (B$)"
                "MNT" -> "Mongolian Tugrik (₮)"
                "KZT" -> "Kazakhstani Tenge (₸)"
                "UZS" -> "Uzbekistani Som (so'm)"
                "KGS" -> "Kyrgyzstani Som (с)"
                "TJS" -> "Tajikistani Somoni (SM)"
                "TMT" -> "Turkmenistani Manat (T)"
                "AFN" -> "Afghan Afghani (؋)"
                "IRR" -> "Iranian Rial (﷼)"

                // Americas
                "MXN" -> "Mexican Peso (MX$)"
                "BRL" -> "Brazilian Real (R$)"
                "ARS" -> "Argentine Peso (AR$)"
                "CLP" -> "Chilean Peso (CL$)"
                "COP" -> "Colombian Peso (CO$)"
                "PEN" -> "Peruvian Sol (S/)"
                "UYU" -> "Uruguayan Peso (UY$)"
                "BOB" -> "Bolivian Boliviano (Bs.)"
                "PYG" -> "Paraguayan Guaraní (₲)"
                "VES" -> "Venezuelan Bolívar (Bs.)"
                "GYD" -> "Guyanese Dollar (G$)"
                "SRD" -> "Surinamese Dollar (Sr$)"
                "TTD" -> "Trinidad Dollar (TT$)"
                "JMD" -> "Jamaican Dollar (J$)"
                "BBD" -> "Barbadian Dollar (Bds$)"
                "BSD" -> "Bahamian Dollar (B$)"
                "BZD" -> "Belize Dollar (BZ$)"
                "GTQ" -> "Guatemalan Quetzal (Q)"
                "HNL" -> "Honduran Lempira (L)"
                "NIO" -> "Nicaraguan Córdoba (C$)"
                "CRC" -> "Costa Rican Colón (₡)"
                "PAB" -> "Panamanian Balboa (B/.)"
                "DOP" -> "Dominican Peso (RD$)"
                "HTG" -> "Haitian Gourde (G)"
                "CUP" -> "Cuban Peso (₱)"

                // Europe
                "RON" -> "Romanian Leu (lei)"
                "BGN" -> "Bulgarian Lev (лв)"
                "HRK" -> "Croatian Kuna (kn)"
                "RSD" -> "Serbian Dinar (РСД)"
                "BAM" -> "Bosnia Mark (КМ)"
                "MKD" -> "Macedonian Denar (ден)"
                "ALL" -> "Albanian Lek (L)"
                "MDL" -> "Moldovan Leu (L)"
                "UAH" -> "Ukrainian Hryvnia (₴)"
                "BYN" -> "Belarusian Ruble (Br)"
                "LTL" -> "Lithuanian Litas (Lt)"
                "LVL" -> "Latvian Lats (Ls)"
                "EEK" -> "Estonian Kroon (kr)"
                "ISK" -> "Icelandic Króna (kr)"
                "GEL" -> "Georgian Lari (₾)"
                "AMD" -> "Armenian Dram (֏)"
                "AZN" -> "Azerbaijani Manat (₼)"

                // Pacific
                "FJD" -> "Fijian Dollar (FJ$)"
                "PGK" -> "Papua New Guinea Kina (K)"
                "SBD" -> "Solomon Islands Dollar (SI$)"
                "VUV" -> "Vanuatu Vatu (VT)"
                "WST" -> "Samoan Tālā (WS$)"
                "TOP" -> "Tongan Paʻanga (T$)"
                "XPF" -> "CFP Franc (₣)"
                
                else -> currencyCode
            }
        }
        
        fun getCurrencySymbol(currencyCode: String): String {
            return when (currencyCode) {
                // Major currencies
                "USD" -> "$"
                "EUR" -> "€"
                "GBP" -> "£"
                "JPY" -> "¥"
                "CAD" -> "C$"
                "AUD" -> "A$"
                "CHF" -> "CHF"
                "CNY" -> "¥"
                "HKD" -> "HK$"
                "SGD" -> "S$"
                "NZD" -> "NZ$"
                "SEK" -> "kr"
                "NOK" -> "kr"
                "DKK" -> "kr"
                "PLN" -> "zł"
                "CZK" -> "Kč"
                "HUF" -> "Ft"
                "RUB" -> "₽"

                // Middle East & Africa
                "AED" -> "د.إ"
                "SAR" -> "﷼"
                "QAR" -> "﷼"
                "KWD" -> "د.ك"
                "BHD" -> "ب.د"
                "OMR" -> "﷼"
                "JOD" -> "د.ا"
                "LBP" -> "ل.ل"
                "EGP" -> "ج.م"
                "MAD" -> "د.م."
                "TND" -> "د.ت"
                "ZAR" -> "R"
                "NGN" -> "₦"
                "KES" -> "KSh"
                "GHS" -> "₵"
                "TRY" -> "₺"
                "ILS" -> "₪"

                // Asia Pacific
                "INR" -> "₹"
                "PKR" -> "₨"
                "BDT" -> "৳"
                "LKR" -> "₨"
                "NPR" -> "₨"
                "BTN" -> "Nu."
                "MVR" -> "Rf"
                "THB" -> "฿"
                "VND" -> "₫"
                "IDR" -> "Rp"
                "MYR" -> "RM"
                "PHP" -> "₱"
                "KRW" -> "₩"
                "TWD" -> "NT$"
                "MMK" -> "K"
                "KHR" -> "៛"
                "LAK" -> "₭"
                "BND" -> "B$"
                "MNT" -> "₮"
                "KZT" -> "₸"
                "UZS" -> "so'm"
                "KGS" -> "с"
                "TJS" -> "SM"
                "TMT" -> "T"
                "AFN" -> "؋"
                "IRR" -> "﷼"

                // Americas
                "MXN" -> "MX$"
                "BRL" -> "R$"
                "ARS" -> "AR$"
                "CLP" -> "CL$"
                "COP" -> "CO$"
                "PEN" -> "S/"
                "UYU" -> "UY$"
                "BOB" -> "Bs."
                "PYG" -> "₲"
                "VES" -> "Bs."
                "GYD" -> "G$"
                "SRD" -> "Sr$"
                "TTD" -> "TT$"
                "JMD" -> "J$"
                "BBD" -> "Bds$"
                "BSD" -> "B$"
                "BZD" -> "BZ$"
                "GTQ" -> "Q"
                "HNL" -> "L"
                "NIO" -> "C$"
                "CRC" -> "₡"
                "PAB" -> "B/."
                "DOP" -> "RD$"
                "HTG" -> "G"
                "CUP" -> "₱"

                // Europe
                "RON" -> "lei"
                "BGN" -> "лв"
                "HRK" -> "kn"
                "RSD" -> "РСД"
                "BAM" -> "КМ"
                "MKD" -> "ден"
                "ALL" -> "L"
                "MDL" -> "L"
                "UAH" -> "₴"
                "BYN" -> "Br"
                "LTL" -> "Lt"
                "LVL" -> "Ls"
                "EEK" -> "kr"
                "ISK" -> "kr"
                "GEL" -> "₾"
                "AMD" -> "֏"
                "AZN" -> "₼"

                // Pacific
                "FJD" -> "FJ$"
                "PGK" -> "K"
                "SBD" -> "SI$"
                "VUV" -> "VT"
                "WST" -> "WS$"
                "TOP" -> "T$"
                "XPF" -> "₣"
                
                else -> "$"
            }
        }
    }
}
package com.example.infocard.util

enum class AppLanguage(val value: String) {
    TR("tr"),
    EN("en");

    val displayName: String
        get() = when (this) {
            TR -> "Türkçe"
            EN -> "English"
        }

    companion object {
        fun fromValue(value: String): AppLanguage {
            return values().firstOrNull { it.value == value } ?: TR
        }
    }
}

enum class Loc {
    VISA,
    MASTERCARD,
    TROY,
    UNKNOWN_PROVIDER,
    SECURITY_SECTION,
    FACE_ID_PROTECTION,
    FACE_ID_DESC,
    ABOUT_SECTION,
    APP_VERSION,
    DEVELOPED_BY,
    SETTINGS_TITLE,
    CLOSE_BUTTON,
    LANGUAGE_SECTION,
    LANGUAGE_LABEL,
    WALLET_TITLE,
    EDIT_BUTTON,
    DONE_BUTTON,
    NO_CARDS_SAVED,
    TAP_TO_ADD_CARD,
    TOAST_COPIED,
    LOCKED_TITLE,
    LOCKED_DESC,
    VERIFY_IDENTITY,
    VALID_THRU,
    CANCEL,
    SAVE,
    ADD_CARD_TITLE,
    EDIT_CARD_TITLE,
    CAMERA_SCAN,
    CARD_NAME_PLACEHOLDER,
    CARD_PROVIDER_LABEL,
    CARD_HOLDER_PLACEHOLDER,
    CARD_NUMBER_PLACEHOLDER,
    EXPIRY_PLACEHOLDER,
    CVV_PLACEHOLDER,
    CARD_COLOR_HEADER,
    CARD_NAME_DEFAULT,
    SELECT,
    SCANNER_INSTRUCTION,
    SCANNER_STEADY;

    fun get(lang: AppLanguage): String {
        return when (this) {
            VISA -> "Visa"
            MASTERCARD -> "Mastercard"
            TROY -> "Troy"
            UNKNOWN_PROVIDER -> if (lang == AppLanguage.TR) "Belirtilmemiş" else "Not Specified"
            SECURITY_SECTION -> if (lang == AppLanguage.TR) "Güvenlik" else "Security"
            FACE_ID_PROTECTION -> if (lang == AppLanguage.TR) "Biyometrik Kilit" else "Biometric Lock"
            FACE_ID_DESC -> if (lang == AppLanguage.TR) "Uygulama açılırken şifre/biyometrik doğrulaması ister." else "Requires biometric/passcode authentication on startup."
            ABOUT_SECTION -> if (lang == AppLanguage.TR) "Hakkında" else "About"
            APP_VERSION -> if (lang == AppLanguage.TR) "Uygulama Sürümü" else "App Version"
            DEVELOPED_BY -> if (lang == AppLanguage.TR) "C4N3RR tarafından geliştirildi" else "Developed by C4N3RR"
            SETTINGS_TITLE -> if (lang == AppLanguage.TR) "Ayarlar" else "Settings"
            CLOSE_BUTTON -> if (lang == AppLanguage.TR) "Kapat" else "Close"
            LANGUAGE_SECTION -> if (lang == AppLanguage.TR) "Dil" else "Language"
            LANGUAGE_LABEL -> if (lang == AppLanguage.TR) "Uygulama Dili" else "App Language"
            WALLET_TITLE -> if (lang == AppLanguage.TR) "Cüzdan" else "Wallet"
            EDIT_BUTTON -> if (lang == AppLanguage.TR) "Düzenle" else "Edit"
            DONE_BUTTON -> if (lang == AppLanguage.TR) "Bitti" else "Done"
            NO_CARDS_SAVED -> if (lang == AppLanguage.TR) "Kayıtlı kart bulunmamaktadır." else "No cards saved."
            TAP_TO_ADD_CARD -> if (lang == AppLanguage.TR) "Eklemek için + butonuna dokunun." else "Tap the + button to add a card."
            TOAST_COPIED -> if (lang == AppLanguage.TR) "Kart numarası kopyalandı!" else "Card number copied!"
            LOCKED_TITLE -> if (lang == AppLanguage.TR) "Cüzdan Kilitli" else "Wallet Locked"
            LOCKED_DESC -> if (lang == AppLanguage.TR) "Kart bilgilerinize güvenli erişim için kimliğinizi doğrulayın." else "Verify your identity for secure access to your card details."
            VERIFY_IDENTITY -> if (lang == AppLanguage.TR) "Kimliği Doğrula" else "Verify Identity"
            VALID_THRU -> if (lang == AppLanguage.TR) "S.K.T." else "EXP"
            CANCEL -> if (lang == AppLanguage.TR) "İptal" else "Cancel"
            SAVE -> if (lang == AppLanguage.TR) "Kaydet" else "Save"
            ADD_CARD_TITLE -> if (lang == AppLanguage.TR) "Yeni Kart Ekle" else "Add New Card"
            EDIT_CARD_TITLE -> if (lang == AppLanguage.TR) "Kartı Düzenle" else "Edit Card"
            CAMERA_SCAN -> if (lang == AppLanguage.TR) "Kamera ile Hızlı Tara" else "Quick Scan with Camera"
            CARD_NAME_PLACEHOLDER -> if (lang == AppLanguage.TR) "Kart Adı (örn: Bonus Gold)" else "Card Name (e.g. Bonus Gold)"
            CARD_PROVIDER_LABEL -> if (lang == AppLanguage.TR) "Kart Sağlayıcı" else "Card Provider"
            CARD_HOLDER_PLACEHOLDER -> if (lang == AppLanguage.TR) "Kart Sahibi (İsteğe Bağlı)" else "Cardholder (Optional)"
            CARD_NUMBER_PLACEHOLDER -> if (lang == AppLanguage.TR) "Kart Numarası (16 haneli)" else "Card Number (16 digits)"
            EXPIRY_PLACEHOLDER -> if (lang == AppLanguage.TR) "AA/YY" else "MM/YY"
            CVV_PLACEHOLDER -> if (lang == AppLanguage.TR) "CVV" else "CVV"
            CARD_COLOR_HEADER -> if (lang == AppLanguage.TR) "KART RENGİ" else "CARD COLOR"
            CARD_NAME_DEFAULT -> if (lang == AppLanguage.TR) "Kart Adı" else "Card Name"
            SELECT -> if (lang == AppLanguage.TR) "Seç" else "Select"
            SCANNER_INSTRUCTION -> if (lang == AppLanguage.TR) "Kredi Kartınızı Çerçeve İçine Alın" else "Align Credit Card Within Frame"
            SCANNER_STEADY -> if (lang == AppLanguage.TR) "Kart taranıyor, lütfen sabit tutun..." else "Scanning card, please hold steady..."
        }
    }
}

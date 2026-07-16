package com.example.infocard

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.infocard.data.Card
import com.example.infocard.data.CardProvider
import com.example.infocard.data.CardStore
import com.example.infocard.data.CardTheme
import com.example.infocard.sensor.MotionManager
import com.example.infocard.theme.InfoCardTheme
import com.example.infocard.ui.view.CardEditSheet
import com.example.infocard.ui.view.LockedView
import com.example.infocard.ui.view.MainWalletView
import com.example.infocard.ui.view.SettingsSheet
import com.example.infocard.scanner.CardScannerActivity
import com.example.infocard.util.AppLanguage
import com.example.infocard.util.Loc

data class ScannedCard(
    val number: String?,
    val expiry: String?,
    val provider: CardProvider
)

sealed class ActiveSheet {
    object Add : ActiveSheet()
    data class Edit(val card: Card) : ActiveSheet()
}

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : FragmentActivity() {

    private lateinit var cardStore: CardStore
    private lateinit var motionManager: MotionManager
    private lateinit var sharedPreferences: SharedPreferences

    // Shared States
    private var isUnlocked by mutableStateOf(false)
    private var appLanguage by mutableStateOf(AppLanguage.TR)
    private var useBiometrics by mutableStateOf(true)

    private var activeSheet by mutableStateOf<ActiveSheet?>(null)
    private var showSettings by mutableStateOf(false)
    private var scannedCardResult by mutableStateOf<ScannedCard?>(null)

    private var isLaunchingScanner = false

    // Scanner Activity Launcher
    private val scannerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isLaunchingScanner = false
        if (result.resultCode == Activity.RESULT_OK) {
            val number = result.data?.getStringExtra("number")
            val expiry = result.data?.getStringExtra("expiry")
            val providerStr = result.data?.getStringExtra("provider") ?: "unknown"
            val provider = CardProvider.values().firstOrNull { it.value == providerStr } ?: CardProvider.UNKNOWN
            scannedCardResult = ScannedCard(number, expiry, provider)
            // Re-open add sheet when scanner returns
            activeSheet = ActiveSheet.Add
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize SharedPrefs and components
        sharedPreferences = getSharedPreferences("infocard_settings", Context.MODE_PRIVATE)
        appLanguage = AppLanguage.fromValue(sharedPreferences.getString("app_language", "tr") ?: "tr")
        useBiometrics = sharedPreferences.getBoolean("use_biometrics", true)

        cardStore = CardStore(applicationContext)
        motionManager = MotionManager(applicationContext)

        // Initial security verification
        authenticate()

        setContent {
            InfoCardTheme {
                Surface(
                    modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val roll by motionManager.roll.collectAsState()
                    val pitch by motionManager.pitch.collectAsState()

                    // Crossfade between Locked screen and main Dashboard
                    Crossfade(
                        targetState = isUnlocked,
                        label = "LockScreenCrossfade"
                    ) { unlocked ->
                        if (unlocked) {
                            MainWalletView(
                                language = appLanguage,
                                cardStore = cardStore,
                                roll = roll,
                                pitch = pitch,
                                onSettingsClick = { showSettings = true },
                                onAddCardClick = { activeSheet = ActiveSheet.Add },
                                onEditCardClick = { activeSheet = ActiveSheet.Edit(it) }
                            )
                        } else {
                            LockedView(
                                language = appLanguage,
                                onUnlock = { authenticate() }
                            )
                        }
                    }

                    // Bottom Sheet: Settings
                    if (showSettings) {
                        SettingsSheet(
                            language = appLanguage,
                            useBiometrics = useBiometrics,
                            onLanguageChange = { newLang ->
                                appLanguage = newLang
                                sharedPreferences.edit().putString("app_language", newLang.value).apply()
                            },
                            onBiometricsChange = { enabled ->
                                useBiometrics = enabled
                                sharedPreferences.edit().putBoolean("use_biometrics", enabled).apply()
                            },
                            onDismiss = { showSettings = false }
                        )
                    }

                    // Bottom Sheet: Add / Edit Card
                    activeSheet?.let { sheet ->
                        val cardToEdit = if (sheet is ActiveSheet.Edit) sheet.card else null

                        // If we have scanned result, pre-populate inputs (we pass it as custom initialization)
                        val initialScannedNumber = scannedCardResult?.number
                        val initialScannedExpiry = scannedCardResult?.expiry
                        val initialScannedProvider = scannedCardResult?.provider

                        val customInitialCard = if (scannedCardResult != null) {
                            Card(
                                name = "",
                                number = initialScannedNumber ?: "",
                                expiry = initialScannedExpiry ?: "",
                                cvv = "",
                                theme = CardTheme.PURPLE_PINK,
                                provider = initialScannedProvider ?: CardProvider.UNKNOWN
                            )
                        } else null

                        // Reset scanned result after reading
                        scannedCardResult = null

                        CardEditSheet(
                            language = appLanguage,
                            cardToEdit = cardToEdit ?: customInitialCard,
                            onSave = { savedCard ->
                                if (sheet is ActiveSheet.Edit) {
                                    cardStore.updateCard(savedCard)
                                } else {
                                    cardStore.addCard(savedCard)
                                }
                                activeSheet = null
                            },
                            onScanRequest = {
                                isLaunchingScanner = true
                                activeSheet = null // Close sheet before starting scanner to prevent overlapping
                                val intent = Intent(this@MainActivity, CardScannerActivity::class.java).apply {
                                    putExtra("lang", appLanguage.value)
                                }
                                scannerLauncher.launch(intent)
                            },
                            onDismiss = { activeSheet = null }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        motionManager.start()
        if (useBiometrics && !isUnlocked && !isLaunchingScanner) {
            authenticate()
        }
    }

    override fun onPause() {
        super.onPause()
        motionManager.stop()
    }

    override fun onStop() {
        super.onStop()
        // Lock screen instantly when backgrounded (unless launching scanner)
        if (useBiometrics && !isLaunchingScanner) {
            isUnlocked = false
        }
    }

    private fun authenticate() {
        if (!useBiometrics) {
            isUnlocked = true
            return
        }

        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // Remain locked
                    isUnlocked = false
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    isUnlocked = true
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    isUnlocked = false
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(Loc.LOCKED_TITLE.get(appLanguage))
            .setDescription(Loc.LOCKED_DESC.get(appLanguage))
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}

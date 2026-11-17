package com.example.data.config

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

data class PrivacyPolicySection(
  val id: String,
  val title: String,
  val summary: String,
  val bullets: List<String> = emptyList()
)

object PrivacyPolicyConfig {

  const val APP_NAME = "Noor"
  const val PACKAGE_ID = "com.aistudio.noorislamic.zpqtwv"
  const val EFFECTIVE_DATE = "November 15, 2026"
  const val DEVELOPER_CONTACT_EMAIL = "oyeyemiyunus24@gmail.com"

  /**
   * Public GitHub Pages Privacy Policy URL.
   * After pushing this repository to GitHub and enabling GitHub Pages from `main` -> `/docs`,
   * set this constant to `https://<your-username>.github.io/<your-repo>/privacy-policy.html`.
   * Kept blank until published so in-app navigation never opens an unverified 404 link.
   */
  const val HOSTED_PRIVACY_POLICY_URL = ""

  val sections: List<PrivacyPolicySection> = listOf(
    PrivacyPolicySection(
      id = "overview",
      title = "1. Overview & Zero-Ad Commitment",
      summary = "Noor is a reverent, 100% ad-free Islamic companion. We do not include advertising SDKs, sell personal data, or track your activity across external apps or websites.",
      bullets = listOf(
        "Complete 604-page offline Uthmani Mushaf and offline Adhan/Surah audio work without tracking.",
        "No behavioral ad profiling or third-party data brokers."
      )
    ),
    PrivacyPolicySection(
      id = "data_collected",
      title = "2. Data Processed & Account Sync",
      summary = "Only information directly needed to run your chosen features is processed:",
      bullets = listOf(
        "Optional Google Sign-In (Firebase Auth): Stores your Firebase UID, display name, and email strictly to authenticate and sync preferences.",
        "Cloud Sync (Firebase Firestore): Stores your selected city, prayer calculation method, prayer completion records, Khatm progress, and earned badges when signed in.",
        "Optional GPS Location: Used only on demand when you tap Auto-Detect My City via GPS to calculate accurate local Salah times."
      )
    ),
    PrivacyPolicySection(
      id = "permissions",
      title = "3. Android Permissions Used",
      summary = "Every permission declared by Noor maps to an explicit user-facing feature:",
      bullets = listOf(
        "Location (COARSE / FINE): Optional on-demand city detection for prayer timings; 150+ cities can also be picked manually.",
        "Notifications & Exact Alarms (POST_NOTIFICATIONS, SCHEDULE_EXACT_ALARM, BOOT_COMPLETED): Delivers reliable local Salah, Suhoor/Iftar, Friday Al-Kahf, Nightly Surahs, Adhkar, and Khatm alerts.",
        "Media Playback Service (FOREGROUND_SERVICE_MEDIA_PLAYBACK): Plays offline Adhan & Surah audio with playback controls.",
        "Appear on Top (SYSTEM_ALERT_WINDOW): Optional permission used only if you enable centered floating reminder cards."
      )
    ),
    PrivacyPolicySection(
      id = "local_storage",
      title = "4. On-Device Local Storage",
      summary = "Most personal worship history stays strictly on your device via Room SQLite and Android SharedPreferences:",
      bullets = listOf(
        "Daily Salah and Ramadan fasting logs, Tadabbur reflections, Quran bookmarks, and Day/Night reader settings.",
        "PDF and CSV exports generated locally only when you choose to export or share them."
      )
    ),
    PrivacyPolicySection(
      id = "third_party",
      title = "5. Third-Party Services & Donations",
      summary = "Noor uses Google Firebase solely for optional sign-in and encrypted cloud sync.",
      bullets = listOf(
        "Voluntary Sadaqah donations open Flutterwave (flutterwave.com/donate/udnd7pfu6sbt) in your external web browser.",
        "Noor never collects, views, or stores debit/credit card numbers or banking credentials."
      )
    ),
    PrivacyPolicySection(
      id = "deletion",
      title = "6. Data Retention & Deletion",
      summary = "You remain in full control of your local and cloud records:",
      bullets = listOf(
        "Local Data: Clear app storage in Android Settings or uninstall Noor at any time to wipe on-device records.",
        "Cloud Account Deletion: Email oyeyemiyunus24@gmail.com with subject 'Noor Data Deletion Request' and your signed-in Google email; verified requests are fulfilled within 7 business days."
      )
    ),
    PrivacyPolicySection(
      id = "contact",
      title = "7. Developer Contact",
      summary = "For privacy questions, children's privacy inquiries, or data deletion requests, reach out directly at oyeyemiyunus24@gmail.com."
    )
  )

  fun hasHostedUrl(url: String = HOSTED_PRIVACY_POLICY_URL): Boolean {
    val trimmed = url.trim()
    if (trimmed.isEmpty()) return false
    val parsed = Uri.parse(trimmed)
    return parsed.scheme == "https" && !parsed.host.isNullOrBlank()
  }

  fun buildOpenPolicyIntent(
    url: String = HOSTED_PRIVACY_POLICY_URL,
    newTask: Boolean = true
  ): Intent? {
    if (!hasHostedUrl(url)) return null
    return Intent(Intent.ACTION_VIEW, Uri.parse(url.trim())).apply {
      addCategory(Intent.CATEGORY_BROWSABLE)
      if (newTask) {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
      }
    }
  }

  fun buildDataDeletionEmailIntent(newTask: Boolean = true): Intent {
    val subject = Uri.encode("Noor Data Deletion Request ($PACKAGE_ID)")
    val body = Uri.encode(
      "Assalamu Alaikum,\n\n" +
        "Please delete my Noor cloud account and synced Firestore records associated with:\n" +
        "Google Account Email: [Enter your email here]\n\n" +
        "Thank you."
    )
    return Intent(
      Intent.ACTION_SENDTO,
      Uri.parse("mailto:$DEVELOPER_CONTACT_EMAIL?subject=$subject&body=$body")
    ).apply {
      if (newTask) {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
      }
    }
  }

  fun openHostedPolicy(context: Context) {
    val intent = buildOpenPolicyIntent(newTask = true) ?: return
    try {
      context.startActivity(intent)
    } catch (_: Exception) {
      copyText(context, "Noor Privacy Policy URL", HOSTED_PRIVACY_POLICY_URL, "Privacy Policy link copied!")
    }
  }

  fun openDataDeletionEmail(context: Context) {
    try {
      context.startActivity(buildDataDeletionEmailIntent(newTask = true))
    } catch (_: Exception) {
      copyContactEmail(context)
    }
  }

  fun copyContactEmail(context: Context) {
    copyText(
      context = context,
      label = "Noor Developer Contact Email",
      value = DEVELOPER_CONTACT_EMAIL,
      toastMessage = "Contact email copied: $DEVELOPER_CONTACT_EMAIL"
    )
  }

  private fun copyText(context: Context, label: String, value: String, toastMessage: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText(label, value))
    Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
  }
}

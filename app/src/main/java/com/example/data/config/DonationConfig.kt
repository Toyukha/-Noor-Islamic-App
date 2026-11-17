package com.example.data.config

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object DonationConfig {

  /**
   * Official Flutterwave Noor Sadaqah Jariyah online donation link.
   * Supports Debit/Credit Cards, Bank Transfer, USSD, Apple Pay, and Mobile Money.
   */
  const val FLUTTERWAVE_DONATION_URL = "https://flutterwave.com/donate/udnd7pfu6sbt"
  const val FLUTTERWAVE_DISPLAY_HOST = "flutterwave.com/donate/udnd7pfu6sbt"

  /**
   * Builds a clean browsable HTTPS intent targeting the Flutterwave donation portal.
   */
  fun buildDonationCheckoutIntent(newTask: Boolean = true): Intent {
    return Intent(Intent.ACTION_VIEW, Uri.parse(FLUTTERWAVE_DONATION_URL)).apply {
      addCategory(Intent.CATEGORY_BROWSABLE)
      if (newTask) {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
      }
    }
  }

  fun hasValidDonationUrl(): Boolean {
    val parsed = Uri.parse(FLUTTERWAVE_DONATION_URL)
    return parsed.scheme == "https" && !parsed.host.isNullOrBlank() && parsed.path?.contains("donate") == true
  }

  /**
   * Opens the secure Flutterwave checkout in the user's default browser or chooser.
   * Gracefully falls back to copying the link to clipboard if no browser can handle the intent.
   */
  fun openDonationCheckout(context: Context) {
    try {
      val viewIntent = buildDonationCheckoutIntent(newTask = true)
      val resolved = viewIntent.resolveActivity(context.packageManager)
      if (resolved != null) {
        context.startActivity(viewIntent)
      } else {
        val chooser = Intent.createChooser(viewIntent, "Open Sadaqah Donation Portal").apply {
          flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(chooser)
      }
    } catch (_: Exception) {
      copyDonationLink(context)
      Toast.makeText(
        context,
        "No browser app found. Donation link copied to clipboard!",
        Toast.LENGTH_LONG
      ).show()
    }
  }

  /**
   * Copies the Flutterwave donation URL to the clipboard for sharing or external use.
   */
  fun copyDonationLink(context: Context) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    val clip = ClipData.newPlainText("Noor Flutterwave Donation Link", FLUTTERWAVE_DONATION_URL)
    clipboard?.setPrimaryClip(clip)
    Toast.makeText(
      context,
      "Donation link copied to clipboard! ✓ Jazakallahu Khayran",
      Toast.LENGTH_SHORT
    ).show()
  }
}

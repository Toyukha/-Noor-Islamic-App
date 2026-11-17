package com.example.data.config

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DonationConfigTest {

  @Test
  fun `flutterwave donation url and host match expected verified link`() {
    assertEquals(
      "https://flutterwave.com/donate/udnd7pfu6sbt",
      DonationConfig.FLUTTERWAVE_DONATION_URL
    )
    assertEquals(
      "flutterwave.com/donate/udnd7pfu6sbt",
      DonationConfig.FLUTTERWAVE_DISPLAY_HOST
    )
    assertTrue(DonationConfig.hasValidDonationUrl())
  }

  @Test
  fun `buildDonationCheckoutIntent creates browsable https view intent`() {
    val newTaskIntent = DonationConfig.buildDonationCheckoutIntent(newTask = true)
    assertEquals(Intent.ACTION_VIEW, newTaskIntent.action)
    assertEquals(Uri.parse(DonationConfig.FLUTTERWAVE_DONATION_URL), newTaskIntent.data)
    assertTrue(newTaskIntent.hasCategory(Intent.CATEGORY_BROWSABLE))
    assertTrue((newTaskIntent.flags and Intent.FLAG_ACTIVITY_NEW_TASK) != 0)

    val sameTaskIntent = DonationConfig.buildDonationCheckoutIntent(newTask = false)
    assertEquals(Intent.ACTION_VIEW, sameTaskIntent.action)
    assertFalse((sameTaskIntent.flags and Intent.FLAG_ACTIVITY_NEW_TASK) != 0)
  }

  @Test
  fun `copyDonationLink writes flutterwave url to system clipboard`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    DonationConfig.copyDonationLink(context)

    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val primaryClip = clipboard.primaryClip
    assertNotNull(primaryClip)
    assertEquals(1, primaryClip!!.itemCount)
    assertEquals(DonationConfig.FLUTTERWAVE_DONATION_URL, primaryClip.getItemAt(0).text.toString())
  }
}

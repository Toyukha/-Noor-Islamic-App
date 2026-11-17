package com.example.data.config

import android.content.Intent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PrivacyPolicyConfigTest {

  @Test
  fun `privacy policy identity matches Noor Play Store package and developer email`() {
    assertEquals("Noor", PrivacyPolicyConfig.APP_NAME)
    assertEquals("com.aistudio.noorislamic.zpqtwv", PrivacyPolicyConfig.PACKAGE_ID)
    assertEquals("oyeyemiyunus24@gmail.com", PrivacyPolicyConfig.DEVELOPER_CONTACT_EMAIL)
    assertTrue(PrivacyPolicyConfig.EFFECTIVE_DATE.isNotBlank())
  }

  @Test
  fun `privacy policy sections cover all required permissions and third-party disclosures`() {
    val allText = PrivacyPolicyConfig.sections.joinToString("\n") { section ->
      section.title + "\n" + section.summary + "\n" + section.bullets.joinToString("\n")
    }

    assertTrue(allText.contains("ad-free", ignoreCase = true))
    assertTrue(allText.contains("Firebase", ignoreCase = true))
    assertTrue(allText.contains("Location", ignoreCase = true))
    assertTrue(allText.contains("SCHEDULE_EXACT_ALARM", ignoreCase = false))
    assertTrue(allText.contains("SYSTEM_ALERT_WINDOW", ignoreCase = false))
    assertTrue(allText.contains("Flutterwave", ignoreCase = true))
    assertTrue(allText.contains(PrivacyPolicyConfig.DEVELOPER_CONTACT_EMAIL))
  }

  @Test
  fun `hosted URL validator rejects empty string and accepts valid GitHub Pages HTTPS URL`() {
    assertFalse(PrivacyPolicyConfig.hasHostedUrl(""))
    assertFalse(PrivacyPolicyConfig.hasHostedUrl("http://example.com/privacy.html"))
    assertNull(PrivacyPolicyConfig.buildOpenPolicyIntent(url = ""))

    val githubPagesUrl = "https://oyeyemiyunus.github.io/noor/privacy-policy.html"
    assertTrue(PrivacyPolicyConfig.hasHostedUrl(githubPagesUrl))
    val intent = PrivacyPolicyConfig.buildOpenPolicyIntent(url = githubPagesUrl)
    assertNotNull(intent)
    assertEquals(Intent.ACTION_VIEW, intent?.action)
    assertEquals(githubPagesUrl, intent?.dataString)
  }

  @Test
  fun `data deletion email intent targets developer contact via mailto`() {
    val intent = PrivacyPolicyConfig.buildDataDeletionEmailIntent()
    assertEquals(Intent.ACTION_SENDTO, intent.action)
    val uriString = intent.dataString ?: ""
    assertTrue(uriString.startsWith("mailto:${PrivacyPolicyConfig.DEVELOPER_CONTACT_EMAIL}"))
    assertTrue(uriString.contains("Deletion"))
  }
}

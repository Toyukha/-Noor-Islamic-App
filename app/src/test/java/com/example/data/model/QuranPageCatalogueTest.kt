package com.example.data.model

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.notification.AlarmPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class QuranPageCatalogueTest {

  @Test
  fun `loads complete 604 page authentic uthmani quran dataset offline`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    QuranPageCatalogue.initialize(context)

    assertTrue(QuranPageCatalogue.isFullDatasetLoaded())

    val distinctFirstAyahs = HashSet<String>()
    for (pageNum in 1..604) {
      val page = QuranPageCatalogue.getPage(pageNum, context)
      assertEquals(pageNum, page.pageNumber)
      assertTrue("Page $pageNum must contain ayahs", page.ayahs.isNotEmpty())
      distinctFirstAyahs.add(page.ayahs.first().textArabic)
    }

    // Previously 596 pages repeated the exact same dummy text; all 604 pages should be unique
    assertTrue("Expected unique content across all 604 pages", distinctFirstAyahs.size > 590)

    val p5 = QuranPageCatalogue.getPage(5, context)
    val p6 = QuranPageCatalogue.getPage(6, context)
    val p50 = QuranPageCatalogue.getPage(50, context)
    assertNotEquals(p5.ayahs, p6.ayahs)
    assertNotEquals(p5.ayahs, p50.ayahs)
  }

  @Test
  fun `resolves exact mushaf page numbers for surah and ayah lookups`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    QuranPageCatalogue.initialize(context)

    assertEquals(1, QuranPageCatalogue.findPageForAyah(1, 1, context))
    assertEquals(42, QuranPageCatalogue.findPageForAyah(2, 255, context))
    assertEquals(293, QuranPageCatalogue.findPageForAyah(18, 1, context))
    assertEquals(562, QuranPageCatalogue.findPageForAyah(67, 1, context))
    assertEquals(604, QuranPageCatalogue.findPageForAyah(112, 1, context))
  }

  @Test
  fun `respects mushaf bismillah rules for at tawbah and multi surah pages`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    QuranPageCatalogue.initialize(context)

    val atTawbahPage = QuranPageCatalogue.getPage(187, context)
    assertEquals(9, atTawbahPage.surahNumber)
    assertFalse("Surah At-Tawbah must not display Bismillah", atTawbahPage.hasBismillah)

    val lastPage = QuranPageCatalogue.getPage(604, context)
    val startingSurahs = lastPage.ayahs.filter { it.isSurahStart }.map { it.surahNumber }
    assertEquals(listOf(112, 113, 114), startingSurahs)
  }

  @Test
  fun `persists quran reader night mode and clamped font size preferences`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    AlarmPreferences.prefs(context).edit().clear().commit()

    assertFalse(AlarmPreferences.isQuranNightMode(context))
    AlarmPreferences.setQuranNightMode(context, true)
    assertTrue(AlarmPreferences.isQuranNightMode(context))

    assertEquals(22, AlarmPreferences.getQuranFontSizeSp(context))
    AlarmPreferences.setQuranFontSizeSp(context, 40)
    assertEquals(30, AlarmPreferences.getQuranFontSizeSp(context))
    AlarmPreferences.setQuranFontSizeSp(context, 10)
    assertEquals(18, AlarmPreferences.getQuranFontSizeSp(context))
  }
}

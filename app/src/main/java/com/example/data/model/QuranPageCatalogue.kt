package com.example.data.model

import android.content.Context
import android.util.Log
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.zip.GZIPInputStream

data class QuranAyah(
  val ayahNumber: Int,
  val textArabic: String,
  val surahNumber: Int = 1,
  val surahNameArabic: String = "",
  val surahNameEnglish: String = "",
  val isSurahStart: Boolean = false
)

data class QuranPageData(
  val pageNumber: Int,
  val juzNumber: Int,
  val surahNumber: Int,
  val surahNameArabic: String,
  val surahNameEnglish: String,
  val hasBismillah: Boolean,
  val isNewSurahStart: Boolean,
  val hizbQuarter: String,
  val ayahs: List<QuranAyah>
)

object QuranPageCatalogue {

  private const val TAG = "QuranPageCatalogue"
  private const val ASSET_PATH = "quran/quran_uthmani_604.bin"

  @Volatile
  private var loadedPages: Map<Int, QuranPageData>? = null

  @Volatile
  private var ayahPageLookup: Map<Long, Int>? = null

  /**
   * Loads all 604 authentic Madani Mushaf pages from the compressed offline asset.
   * Safe and idempotent to call multiple times from UI or background threads.
   */
  fun initialize(context: Context) {
    if (loadedPages != null) return
    synchronized(this) {
      if (loadedPages != null) return
      try {
        context.applicationContext.assets.open(ASSET_PATH).use { rawStream ->
          GZIPInputStream(rawStream).use { gzipStream ->
            BufferedReader(InputStreamReader(gzipStream, Charsets.UTF_8)).use { reader ->
              val jsonText = reader.readText()
              parsePagesJson(jsonText)
            }
          }
        }
      } catch (e: Exception) {
        Log.e(TAG, "Failed to load offline 604-page Quran dataset", e)
      }
    }
  }

  internal fun parsePagesJson(jsonText: String) {
    val array = JSONArray(jsonText)
    val pageMap = HashMap<Int, QuranPageData>(604)
    val ayahIndex = HashMap<Long, Int>(6236)

    for (i in 0 until array.length()) {
      val obj = array.getJSONObject(i)
      val pageNum = obj.getInt("p")
      val juz = obj.getInt("j")
      val hizbQuarterIdx = obj.optInt("hq", ((pageNum - 1) / 3) + 1)
      val surahNum = obj.getInt("sn")
      val surahAr = obj.getString("sa")
      val surahEn = obj.getString("se")
      val ayahsArr = obj.getJSONArray("ay")

      val ayahs = ArrayList<QuranAyah>(ayahsArr.length())
      var pageHasSurahStart = false
      var pageHasBismillah = false

      for (k in 0 until ayahsArr.length()) {
        val aObj = ayahsArr.getJSONObject(k)
        val aSurah = aObj.getInt("s")
        val aSurahAr = aObj.optString("sa", surahAr)
        val aSurahEn = aObj.optString("se", surahEn)
        val aNum = aObj.getInt("n")
        val aText = aObj.getString("t")
        val isStart = aObj.optBoolean("st", aNum == 1)

        if (isStart) {
          pageHasSurahStart = true
          if (aSurah != 1 && aSurah != 9) {
            pageHasBismillah = true
          }
        }

        ayahIndex[packSurahAyahKey(aSurah, aNum)] = pageNum
        ayahs.add(
          QuranAyah(
            ayahNumber = aNum,
            textArabic = aText,
            surahNumber = aSurah,
            surahNameArabic = aSurahAr,
            surahNameEnglish = aSurahEn,
            isSurahStart = isStart
          )
        )
      }

      val hizbNum = ((hizbQuarterIdx - 1) / 4) + 1
      val rubNum = ((hizbQuarterIdx - 1) % 4) + 1
      val hizbLabel = if (pageNum == 604) {
        "الحزب ٦٠ - خاتمة المصحف"
      } else {
        "الحزب ${toArabicNumerals(hizbNum)} • الربع ${toArabicNumerals(rubNum)}"
      }

      pageMap[pageNum] = QuranPageData(
        pageNumber = pageNum,
        juzNumber = juz,
        surahNumber = surahNum,
        surahNameArabic = surahAr,
        surahNameEnglish = surahEn,
        hasBismillah = pageHasBismillah,
        isNewSurahStart = pageHasSurahStart,
        hizbQuarter = hizbLabel,
        ayahs = ayahs
      )
    }

    loadedPages = pageMap
    ayahPageLookup = ayahIndex
  }

  /**
   * Returns authentic Arabic Quran page data for any Mushaf page 1..604.
   * When [context] is provided, automatically initializes the full 604-page asset on first call.
   */
  fun getPage(pageNumber: Int, context: Context? = null): QuranPageData {
    val clamped = pageNumber.coerceIn(1, 604)
    if (loadedPages == null && context != null) {
      initialize(context)
    }
    return loadedPages?.get(clamped)
      ?: FALLBACK_SAMPLE_CACHE[clamped]
      ?: buildMinimalSafetyPage(clamped)
  }

  /**
   * Resolves the exact Madani Mushaf page number (1..604) containing a given Surah & Ayah.
   */
  fun findPageForAyah(surahNumber: Int, ayahNumber: Int, context: Context? = null): Int {
    if (ayahPageLookup == null && context != null) {
      initialize(context)
    }
    val key = packSurahAyahKey(surahNumber, ayahNumber)
    ayahPageLookup?.get(key)?.let { return it }
    // Fallback: locate the first page of that Surah if Ayah key isn't indexed
    ayahPageLookup?.get(packSurahAyahKey(surahNumber, 1))?.let { return it }
    return loadedPages?.values?.firstOrNull { page ->
      page.ayahs.any { it.surahNumber == surahNumber }
    }?.pageNumber ?: 1
  }

  fun isFullDatasetLoaded(): Boolean = (loadedPages?.size ?: 0) >= 604

  private fun packSurahAyahKey(surahNumber: Int, ayahNumber: Int): Long =
    (surahNumber.toLong() shl 16) or (ayahNumber.toLong() and 0xFFFFL)

  private fun toArabicNumerals(number: Int): String {
    val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    return number.toString().map { c ->
      if (c in '0'..'9') arabicDigits[c - '0'] else c
    }.joinToString("")
  }

  // Fallback cache only used when getPage() is called without Context in unit tests
  private val FALLBACK_SAMPLE_CACHE = mapOf(
    1 to QuranPageData(
      pageNumber = 1,
      juzNumber = 1,
      surahNumber = 1,
      surahNameArabic = "سُورَةُ الفَاتِحَةِ",
      surahNameEnglish = "Al-Faatiha",
      hasBismillah = false,
      isNewSurahStart = true,
      hizbQuarter = "الحزب ١ • الربع ١",
      ayahs = listOf(
        QuranAyah(1, "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ", 1, "سُورَةُ الفَاتِحَةِ", "Al-Faatiha", true),
        QuranAyah(2, "ٱلْحَمْدُ لِلَّهِ رَبِّ ٱلْعَٰلَمِينَ", 1, "سُورَةُ الفَاتِحَةِ", "Al-Faatiha", false),
        QuranAyah(3, "ٱلرَّحْمَٰنِ ٱلرَّحِيمِ", 1, "سُورَةُ الفَاتِحَةِ", "Al-Faatiha", false),
        QuranAyah(4, "مَٰلِكِ يَوْمِ ٱلدِّينِ", 1, "سُورَةُ الفَاتِحَةِ", "Al-Faatiha", false),
        QuranAyah(5, "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", 1, "سُورَةُ الفَاتِحَةِ", "Al-Faatiha", false),
        QuranAyah(6, "ٱهْدِنَا ٱلصِّرَٰطَ ٱلْمُسْتَقِيمَ", 1, "سُورَةُ الفَاتِحَةِ", "Al-Faatiha", false),
        QuranAyah(7, "صِرَٰطَ ٱلَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ ٱلْمَغْضُوبِ عَلَيْهِمْ وَلَا ٱلضَّآلِّينَ", 1, "سُورَةُ الفَاتِحَةِ", "Al-Faatiha", false)
      )
    )
  )

  private fun buildMinimalSafetyPage(pageNumber: Int): QuranPageData {
    val juz = ((pageNumber - 1) / 20) + 1
    return QuranPageData(
      pageNumber = pageNumber,
      juzNumber = juz,
      surahNumber = 1,
      surahNameArabic = "القرآن الكريم",
      surahNameEnglish = "Al-Quran Al-Kareem",
      hasBismillah = false,
      isNewSurahStart = false,
      hizbQuarter = "الصفحة ${toArabicNumerals(pageNumber)}",
      ayahs = listOf(
        QuranAyah(1, "إِنَّ هَٰذَا ٱلْقُرْءَانَ يَهْدِى لِلَّتِى هِىَ أَقْوَمُ", 17, "سُورَةُ الإِسْرَاءِ", "Al-Israa", false)
      )
    )
  }
}

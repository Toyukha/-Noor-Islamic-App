package com.example.ai

import android.content.Context
import android.util.Log
import com.example.data.local.NoorDatabase
import com.example.data.local.VerseReflectionEntity
import com.example.data.model.DailyVerse
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "VerseReflectionService"

sealed interface ReflectionUiState {
  data object Idle : ReflectionUiState
  data object Loading : ReflectionUiState
  data class Success(val reflection: VerseReflectionEntity, val isFromLocalCache: Boolean) : ReflectionUiState
  data class Error(val message: String) : ReflectionUiState
}

class VerseReflectionService(private val context: Context) {

  private val localDao by lazy {
    NoorDatabase.getDatabase(context).verseReflectionDao()
  }

  // Define structured JSON schema using Schema.obj per Firebase AI Logic guidelines
  private val reflectionSchema = Schema.obj(
    properties = mapOf(
      "spiritualTheme" to Schema.string(description = "Short core spiritual theme of the verse, e.g. Divine Nearness, Steadfastness in Trials, Abundant Mercy"),
      "contemplationNarrative" to Schema.string(description = "A warm, profound, and eloquent spiritual commentary exploring the deeper wisdom and solace of this verse"),
      "dailyActionSteps" to Schema.string(description = "2-3 practical, tangible actions a believer can embody in daily life today"),
      "supplicationDua" to Schema.string(description = "A heartfelt supplication (Du'a) in English inspired by the teachings of this verse")
    )
  )

  private val config = generationConfig {
    responseMimeType = "application/json"
    responseSchema = reflectionSchema
  }

  // Model initialized lazily to avoid premature initialization before App Check
  private val model by lazy {
    Firebase.ai.generativeModel(
      modelName = "gemini-3.8-flash",
      generationConfig = config
    )
  }

  /**
   * Observes local reflection from Room for the given date.
   */
  fun observeLocalReflection(dateStr: String): Flow<VerseReflectionEntity?> {
    return localDao.getReflectionForDate(dateStr)
  }

  /**
   * Generates a profound spiritual reflection for the given Quranic verse using Gemini API
   * and caches the result locally in Room SQLite.
   */
  suspend fun fetchReflection(
    dateStr: String,
    verse: DailyVerse,
    forceRefresh: Boolean = false
  ): ReflectionUiState = withContext(Dispatchers.IO) {
    // 1. Check local Room cache first unless forceRefresh is requested
    if (!forceRefresh) {
      val cached = localDao.getReflectionForDateDirect(dateStr)
      if (cached != null) {
        return@withContext ReflectionUiState.Success(cached, isFromLocalCache = true)
      }
    }

    try {
      val prompt = """
        You are an expert, compassionate Islamic scholar and spiritual counselor. Provide a profound spiritual reflection (Tadabbur) on the following Quranic verse for a daily devotional app:

        Surah: ${verse.surahName} (${verse.surahNumber}:${verse.ayahNumber})
        Arabic: ${verse.arabicText}
        Translation: "${verse.englishTranslation}"

        Craft:
        1. A concise, inspiring Spiritual Theme (2-5 words).
        2. A contemplation narrative that brings peace, hope, and clarity to the reader's heart.
        3. Practical daily action steps to live this verse today.
        4. A sincere supplication (Du'a) starting with 'O Allah...'
      """.trimIndent()

      // Mandated: realistic 30-60s timeout for model inference
      val response = withTimeout(60_000L) {
        model.generateContent(prompt)
      }

      val jsonString = response.text
      if (jsonString.isNullOrBlank()) {
        return@withContext ReflectionUiState.Error("Gemini returned an empty response. Please try again.")
      }

      // Parse structured JSON response directly
      val json = JSONObject(jsonString)
      val spiritualTheme = json.optString("spiritualTheme", "Divine Guidance")
      val contemplationNarrative = json.optString("contemplationNarrative", verse.reflectionPrompt)
      val dailyActionSteps = json.optString("dailyActionSteps", "Reflect on this ayah during your next prayer.")
      val supplicationDua = json.optString("supplicationDua", "O Allah, guide our hearts to Your light and keep us steadfast.")

      val entity = VerseReflectionEntity(
        date = dateStr,
        surahName = verse.surahName,
        surahNumber = verse.surahNumber,
        ayahNumber = verse.ayahNumber,
        arabicText = verse.arabicText,
        englishTranslation = verse.englishTranslation,
        spiritualTheme = spiritualTheme,
        contemplationNarrative = contemplationNarrative,
        dailyActionSteps = dailyActionSteps,
        supplicationDua = supplicationDua,
        generatedAt = System.currentTimeMillis()
      )

      // Save locally in Room for offline access
      localDao.insertOrUpdate(entity)
      Log.d(TAG, "Reflection for $dateStr saved to local Room database.")

      ReflectionUiState.Success(entity, isFromLocalCache = false)
    } catch (e: TimeoutCancellationException) {
      Log.e(TAG, "Gemini reflection generation timed out", e)
      // Check if we have cached fallback
      val cached = localDao.getReflectionForDateDirect(dateStr)
      if (cached != null) {
        ReflectionUiState.Success(cached, isFromLocalCache = true)
      } else {
        ReflectionUiState.Error("Reflection request timed out. Please check your connection and try again.")
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Log.e(TAG, "Firebase AI reflection generation failed", e)
      // Check if we have cached fallback
      val cached = localDao.getReflectionForDateDirect(dateStr)
      if (cached != null) {
        ReflectionUiState.Success(cached, isFromLocalCache = true)
      } else {
        ReflectionUiState.Error(e.localizedMessage ?: "Failed to generate AI reflection.")
      }
    }
  }
}

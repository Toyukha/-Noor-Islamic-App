package com.example.data.export

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.DailyLog
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportExportManager {

  private const val TAG = "ReportExportManager"

  /**
   * Generates a CSV file containing all spiritual obligation records.
   */
  fun generateCsvReport(context: Context, logs: List<DailyLog>): File {
    val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val file = File(exportDir, "Noor_Tracker_Report_$timestamp.csv")

    val header = "Date,Fajr,Dhuhr,Asr,Maghrib,Isha,Completed_Prayers,Tahajjud,Duha,Witr,Rawatib_Count,Sunnah_Prayers,Fasting,Fasting_Type,Quran_Pages,Quran_Minutes,Last_Surah,Last_Ayah\n"

    val sortedLogs = logs.sortedByDescending { it.date }
    val sb = StringBuilder(header)

    for (log in sortedLogs) {
      val f = if (log.fajr) "Yes" else "No"
      val d = if (log.dhuhr) "Yes" else "No"
      val a = if (log.asr) "Yes" else "No"
      val m = if (log.maghrib) "Yes" else "No"
      val i = if (log.isha) "Yes" else "No"
      val tah = if (log.tahajjud) "Yes" else "No"
      val duh = if (log.duha) "Yes" else "No"
      val wit = if (log.witr) "Yes" else "No"
      val fast = if (log.isFasting) "Yes" else "No"

      sb.append(
        "${log.date},$f,$d,$a,$m,$i,${log.completedPrayersCount},$tah,$duh,$wit,${log.rawatibCount},${log.sunnahPrayers},$fast,${log.fastingType},${log.quranPagesRead},${log.quranMinutes},\"${log.lastSurah}\",${log.lastAyah}\n"
      )
    }

    file.writeText(sb.toString())
    return file
  }

  /**
   * Generates a clean, print-ready PDF document summarizing the user's tracker compliance.
   */
  fun generatePdfReport(
    context: Context,
    logs: List<DailyLog>,
    userCity: String = "Mecca",
    userName: String = "Muslim Servant"
  ): File {
    val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val file = File(exportDir, "Noor_Tracker_Report_$timestamp.pdf")

    val document = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 dimensions
    val page = document.startPage(pageInfo)
    val canvas = page.canvas

    val paint = Paint().apply { isAntiAlias = true }

    // 1. Header Banner
    paint.color = Color.parseColor("#0C3823") // Deep Emerald
    canvas.drawRect(0f, 0f, 595f, 100f, paint)

    paint.color = Color.parseColor("#D4AF37") // Radiant Gold
    paint.textSize = 22f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("NOOR • SPIRITUAL OBLIGATIONS REPORT", 30f, 45f, paint)

    paint.color = Color.WHITE
    paint.textSize = 11f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    val dateStr = SimpleDateFormat("EEEE, dd MMMM yyyy • HH:mm", Locale.US).format(Date())
    canvas.drawText("Generated: $dateStr  |  User: $userName ($userCity)", 30f, 72f, paint)

    // 2. Summary Statistics Card
    val totalPrayers = logs.sumOf { it.completedPrayersCount }
    val maxPossiblePrayers = (logs.size * 5).coerceAtLeast(1)
    val prayerRate = ((totalPrayers.toDouble() / maxPossiblePrayers) * 100).toInt()
    val totalFastingDays = logs.count { it.isFasting }
    val totalQuranPages = logs.sumOf { it.quranPagesRead }
    val totalTahajjudDays = logs.count { it.tahajjud }

    paint.color = Color.parseColor("#F5F7F5")
    canvas.drawRoundRect(30f, 120f, 565f, 210f, 14f, 14f, paint)

    paint.color = Color.parseColor("#0C3823")
    paint.textSize = 13f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("ACTIVITY SUMMARY (${logs.size} DAYS LOGGED)", 50f, 145f, paint)

    paint.textSize = 11f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    paint.color = Color.DKGRAY
    canvas.drawText("• Solah Compliance: $totalPrayers / $maxPossiblePrayers prayers completed ($prayerRate%)", 50f, 170f, paint)
    canvas.drawText("• Fasting (Sawm): $totalFastingDays days logged  |  Tahajjud: $totalTahajjudDays nights performed", 50f, 188f, paint)
    canvas.drawText("• Quran Tilawah: $totalQuranPages pages recited", 50f, 204f, paint)

    // 3. Table Header
    val startY = 240f
    paint.color = Color.parseColor("#0C3823")
    canvas.drawRect(30f, startY, 565f, startY + 24f, paint)

    paint.color = Color.WHITE
    paint.textSize = 10f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("Date", 40f, startY + 16f, paint)
    canvas.drawText("Fajr", 120f, startY + 16f, paint)
    canvas.drawText("Dhuhr", 160f, startY + 16f, paint)
    canvas.drawText("Asr", 205f, startY + 16f, paint)
    canvas.drawText("Maghrib", 245f, startY + 16f, paint)
    canvas.drawText("Isha", 300f, startY + 16f, paint)
    canvas.drawText("Nawafil", 345f, startY + 16f, paint)
    canvas.drawText("Fasting", 420f, startY + 16f, paint)
    canvas.drawText("Quran Pages", 485f, startY + 16f, paint)

    // 4. Table Rows
    var currentY = startY + 24f
    val rowHeight = 20f
    val displayLogs = logs.sortedByDescending { it.date }.take(22)

    val checkSymbol = "✓"
    val crossSymbol = "—"

    for ((index, log) in displayLogs.withIndex()) {
      if (index % 2 == 1) {
        paint.color = Color.parseColor("#F9F9F9")
        canvas.drawRect(30f, currentY, 565f, currentY + rowHeight, paint)
      }

      paint.color = Color.BLACK
      paint.textSize = 9.5f
      paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

      canvas.drawText(log.date, 40f, currentY + 14f, paint)

      fun drawCheck(completed: Boolean, x: Float) {
        paint.color = if (completed) Color.parseColor("#1B5E20") else Color.LTGRAY
        canvas.drawText(if (completed) checkSymbol else crossSymbol, x, currentY + 14f, paint)
      }

      drawCheck(log.fajr, 125f)
      drawCheck(log.dhuhr, 168f)
      drawCheck(log.asr, 210f)
      drawCheck(log.maghrib, 258f)
      drawCheck(log.isha, 308f)

      paint.color = Color.DKGRAY
      val nawafilSummary = "${if (log.tahajjud) "Tahajjud " else ""}${if (log.witr) "Witr " else ""}${log.rawatibCount}R".trim()
      canvas.drawText(if (nawafilSummary.isBlank()) "—" else nawafilSummary, 345f, currentY + 14f, paint)

      canvas.drawText(if (log.isFasting) log.fastingType else "—", 420f, currentY + 14f, paint)
      canvas.drawText("${log.quranPagesRead} pgs", 495f, currentY + 14f, paint)

      currentY += rowHeight
    }

    // Footer
    paint.color = Color.LTGRAY
    canvas.drawLine(30f, 790f, 565f, 790f, paint)
    paint.textSize = 9f
    paint.color = Color.GRAY
    canvas.drawText("Noor Islamic Companion • Powered by Pure Sunnah & Islamic Devotion • May Allah accept your good deeds", 30f, 810f, paint)

    document.finishPage(page)

    FileOutputStream(file).use { out ->
      document.writeTo(out)
    }
    document.close()

    return file
  }

  /**
   * Shares the generated report file via Android Intent Chooser.
   */
  fun shareReportFile(context: Context, file: File, mimeType: String = "text/csv") {
    try {
      val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
      )

      val intent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "Noor Spiritual Tracker Report")
        putExtra(Intent.EXTRA_TEXT, "Here is my spiritual tracker report from Noor Islamic Companion.")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }

      val chooser = Intent.createChooser(intent, "Export Spiritual Report")
      chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(chooser)
    } catch (e: Exception) {
      Log.e(TAG, "Error sharing report file", e)
      Toast.makeText(context, "Could not open share dialog: ${e.message}", Toast.LENGTH_LONG).show()
    }
  }

  /**
   * Saves/copies file to the public Downloads folder for offline access using
   * MediaStore on Android 10+ (Scoped Storage) and direct file copy on older OS versions.
   */
  fun saveToDownloads(context: Context, file: File): Boolean {
    return try {
      val mimeType = if (file.name.endsWith(".pdf", ignoreCase = true)) {
        "application/pdf"
      } else {
        "text/csv"
      }

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val values = ContentValues().apply {
          put(MediaStore.Downloads.DISPLAY_NAME, file.name)
          put(MediaStore.Downloads.MIME_TYPE, mimeType)
          put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Noor")
          put(MediaStore.Downloads.IS_PENDING, 1)
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
          ?: throw IllegalStateException("Could not create Downloads MediaStore entry")

        resolver.openOutputStream(uri)?.use { out ->
          file.inputStream().use { input ->
            input.copyTo(out)
          }
        } ?: throw IllegalStateException("Could not open Downloads output stream")

        values.clear()
        values.put(MediaStore.Downloads.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
      } else {
        @Suppress("DEPRECATION")
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) downloadsDir.mkdirs()
        val destination = File(downloadsDir, file.name)
        file.copyTo(destination, overwrite = true)
      }

      Toast.makeText(context, "Report saved to Downloads:\n${file.name}", Toast.LENGTH_LONG).show()
      true
    } catch (e: Exception) {
      Log.e(TAG, "Failed saving to downloads", e)
      Toast.makeText(context, "Saved in app cache: ${file.name}", Toast.LENGTH_SHORT).show()
      false
    }
  }
}

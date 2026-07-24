package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.StudySession
import com.example.data.model.Subject
import com.example.data.model.UserProfile
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfExporter {

    enum class ReportPeriod(val title: String, val days: Int) {
        WEEKLY("Weekly Progress Report (Last 7 Days)", 7),
        MONTHLY("Monthly Progress Report (Last 30 Days)", 30)
    }

    fun generateAndSharePdfReport(
        context: Context,
        period: ReportPeriod,
        userProfile: UserProfile?,
        sessions: List<StudySession>,
        subjects: List<Subject>
    ): File? {
        val pdfDocument = PdfDocument()

        // Standard A4 Page dimensions at 72 DPI (595 x 842 pt)
        val pageWidth = 595
        val pageHeight = 842
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }

        // Color Palette
        val primaryColor = Color.parseColor("#3F51B5") // Deep Indigo
        val primaryContainerColor = Color.parseColor("#E8EAF6")
        val textColorPrimary = Color.parseColor("#1A1C1E")
        val textColorSecondary = Color.parseColor("#5C5E62")
        val cardBgColor = Color.parseColor("#F5F6FA")
        val dividerColor = Color.parseColor("#E0E0E0")
        val accentGreen = Color.parseColor("#2E7D32")

        val calendar = Calendar.getInstance()
        val nowMs = System.currentTimeMillis()
        val startPeriodMs = nowMs - (period.days * 86400000L)

        val periodSessions = sessions.filter { it.timestamp >= startPeriodMs }
        val totalSecs = periodSessions.sumOf { it.durationSeconds }
        val totalHours = totalSecs / 3600f
        val sessionCount = periodSessions.size
        val avgDailyHours = totalHours / period.days.toFloat()

        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val generatedDateStr = dateFormat.format(Date(nowMs))
        val startDateStr = dateFormat.format(Date(startPeriodMs))

        // 1. TOP HEADER BANNER
        paint.color = primaryColor
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 90f, paint)

        // Header Title
        paint.color = Color.WHITE
        paint.textSize = 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("ACADEMIC STUDY PROGRESS REPORT", 28f, 42f, paint)

        // Header Subtitle
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Generated on $generatedDateStr • Study Tracker", 28f, 65f, paint)

        var currentY = 115f

        // 2. STUDENT PROFILE CARD
        paint.color = cardBgColor
        val profileRect = RectF(28f, currentY, pageWidth - 28f, currentY + 95f)
        canvas.drawRoundRect(profileRect, 12f, 12f, paint)

        // Student Info
        val studentName = userProfile?.name?.ifBlank { "Alex Scholar" } ?: "Alex Scholar"
        val academicLevel = userProfile?.academicLevel?.ifBlank { "Student Scholar" } ?: "Student Scholar"
        val motto = userProfile?.motto?.ifBlank { "Building consistent daily habits." } ?: "Building consistent daily habits."

        paint.color = primaryColor
        paint.textSize = 16f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(studentName, 44f, currentY + 30f, paint)

        paint.color = textColorSecondary
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("$academicLevel • Target: ${userProfile?.targetDailyHours ?: 3.5f} hrs/day", 44f, currentY + 50f, paint)

        paint.color = textColorPrimary
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        canvas.drawText("\"$motto\"", 44f, currentY + 72f, paint)

        currentY += 115f

        // 3. PERIOD SUMMARY CARDS (3 Stat Boxes)
        paint.color = textColorPrimary
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(period.title, 28f, currentY, paint)

        paint.color = textColorSecondary
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Period: $startDateStr to $generatedDateStr", 28f, currentY + 16f, paint)

        currentY += 28f

        val statBoxWidth = (pageWidth - 56f - 24f) / 3f
        val statBoxHeight = 65f

        // Box 1: Total Study Hours
        drawStatBox(canvas, paint, 28f, currentY, statBoxWidth, statBoxHeight, "TOTAL HOURS", String.format("%.1f hrs", totalHours), primaryContainerColor, primaryColor)

        // Box 2: Total Sessions
        drawStatBox(canvas, paint, 28f + statBoxWidth + 12f, currentY, statBoxWidth, statBoxHeight, "SESSIONS", "$sessionCount completed", cardBgColor, textColorPrimary)

        // Box 3: Daily Average
        drawStatBox(canvas, paint, 28f + (statBoxWidth + 12f) * 2, currentY, statBoxWidth, statBoxHeight, "DAILY AVG", String.format("%.1f hrs/day", avgDailyHours), cardBgColor, accentGreen)

        currentY += statBoxHeight + 28f

        // 4. SUBJECT-WISE BREAKDOWN TABLE
        paint.color = textColorPrimary
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Subject Breakdown", 28f, currentY, paint)

        currentY += 14f

        // Table Header
        paint.color = primaryContainerColor
        val tableHeaderRect = RectF(28f, currentY, pageWidth - 28f, currentY + 24f)
        canvas.drawRoundRect(tableHeaderRect, 6f, 6f, paint)

        paint.color = primaryColor
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("SUBJECT", 40f, currentY + 16f, paint)
        canvas.drawText("SESSIONS", 260f, currentY + 16f, paint)
        canvas.drawText("TIME STUDIED", 370f, currentY + 16f, paint)
        canvas.drawText("SHARE", 480f, currentY + 16f, paint)

        currentY += 30f

        val overallSecs = periodSessions.sumOf { it.durationSeconds }

        subjects.forEach { subject ->
            val subSessions = periodSessions.filter { it.subjectId == subject.id }
            val subSecs = subSessions.sumOf { it.durationSeconds }
            val subHours = subSecs / 3600f
            val pct = if (overallSecs > 0) (subSecs.toFloat() / overallSecs.toFloat()) * 100f else 0f

            // Draw row line
            paint.color = cardBgColor
            val rowRect = RectF(28f, currentY - 12f, pageWidth - 28f, currentY + 14f)
            canvas.drawRoundRect(rowRect, 4f, 4f, paint)

            // Subject Indicator dot
            try {
                paint.color = subject.colorHex.toInt()
            } catch (e: Exception) {
                paint.color = primaryColor
            }
            canvas.drawCircle(42f, currentY, 5f, paint)

            paint.color = textColorPrimary
            paint.textSize = 11f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(subject.name, 56f, currentY + 4f, paint)

            paint.color = textColorSecondary
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("${subSessions.size} sessions", 260f, currentY + 4f, paint)
            canvas.drawText(String.format("%.1f hrs", subHours), 370f, currentY + 4f, paint)
            canvas.drawText(String.format("%.1f%%", pct), 480f, currentY + 4f, paint)

            currentY += 30f
        }

        currentY += 15f

        // 5. RECENT STUDY LOGS TABLE (up to 6 recent items)
        if (currentY < pageHeight - 180f) {
            paint.color = textColorPrimary
            paint.textSize = 14f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Recent Activity Log", 28f, currentY, paint)

            currentY += 14f

            val recentSessions = periodSessions.sortedByDescending { it.timestamp }.take(6)
            if (recentSessions.isEmpty()) {
                paint.color = textColorSecondary
                paint.textSize = 10f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                canvas.drawText("No sessions recorded during this period.", 28f, currentY + 16f, paint)
                currentY += 30f
            } else {
                recentSessions.forEach { sess ->
                    val subName = subjects.find { it.id == sess.subjectId }?.name ?: "General"
                    val sessDateStr = dateFormat.format(Date(sess.timestamp)) + " " + timeFormat.format(Date(sess.timestamp))
                    val mins = sess.durationSeconds / 60

                    paint.color = dividerColor
                    paint.strokeWidth = 1f
                    canvas.drawLine(28f, currentY, pageWidth - 28f, currentY, paint)

                    paint.color = textColorPrimary
                    paint.textSize = 10f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    canvas.drawText(subName, 32f, currentY + 16f, paint)

                    paint.color = textColorSecondary
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    canvas.drawText(sessDateStr, 180f, currentY + 16f, paint)
                    canvas.drawText("$mins mins (${sess.sessionType})", 380f, currentY + 16f, paint)

                    currentY += 24f
                }
            }
        }

        // 6. FOOTER
        paint.color = dividerColor
        paint.strokeWidth = 1f
        canvas.drawLine(28f, pageHeight - 45f, pageWidth - 28f, pageHeight - 45f, paint)

        paint.color = textColorSecondary
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        canvas.drawText("Study Tracker App • Keep striving for continuous growth and academic excellence!", 28f, pageHeight - 28f, paint)

        pdfDocument.finishPage(page)

        // Write PDF to Cache directory
        val fileName = "Study_Report_${period.name.lowercase()}_${System.currentTimeMillis()}.pdf"
        val pdfFile = File(context.cacheDir, fileName)

        try {
            FileOutputStream(pdfFile).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()
            return pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            return null
        }
    }

    private fun drawStatBox(
        canvas: Canvas,
        paint: Paint,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        label: String,
        value: String,
        bgColor: Int,
        valueColor: Int
    ) {
        paint.color = bgColor
        val rect = RectF(x, y, x + width, y + height)
        canvas.drawRoundRect(rect, 8f, 8f, paint)

        paint.color = Color.parseColor("#5C5E62")
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(label, x + 12f, y + 22f, paint)

        paint.color = valueColor
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(value, x + 12f, y + 46f, paint)
    }

    fun sharePdfFile(context: Context, pdfFile: File) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, pdfFile)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Academic Study Progress Report")
                putExtra(Intent.EXTRA_TEXT, "Here is my study progress report generated with Study Tracker!")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share Study Report PDF")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri

object UpiUtils {

  /**
   * Generates a standard NPCI compliant UPI Payment URI string.
   * e.g. upi://pay?pa=messmanager@okaxis&pn=Office%20Mess&am=840.00&cu=INR&tn=Mess%20Bill%20Sep%202026
   */
  fun buildUpiUri(
    upiId: String,
    payeeName: String,
    amountRupees: Double,
    transactionNote: String
  ): String {
    val cleanUpi = upiId.trim()
    val cleanName = Uri.encode(payeeName.trim().ifBlank { "Office Mess" })
    val formattedAmount = String.format(java.util.Locale.US, "%.2f", amountRupees.coerceAtLeast(0.0))
    val cleanNote = Uri.encode(transactionNote.trim().ifBlank { "Mess Bill Payment" })

    return "upi://pay?pa=$cleanUpi&pn=$cleanName&am=$formattedAmount&cu=INR&tn=$cleanNote"
  }

  fun generateUpiUri(
    upiId: String,
    payeeName: String,
    amountRupees: Double,
    note: String
  ): String = buildUpiUri(upiId, payeeName, amountRupees, note)

  fun launchUpiIntent(context: Context, upiUriString: String): Result<Unit> {
    return try {
      val intent = Intent(Intent.ACTION_VIEW, Uri.parse(upiUriString))
      val chooser = Intent.createChooser(intent, "Pay Mess Bill with UPI")
      chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(chooser)
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Generates a simple, robust deterministic 21x21 QR-like visual module grid
   * for local offline display on Jetpack Compose Canvas.
   */
  fun generateVisualQrGrid(text: String, size: Int = 21): Array<BooleanArray> {
    val grid = Array(size) { BooleanArray(size) }
    val hash = text.hashCode()

    // 1. Draw standard Finder Patterns at 3 corners (7x7)
    drawFinderPattern(grid, 0, 0)
    drawFinderPattern(grid, size - 7, 0)
    drawFinderPattern(grid, 0, size - 7)

    // 2. Draw Timing Patterns (row 6 and col 6)
    for (i in 8 until size - 8) {
      grid[6][i] = (i % 2 == 0)
      grid[i][6] = (i % 2 == 0)
    }

    // 3. Fill payload modules based on data stream
    val bytes = text.toByteArray()
    var bitIndex = 0
    for (r in 0 until size) {
      for (c in 0 until size) {
        // Skip finder zones
        if (isFinderZone(r, c, size)) continue
        if (r == 6 || c == 6) continue

        val byteVal = if (bytes.isNotEmpty()) bytes[bitIndex % bytes.size].toInt() else 0
        val isDark = ((byteVal xor (r * 31 + c * 17 + hash)) and (1 shl (bitIndex % 8))) != 0
        grid[r][c] = isDark
        bitIndex++
      }
    }
    return grid
  }

  private fun drawFinderPattern(grid: Array<BooleanArray>, startR: Int, startC: Int) {
    for (r in 0 until 7) {
      for (c in 0 until 7) {
        val isBorder = (r == 0 || r == 6 || c == 0 || c == 6)
        val isCenter = (r in 2..4 && c in 2..4)
        grid[startR + r][startC + c] = isBorder || isCenter
      }
    }
  }

  private fun isFinderZone(r: Int, c: Int, size: Int): Boolean {
    val inTopLeft = r < 8 && c < 8
    val inTopRight = r < 8 && c >= size - 8
    val inBottomLeft = r >= size - 8 && c < 8
    return inTopLeft || inTopRight || inBottomLeft
  }
}

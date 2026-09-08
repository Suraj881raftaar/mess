package com.example.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object FileUtils {

  fun saveReceiptImageToInternalStorage(context: Context, sourceUri: Uri): String? {
    return try {
      val receiptsDir = File(context.filesDir, "receipts")
      if (!receiptsDir.exists()) {
        receiptsDir.mkdirs()
      }

      val fileName = "receipt_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
      val destinationFile = File(receiptsDir, fileName)

      context.contentResolver.openInputStream(sourceUri)?.use { input: InputStream ->
        FileOutputStream(destinationFile).use { output ->
          input.copyTo(output)
        }
      }
      destinationFile.absolutePath
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }

  fun getFileUri(context: Context, filePath: String): Uri? {
    return try {
      val file = File(filePath)
      if (file.exists()) {
        FileProvider.getUriForFile(
          context,
          "${context.packageName}.fileprovider",
          file
        )
      } else {
        null
      }
    } catch (e: Exception) {
      null
    }
  }

  fun deleteReceiptFile(filePath: String?) {
    if (filePath.isNullOrBlank()) return
    try {
      val file = File(filePath)
      if (file.exists()) {
        file.delete()
      }
    } catch (_: Exception) {}
  }
}

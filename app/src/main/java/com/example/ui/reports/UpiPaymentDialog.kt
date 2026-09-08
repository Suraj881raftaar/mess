package com.example.ui.reports

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.UpiUtils

@Composable
fun UpiPaymentDialog(
  employeeName: String,
  month: String,
  amountRupees: Double,
  upiId: String,
  payeeName: String,
  onDismiss: () -> Unit,
  onPaymentRecorded: () -> Unit
) {
  val context = LocalContext.current
  val note = "Mess bill $month - $employeeName"
  val upiUri = remember(upiId, payeeName, amountRupees, note) {
    UpiUtils.buildUpiUri(upiId, payeeName, amountRupees, note)
  }

  val qrGrid = remember(upiUri) {
    UpiUtils.generateVisualQrGrid(upiUri, size = 21)
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          Icons.Default.QrCode2,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Scan & Pay with UPI", fontWeight = FontWeight.Bold)
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // QR Code Canvas Frame
        Box(
          modifier = Modifier
            .size(200.dp)
            .background(Color.White, shape = RoundedCornerShape(12.dp))
            .border(2.dp, MaterialTheme.colorScheme.outlineVariant, shape = RoundedCornerShape(12.dp))
            .padding(14.dp),
          contentAlignment = Alignment.Center
        ) {
          Canvas(modifier = Modifier.fillMaxWidth().height(172.dp)) {
            val numCells = qrGrid.size
            val cellSize = size.width / numCells
            for (r in 0 until numCells) {
              for (c in 0 until numCells) {
                if (qrGrid[r][c]) {
                  drawRect(
                    color = Color.Black,
                    topLeft = Offset(c * cellSize, r * cellSize),
                    size = Size(cellSize, cellSize)
                  )
                }
              }
            }
          }
        }

        // Amount & Payee Badge
        Card(
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              "Amount: ₹${String.format(java.util.Locale.US, "%.2f", amountRupees)}",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
              "UPI ID: $upiId ($payeeName)",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
          }
        }

        // Action Buttons: Launch UPI app or Share
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Button(
            onClick = {
              val res = UpiUtils.launchUpiIntent(context, upiUri)
              if (res.isFailure) {
                Toast.makeText(context, "No UPI app found. Please scan QR or copy UPI ID.", Toast.LENGTH_SHORT).show()
              }
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp)
          ) {
            Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Open UPI App", fontSize = 12.sp)
          }

          OutlinedButton(
            onClick = {
              val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
              clipboard.setPrimaryClip(ClipData.newPlainText("UPI Payment Link", upiUri))
              Toast.makeText(context, "UPI payment link copied!", Toast.LENGTH_SHORT).show()
            },
            shape = RoundedCornerShape(8.dp)
          ) {
            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Link", modifier = Modifier.size(16.dp))
          }

          OutlinedButton(
            onClick = {
              val shareText = "Office Mess Bill ($month)\nEmployee: $employeeName\nAmount: ₹${String.format(java.util.Locale.US, "%.2f", amountRupees)}\nUPI ID: $upiId\nPay Link: $upiUri"
              val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareText)
                type = "text/plain"
              }
              val shareIntent = Intent.createChooser(sendIntent, "Share Mess Bill")
              context.startActivity(shareIntent)
            },
            shape = RoundedCornerShape(8.dp)
          ) {
            Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onPaymentRecorded,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
      ) {
        Text("Record Payment as Settled")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Close")
      }
    }
  )
}

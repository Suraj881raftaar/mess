package com.example.ui.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.entity.Employee
import com.example.util.CurrencyUtils
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun RecordPaymentDialog(
  employee: Employee,
  month: String,
  payablePaise: Long,
  alreadyPaidPaise: Long,
  pendingPaise: Long,
  onDismiss: () -> Unit,
  onSavePayment: (amountPaise: Long, paymentDate: String, paymentMethod: String, notes: String?) -> Unit
) {
  val defaultDueAmount = if (pendingPaise > 0) String.format(java.util.Locale.US, "%.2f", pendingPaise / 100.0) else ""
  var amountInput by remember { mutableStateOf(defaultDueAmount) }
  var paymentMethod by remember { mutableStateOf("Cash") }
  var notes by remember { mutableStateOf("") }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  val todayDate = remember { LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Payments,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
          Text(
            text = "Record Deposit",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "${employee.name} (${employee.employeeCode})",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Summary of dues
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Total Bill",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = CurrencyUtils.formatPaise(payablePaise),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
              )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "Paid so far",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = CurrencyUtils.formatPaise(alreadyPaidPaise),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }

            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = "Left to Pay",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = CurrencyUtils.formatPaise(pendingPaise),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (pendingPaise > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
              )
            }
          }
        }

        // Amount Input Field
        OutlinedTextField(
          value = amountInput,
          onValueChange = {
            amountInput = it
            errorMessage = null
          },
          label = { Text("Deposit Amount (₹)") },
          placeholder = { Text("0.00") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_deposit_amount"),
          isError = errorMessage != null,
          supportingText = errorMessage?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
        )

        // Quick Preset Chips
        if (pendingPaise > 0) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            val dueRupees = pendingPaise / 100.0
            FilterChip(
              selected = amountInput == String.format(java.util.Locale.US, "%.2f", dueRupees),
              onClick = { amountInput = String.format(java.util.Locale.US, "%.2f", dueRupees) },
              label = { Text("Full Due (₹${dueRupees.toLong()})") },
              modifier = Modifier.testTag("chip_preset_full_due")
            )
            FilterChip(
              selected = amountInput == "500",
              onClick = { amountInput = "500" },
              label = { Text("₹500") }
            )
            FilterChip(
              selected = amountInput == "1000",
              onClick = { amountInput = "1000" },
              label = { Text("₹1,000") }
            )
          }
        }

        // Payment Method Selector
        Column {
          Text(
            text = "Payment Mode",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(4.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            PaymentModeChip(
              label = "Cash",
              icon = Icons.Default.Money,
              selected = paymentMethod == "Cash",
              onClick = { paymentMethod = "Cash" }
            )
            PaymentModeChip(
              label = "UPI",
              icon = Icons.Default.QrCode,
              selected = paymentMethod == "UPI",
              onClick = { paymentMethod = "UPI" }
            )
            PaymentModeChip(
              label = "Bank",
              icon = Icons.Default.AccountBalance,
              selected = paymentMethod == "Bank Transfer",
              onClick = { paymentMethod = "Bank Transfer" }
            )
          }
        }

        // Note
        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text("Note / Transaction ID (Optional)") },
          placeholder = { Text("e.g. Received by cashier") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val parsedRupees = amountInput.toDoubleOrNull()
          if (parsedRupees == null || parsedRupees <= 0.0) {
            errorMessage = "Please enter a valid deposit amount > 0"
            return@Button
          }
          val paise = (parsedRupees * 100).toLong()
          onSavePayment(paise, todayDate, paymentMethod, notes.ifBlank { null })
        },
        modifier = Modifier.testTag("btn_save_deposit")
      ) {
        Text("Save Deposit")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

@Composable
private fun PaymentModeChip(
  label: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  selected: Boolean,
  onClick: () -> Unit
) {
  FilterChip(
    selected = selected,
    onClick = onClick,
    label = { Text(label) },
    leadingIcon = {
      Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(16.dp)
      )
    }
  )
}

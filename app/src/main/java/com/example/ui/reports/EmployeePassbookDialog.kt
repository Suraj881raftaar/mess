package com.example.ui.reports

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.entity.Payment
import com.example.util.CurrencyUtils

@Composable
fun EmployeePassbookDialog(
  item: EmployeeReportItem,
  formattedMonth: String,
  onDismiss: () -> Unit,
  onDeletePayment: (Payment) -> Unit,
  onOpenDepositDialog: () -> Unit
) {
  val employee = item.employee
  var paymentToDelete by remember { mutableStateOf<Payment?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Payments,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = "Payment Passbook",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "${employee.name} • ${employee.employeeCode} ($formattedMonth)",
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
          .padding(vertical = 4.dp)
      ) {
        // Balance Overview Tile
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
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
                text = CurrencyUtils.formatPaise(item.payablePaise),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "Deposited",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = CurrencyUtils.formatPaise(item.paidPaise),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
              )
            }

            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = "Left to Pay",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = CurrencyUtils.formatPaise(item.pendingPaise),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (item.pendingPaise > 0) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "Deposits Made (${item.payments.size})",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (item.payments.isEmpty()) {
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = "No deposits recorded yet for this month.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        } else {
          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .height(200.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            items(item.payments, key = { it.id }) { payment ->
              Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Text(
                        text = CurrencyUtils.formatPaise(payment.amountPaise),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                      )
                      Spacer(modifier = Modifier.width(8.dp))
                      Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                      ) {
                        Text(
                          text = payment.paymentMethod,
                          style = MaterialTheme.typography.labelSmall,
                          modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                      }
                    }
                    Text(
                      text = "${payment.paymentDate}${if (!payment.notes.isNullOrBlank()) " • ${payment.notes}" else ""}",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }

                  IconButton(
                    onClick = { paymentToDelete = payment },
                    modifier = Modifier.size(32.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.Delete,
                      contentDescription = "Delete Deposit",
                      tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                      modifier = Modifier.size(18.dp)
                    )
                  }
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
          onClick = {
            onDismiss()
            onOpenDepositDialog()
          },
          shape = RoundedCornerShape(8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text("Record Deposit")
        }
        TextButton(onClick = onDismiss) {
          Text("Close")
        }
      }
    }
  )

  // Confirmation dialog before deleting a deposit
  paymentToDelete?.let { payment ->
    AlertDialog(
      onDismissRequest = { paymentToDelete = null },
      title = { Text("Delete Deposit Record?") },
      text = {
        Text("Are you sure you want to remove the deposit of ${CurrencyUtils.formatPaise(payment.amountPaise)} on ${payment.paymentDate}?")
      },
      confirmButton = {
        Button(
          onClick = {
            onDeletePayment(payment)
            paymentToDelete = null
          }
        ) {
          Text("Delete")
        }
      },
      dismissButton = {
        TextButton(onClick = { paymentToDelete = null }) {
          Text("Cancel")
        }
      }
    )
  }
}

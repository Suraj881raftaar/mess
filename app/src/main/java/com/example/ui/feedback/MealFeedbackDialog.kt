package com.example.ui.feedback

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MealType

@Composable
fun MealFeedbackDialog(
  mealType: MealType,
  date: String,
  dishName: String?,
  onDismiss: () -> Unit,
  onSubmit: (rating: Int, comment: String?, employeeName: String?) -> Unit
) {
  var rating by remember { mutableIntStateOf(5) }
  var comment by remember { mutableStateOf("") }
  var employeeName by remember { mutableStateOf("") }

  val ratingLabels = listOf(
    "1 - Poor / Needs Improvement",
    "2 - Below Average",
    "3 - Average / Good",
    "4 - Very Good",
    "5 - Delicious / Outstanding! 🌟"
  )

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Column {
        Text(
          "Rate ${mealType.displayName}",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
        dishName?.let {
          Text(
            it,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Star Selector
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
          Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) {
            (1..5).forEach { starIndex ->
              Icon(
                imageVector = if (starIndex <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = "$starIndex stars",
                tint = if (starIndex <= rating) Color(0xFFFFB300) else MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier
                  .size(38.dp)
                  .clickable { rating = starIndex }
                  .padding(2.dp)
              )
            }
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            ratingLabels[(rating - 1).coerceIn(0, 4)],
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
        }

        OutlinedTextField(
          value = comment,
          onValueChange = { comment = it },
          label = { Text("Comments / Feedback (Taste, Portions, Salt...)") },
          placeholder = { Text("e.g., Paneer was fresh, Dal was great!") },
          modifier = Modifier.fillMaxWidth(),
          minLines = 2,
          shape = RoundedCornerShape(10.dp)
        )

        OutlinedTextField(
          value = employeeName,
          onValueChange = { employeeName = it },
          label = { Text("Your Name (Optional / Anonymous)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp)
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onSubmit(rating, comment.ifBlank { null }, employeeName.ifBlank { null })
        }
      ) {
        Text("Submit Review")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

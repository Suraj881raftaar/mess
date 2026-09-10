package com.example.ui.hub

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun MoreHubScreen(
  activeEmployeeCount: Int,
  totalExpensesCount: Int,
  pantryItemsCount: Int,
  onNavigateToEmployees: () -> Unit,
  onNavigateToExpenses: () -> Unit,
  onNavigateToPantry: () -> Unit,
  onNavigateToSettings: () -> Unit,
  modifier: Modifier = Modifier
) {
  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("more_hub_screen"),
    contentPadding = PaddingValues(20.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp)) {
        Text(
          text = "Administration & Tools",
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Staff directory, kitchen inventory, expense register, and settings",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    item {
      HubNavigationCard(
        title = "Staff Directory",
        subtitle = "Manage active employees, departments, and dietary preferences",
        badgeText = "$activeEmployeeCount Active",
        icon = Icons.Default.Badge,
        iconTint = MaterialTheme.colorScheme.primary,
        iconContainer = MaterialTheme.colorScheme.primaryContainer,
        onClick = onNavigateToEmployees,
        testTag = "hub_card_employees"
      )
    }

    item {
      HubNavigationCard(
        title = "Pantry & Groceries",
        subtitle = "Stock tracking, low-stock alerts, auto-restock, and shopping list",
        badgeText = "$pantryItemsCount Items",
        icon = Icons.Default.Inventory2,
        iconTint = Color(0xFF1B5E20),
        iconContainer = Color(0xFFC8E6C9),
        onClick = onNavigateToPantry,
        testTag = "hub_card_pantry"
      )
    }

    item {
      HubNavigationCard(
        title = "Expense Register",
        subtitle = "Record & categorize mess grocery purchases, vegetables, and fuel",
        badgeText = "$totalExpensesCount Logged",
        icon = Icons.AutoMirrored.Filled.ReceiptLong,
        iconTint = Color(0xFFE65100),
        iconContainer = Color(0xFFFFE0B2),
        onClick = onNavigateToExpenses,
        testTag = "hub_card_expenses"
      )
    }

    item {
      HubNavigationCard(
        title = "Mess Settings",
        subtitle = "Configure mess name, currency settings, backup, and preferences",
        badgeText = "Configure",
        icon = Icons.Default.Settings,
        iconTint = MaterialTheme.colorScheme.secondary,
        iconContainer = MaterialTheme.colorScheme.secondaryContainer,
        onClick = onNavigateToSettings,
        testTag = "hub_card_settings"
      )
    }

    item {
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun HubNavigationCard(
  title: String,
  subtitle: String,
  badgeText: String,
  icon: ImageVector,
  iconTint: Color,
  iconContainer: Color,
  onClick: () -> Unit,
  testTag: String
) {
  Card(
    onClick = onClick,
    modifier = Modifier
      .fillMaxWidth()
      .testTag(testTag),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(52.dp)
          .clip(CircleShape)
          .background(iconContainer),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = iconTint,
          modifier = Modifier.size(26.dp)
        )
      }

      Spacer(modifier = Modifier.width(16.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
          ) {
            Text(
              text = badgeText,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          lineHeight = MaterialTheme.typography.bodySmall.lineHeight
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.size(20.dp)
      )
    }
  }
}

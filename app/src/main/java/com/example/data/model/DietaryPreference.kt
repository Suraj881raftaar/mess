package com.example.data.model

enum class DietaryPreference(val displayName: String, val badgeEmoji: String) {
  REGULAR_VEG("Vegetarian", "🥬"),
  JAIN("Jain", "🌿"),
  EGGITARIAN("Eggetarian", "🥚"),
  NON_VEG("Non-Veg", "🍗"),
  VEGAN("Vegan", "🌱"),
  GLUTEN_FREE("Gluten-Free", "🌾");

  val label: String
    get() = "$badgeEmoji $displayName"

  companion object {
    val VEG = REGULAR_VEG

    fun fromString(value: String?): DietaryPreference {
      return entries.find { it.name.equals(value, ignoreCase = true) } ?: REGULAR_VEG
    }
  }
}

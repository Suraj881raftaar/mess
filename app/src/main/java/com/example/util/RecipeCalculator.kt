package com.example.util

data class RecipeIngredient(
  val name: String,
  val baseQuantityPerPerson: Double,
  val unit: String,
  val notes: String? = null
)

data class RecipeDish(
  val id: String,
  val name: String,
  val category: String, // Breakfast, Main Curry, Rice & Bread, Dal
  val isVeg: Boolean = true,
  val ingredients: List<RecipeIngredient>
)

object RecipeCalculator {

  val dishes = listOf(
    RecipeDish(
      id = "dal_tadka",
      name = "Yellow Dal Tadka",
      category = "Dal & Pulses",
      ingredients = listOf(
        RecipeIngredient("Toor Dal / Moong Dal", 0.07, "kg", "70g dry dal per person"),
        RecipeIngredient("Onions", 0.05, "kg"),
        RecipeIngredient("Tomatoes", 0.04, "kg"),
        RecipeIngredient("Garlic & Ginger", 0.01, "kg"),
        RecipeIngredient("Ghee / Oil", 0.015, "L"),
        RecipeIngredient("Mustard & Cumin Seeds", 0.005, "kg")
      )
    ),
    RecipeDish(
      id = "paneer_butter_masala",
      name = "Paneer Butter Masala",
      category = "Main Curry",
      ingredients = listOf(
        RecipeIngredient("Fresh Paneer", 0.12, "kg", "120g paneer cubes per person"),
        RecipeIngredient("Tomatoes (for gravy)", 0.10, "kg"),
        RecipeIngredient("Onions", 0.06, "kg"),
        RecipeIngredient("Butter & Fresh Cream", 0.025, "kg"),
        RecipeIngredient("Cashews / Magajtari", 0.015, "kg"),
        RecipeIngredient("Garam Masala & Kasuri Methi", 0.005, "kg")
      )
    ),
    RecipeDish(
      id = "veg_biryani",
      name = "Hyderabadi Veg Biryani / Pulao",
      category = "Rice Dishes",
      ingredients = listOf(
        RecipeIngredient("Basmati Rice", 0.10, "kg", "100g raw rice per person"),
        RecipeIngredient("Mixed Vegetables (Carrot, Beans, Peas)", 0.08, "kg"),
        RecipeIngredient("Potatoes", 0.04, "kg"),
        RecipeIngredient("Curd / Yogurt", 0.03, "L"),
        RecipeIngredient("Fried Onions (Birista)", 0.03, "kg"),
        RecipeIngredient("Biryani Spices & Mint/Coriander", 0.01, "kg"),
        RecipeIngredient("Ghee / Oil", 0.02, "L")
      )
    ),
    RecipeDish(
      id = "chole_masala",
      name = "Amritsari Chole Masala",
      category = "Main Curry",
      ingredients = listOf(
        RecipeIngredient("Kabuli Chana (Dry Chickpeas)", 0.08, "kg", "Soak 8 hours"),
        RecipeIngredient("Onions", 0.06, "kg"),
        RecipeIngredient("Tomatoes", 0.05, "kg"),
        RecipeIngredient("Chole Masala Powder", 0.01, "kg"),
        RecipeIngredient("Cooking Oil", 0.015, "L")
      )
    ),
    RecipeDish(
      id = "rajma_curry",
      name = "Punjabi Rajma Curry",
      category = "Dal & Pulses",
      ingredients = listOf(
        RecipeIngredient("Kashmiri / Chitra Rajma", 0.08, "kg", "Soak overnight"),
        RecipeIngredient("Onions", 0.06, "kg"),
        RecipeIngredient("Tomatoes", 0.07, "kg"),
        RecipeIngredient("Ginger Garlic Paste", 0.015, "kg"),
        RecipeIngredient("Cooking Oil", 0.015, "L")
      )
    ),
    RecipeDish(
      id = "poha",
      name = "Kanda Poha & Tea",
      category = "Breakfast",
      ingredients = listOf(
        RecipeIngredient("Thick Poha (Flattened Rice)", 0.08, "kg"),
        RecipeIngredient("Onions", 0.04, "kg"),
        RecipeIngredient("Potatoes", 0.03, "kg"),
        RecipeIngredient("Peanuts", 0.015, "kg"),
        RecipeIngredient("Mustard Seeds, Curry Leaves, Green Chillies", 0.005, "kg"),
        RecipeIngredient("Lemon & Fresh Coriander", 0.01, "kg")
      )
    ),
    RecipeDish(
      id = "sambhar",
      name = "South Indian Sambhar",
      category = "Dal & Pulses",
      ingredients = listOf(
        RecipeIngredient("Toor Dal", 0.05, "kg"),
        RecipeIngredient("Drumstick & Pumpkin / Bottle Gourd", 0.07, "kg"),
        RecipeIngredient("Shallots / Small Onions", 0.03, "kg"),
        RecipeIngredient("Tamarind Paste", 0.01, "kg"),
        RecipeIngredient("Sambhar Powder", 0.008, "kg")
      )
    ),
    RecipeDish(
      id = "roti_chapati",
      name = "Fresh Phulka / Chapati",
      category = "Breads",
      ingredients = listOf(
        RecipeIngredient("Whole Wheat Flour (Atta)", 0.09, "kg", "Yields ~3-4 rotis per person"),
        RecipeIngredient("Water & Salt", 0.05, "L"),
        RecipeIngredient("Oil / Ghee for brushing", 0.01, "L")
      )
    )
  )

  fun calculateIngredients(dish: RecipeDish, headcount: Int): List<Pair<RecipeIngredient, Double>> {
    val count = headcount.coerceAtLeast(1)
    return dish.ingredients.map { ingredient ->
      val total = ingredient.baseQuantityPerPerson * count
      val rounded = (Math.round(total * 100.0) / 100.0).coerceAtLeast(0.01)
      ingredient to rounded
    }
  }
}

package com.babyygurll.store.data.model

data class BoutiqueSubcategory(
    val id: String,
    val name: String
)

data class BoutiqueCategory(
    val id: String,
    val name: String,
    val icon: String,
    val subcategories: List<BoutiqueSubcategory>
)

object BoutiqueCategoryData {
    val categories = listOf(
        BoutiqueCategory(
            id = "hair-accessories",
            name = "Hair Accessories",
            icon = "🎀",
            subcategories = listOf(
                BoutiqueSubcategory("crochet-scrunchies", "Crochet Scrunchies"),
                BoutiqueSubcategory("crochet-bandanas", "Crochet Bandanas"),
                BoutiqueSubcategory("crochet-headbands", "Crochet Headbands"),
                BoutiqueSubcategory("hair-bows", "Hair Bows"),
                BoutiqueSubcategory("hair-clips-ties", "Hair Clips & Ties"),
                BoutiqueSubcategory("headscarves", "Headscarves"),
                BoutiqueSubcategory("hair-accessories-sets", "Hair Accessories Sets")
            )
        ),
        BoutiqueCategory(
            id = "bags-purses",
            name = "Bags & Purses",
            icon = "👜",
            subcategories = listOf(
                BoutiqueSubcategory("crochet-tote-bags", "Crochet Tote Bags"),
                BoutiqueSubcategory("mini-bags", "Mini Bags"),
                BoutiqueSubcategory("shoulder-bags", "Shoulder Bags"),
                BoutiqueSubcategory("sling-bags", "Sling Bags"),
                BoutiqueSubcategory("handbags", "Handbags"),
                BoutiqueSubcategory("coin-purses", "Coin Purses"),
                BoutiqueSubcategory("pouches-wallets", "Pouches & Wallets"),
                BoutiqueSubcategory("phone-bags", "Phone Bags"),
                BoutiqueSubcategory("bag-charms", "Bag Charms")
            )
        ),
        BoutiqueCategory(
            id = "flowers-bouquets",
            name = "Flowers & Bouquets",
            icon = "💐",
            subcategories = listOf(
                BoutiqueSubcategory("crochet-roses", "Crochet Roses"),
                BoutiqueSubcategory("crochet-tulips", "Crochet Tulips"),
                BoutiqueSubcategory("crochet-sunflowers", "Crochet Sunflowers"),
                BoutiqueSubcategory("crochet-daisies", "Crochet Daisies"),
                BoutiqueSubcategory("crochet-lilies", "Crochet Lilies"),
                BoutiqueSubcategory("mixed-flower-bouquets", "Mixed Flower Bouquets"),
                BoutiqueSubcategory("single-stem-flowers", "Single Stem Flowers"),
                BoutiqueSubcategory("flower-keychains", "Flower Keychains"),
                BoutiqueSubcategory("flower-accessories", "Flower Accessories")
            )
        ),
        BoutiqueCategory(
            id = "amigurumi-plushies",
            name = "Amigurumi & Plushies",
            icon = "🧸",
            subcategories = listOf(
                BoutiqueSubcategory("crochet-teddy-bears", "Crochet Teddy Bears"),
                BoutiqueSubcategory("crochet-bunnies", "Crochet Bunnies"),
                BoutiqueSubcategory("crochet-cats", "Crochet Cats"),
                BoutiqueSubcategory("crochet-dogs", "Crochet Dogs"),
                BoutiqueSubcategory("crochet-dolls", "Crochet Dolls"),
                BoutiqueSubcategory("mini-plushies", "Mini Plushies"),
                BoutiqueSubcategory("food-plushies", "Food Plushies"),
                BoutiqueSubcategory("character-plushies", "Character Plushies"),
                BoutiqueSubcategory("plushie-keychains", "Plushie Keychains")
            )
        ),
        BoutiqueCategory(
            id = "clothing-wearables",
            name = "Clothing & Wearables",
            icon = "🧣",
            subcategories = listOf(
                BoutiqueSubcategory("crochet-scarves", "Crochet Scarves"),
                BoutiqueSubcategory("crochet-shawls", "Crochet Shawls"),
                BoutiqueSubcategory("crochet-sweaters", "Crochet Sweaters"),
                BoutiqueSubcategory("crochet-cardigans", "Crochet Cardigans"),
                BoutiqueSubcategory("crochet-tops", "Crochet Tops"),
                BoutiqueSubcategory("crochet-dresses", "Crochet Dresses"),
                BoutiqueSubcategory("crochet-shrugs", "Crochet Shrugs"),
                BoutiqueSubcategory("crochet-skirts", "Crochet Skirts"),
                BoutiqueSubcategory("crochet-beanies-hats", "Crochet Beanies & Hats"),
                BoutiqueSubcategory("crochet-gloves-mittens", "Crochet Gloves & Mittens"),
                BoutiqueSubcategory("crochet-leg-warmers", "Crochet Leg Warmers")
            )
        ),
        BoutiqueCategory(
            id = "home-living",
            name = "Home & Living",
            icon = "🏡",
            subcategories = listOf(
                BoutiqueSubcategory("crochet-coasters", "Crochet Coasters"),
                BoutiqueSubcategory("crochet-table-runners", "Crochet Table Runners"),
                BoutiqueSubcategory("crochet-mats", "Crochet Mats"),
                BoutiqueSubcategory("cushion-covers", "Cushion Covers"),
                BoutiqueSubcategory("crochet-blankets", "Crochet Blankets"),
                BoutiqueSubcategory("crochet-plant-hangers", "Crochet Plant Hangers"),
                BoutiqueSubcategory("crochet-flower-pots", "Crochet Flower Pots"),
                BoutiqueSubcategory("wall-hangings", "Wall Hangings"),
                BoutiqueSubcategory("decorative-baskets", "Decorative Baskets"),
                BoutiqueSubcategory("home-decor-accessories", "Home Decor Accessories")
            )
        ),
        BoutiqueCategory(
            id = "jewellery-keychains",
            name = "Jewellery & Keychains",
            icon = "💍",
            subcategories = listOf(
                BoutiqueSubcategory("crochet-bracelets", "Crochet Bracelets"),
                BoutiqueSubcategory("crochet-necklaces", "Crochet Necklaces"),
                BoutiqueSubcategory("crochet-earrings", "Crochet Earrings"),
                BoutiqueSubcategory("crochet-rings", "Crochet Rings"),
                BoutiqueSubcategory("crochet-keychains", "Crochet Keychains"),
                BoutiqueSubcategory("bag-charms", "Bag Charms"),
                BoutiqueSubcategory("phone-charms", "Phone Charms")
            )
        ),
        BoutiqueCategory(
            id = "gifts-special",
            name = "Gifts & Special Collections",
            icon = "🎁",
            subcategories = listOf(
                BoutiqueSubcategory("birthday-gifts", "Birthday Gifts"),
                BoutiqueSubcategory("anniversary-gifts", "Anniversary Gifts"),
                BoutiqueSubcategory("couple-gifts", "Couple Gifts"),
                BoutiqueSubcategory("personalised-gifts", "Personalised Gifts"),
                BoutiqueSubcategory("crochet-gift-sets", "Crochet Gift Sets"),
                BoutiqueSubcategory("mini-gift-hampers", "Mini Gift Hampers"),
                BoutiqueSubcategory("custom-crochet-orders", "Custom Crochet Orders")
            )
        )
    )

    fun getCategoryById(id: String): BoutiqueCategory? {
        return categories.firstOrNull { it.id == id }
    }
}


package com.babyygurll.admin.util

object TagAutomation {
    fun generateTags(
        category: String,
        categoryName: String,
        subcategory: String,
        subcategoryName: String,
        title: String,
        description: String
    ): List<String> {
        val fullText = listOf(category, categoryName, subcategory, subcategoryName, title, description)
            .joinToString(" ")
            .lowercase()

        val tags = linkedSetOf<String>()

        // 1. Core artisanal crochet tags
        tags.add("crochet")
        tags.add("handmade")

        fun hasWord(word: String): Boolean {
            val pattern = Regex("(?:^|[^a-z0-9])${Regex.escape(word.lowercase())}(?:$|[^a-z0-9])", RegexOption.IGNORE_CASE)
            return pattern.containsMatchIn(fullText)
        }

        // 2. Exact phrase matching for high-intent shopping queries
        val phraseMatches = listOf(
            "triangle bandana", "hair scarf", "head scarf", "hair accessory", "hair accessories",
            "tote bag", "shoulder bag", "crossbody bag", "granny square", "milk cotton",
            "gift for her", "desk decor", "home decor", "wall hanging", "mug rug",
            "bucket hat", "crop top", "cushion cover", "bag charm", "plushie keychain",
            "strawberry cow", "forever flowers", "open knit", "cottagecore", "cozy"
        )
        for (phrase in phraseMatches) {
            if (fullText.contains(phrase)) tags.add(phrase)
        }

        // 3. Keyword to tags mapping (checked with word boundaries)
        val keywordMap = mapOf(
            "bandana" to listOf("bandana", "headwear"),
            "headscarf" to listOf("headscarf", "hair scarf"),
            "scrunchie" to listOf("scrunchie", "hair tie", "hair accessory"),
            "plush" to listOf("plushie", "amigurumi"),
            "plushie" to listOf("plushie", "amigurumi"),
            "amigurumi" to listOf("amigurumi", "plushie"),
            "bag" to listOf("crochet bag"),
            "tote" to listOf("tote bag", "crochet bag"),
            "purse" to listOf("purse", "crochet bag"),
            "tulip" to listOf("tulip", "floral", "flower bouquet"),
            "rose" to listOf("rose", "floral", "flower bouquet"),
            "sunflower" to listOf("sunflower", "floral"),
            "daisy" to listOf("daisy", "floral"),
            "bouquet" to listOf("bouquet", "forever flowers"),
            "flower" to listOf("floral", "flowers"),
            "bloom" to listOf("floral"),
            "coaster" to listOf("coaster", "tableware", "home decor"),
            "cardigan" to listOf("cardigan", "apparel", "knitwear"),
            "sweater" to listOf("sweater", "apparel"),
            "vest" to listOf("vest", "apparel"),
            "scarf" to listOf("scarf"),
            "beanie" to listOf("beanie", "winter hat"),
            "beret" to listOf("beret"),
            "hat" to listOf("crochet hat"),
            "keychain" to listOf("keychain", "bag charm"),
            "cow" to listOf("cow plush", "cute animal"),
            "bunny" to listOf("bunny plush", "cute animal"),
            "cat" to listOf("cat plush", "cute animal"),
            "dog" to listOf("dog plush", "cute animal"),
            "boho" to listOf("boho", "bohemian"),
            "bohemian" to listOf("boho", "bohemian"),
            "vintage" to listOf("vintage"),
            "cotton" to listOf("milk cotton"),
            "chenille" to listOf("chenille yarn"),
            "wool" to listOf("wool"),
            "aesthetic" to listOf("aesthetic"),
            "pastel" to listOf("pastel"),
            "retro" to listOf("retro"),
            "triangle" to listOf("triangle bandana"),
            "summer" to listOf("summer style"),
            "beach" to listOf("beach wear"),
            "gift" to listOf("gift for her")
        )

        for ((key, mapped) in keywordMap) {
            if (hasWord(key)) {
                tags.addAll(mapped)
            }
        }

        // 4. Subcategory-specific enrichment
        val sub = (if (subcategoryName.isNotBlank()) subcategoryName else subcategory).lowercase()
        if (sub.contains("bandana")) { tags.add("bandana"); tags.add("headwear"); tags.add("hair accessories") }
        if (sub.contains("scrunchie")) { tags.add("scrunchie"); tags.add("hair accessories") }
        if (sub.contains("tote")) { tags.add("tote bag"); tags.add("crochet bag") }
        if (sub.contains("shoulder")) { tags.add("shoulder bag"); tags.add("crochet bag") }
        if (sub.contains("hat") || sub.contains("beanie")) { tags.add("crochet hat") }
        if (sub.contains("plush") || sub.contains("amigurumi")) { tags.add("amigurumi"); tags.add("plushie") }
        if (sub.contains("bouquet") || sub.contains("flower")) { tags.add("forever flowers"); tags.add("floral") }
        if (sub.contains("coaster") || sub.contains("rug")) { tags.add("home decor"); tags.add("coaster") }
        if (sub.contains("cardigan") || sub.contains("sweater") || sub.contains("top")) { tags.add("knitwear"); tags.add("apparel") }

        // 5. Title specific unique tokens
        val stopWords = setOf("with", "from", "this", "that", "your", "made", "item", "boutique", "babyygurll", "piece", "hand")
        val titleTokens = title.lowercase()
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.length >= 4 && !stopWords.contains(it) && it != "crochet" && it != "handmade" && it != "handcrafted" }

        for (token in titleTokens) {
            tags.add(token)
        }

        return tags.filter { it.isNotBlank() }.take(15)
    }
}


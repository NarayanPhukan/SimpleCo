// BabyyGurll Crochet - Product Catalog Data
const PRODUCTS_DATA = [
  {
    id: "bgc-cow-01",
    name: "Strawberry Cow Amigurumi Plush",
    category: "plushies",
    categoryName: "Amigurumi Plushies",
    price: 34.99,
    originalPrice: 42.00,
    image: "assets/images/strawberry-cow.jpg",
    badge: "Bestseller 🔥",
    rating: 4.9,
    reviewsCount: 184,
    leadTime: "Ready to ship (1-2 days)",
    description: "An internet sensation! Our bestselling handmade strawberry cow is crafted with super-plush milk cotton chenille yarn, charming pink patches, and crochet strawberry horns. Accented with a dainty daisy flower collar.",
    specs: {
      yarn: "100% Milk Cotton & Chunky Chenille",
      height: "7.5 inches tall",
      stuffing: "Hypoallergenic Cloudfill",
      care: "Hand wash cold, air dry flat"
    },
    colors: [
      { name: "Strawberry Pink", hex: "#ff9ebb" },
      { name: "Matcha Mint", hex: "#b4d8ae" },
      { name: "Blueberry Bliss", hex: "#b8d0eb" }
    ],
    inStock: true
  },
  {
    id: "bgc-tulip-02",
    name: "Everlasting Pastel Tulip Bouquet",
    category: "floral",
    categoryName: "Floral & Home",
    price: 42.00,
    originalPrice: 49.00,
    image: "assets/images/tulip-bouquet.jpg",
    badge: "Top Gift 🌸",
    rating: 5.0,
    reviewsCount: 212,
    leadTime: "Handmade in 2-3 days",
    description: "Real flowers wilt, but these handmade crochet blooms last forever! Features 9 meticulously stitched pastel tulips and mini daisies wrapped in aesthetic Korean matte floral paper with a luxurious satin ribbon bow.",
    specs: {
      yarn: "Premium Combed Milk Cotton",
      height: "14 inches overall",
      stems: "Flexible floral craft wire",
      extras: "Free handwritten gift card included"
    },
    colors: [
      { name: "Blush Harmony", hex: "#ffb4c2" },
      { name: "Sunrise Peach", hex: "#fed1b2" },
      { name: "Lavender Glow", hex: "#d7bde2" }
    ],
    inStock: true
  },
  {
    id: "bgc-bag-03",
    name: "Daisy Granny Square Tote Bag",
    category: "bags",
    categoryName: "Bags & Totes",
    price: 38.50,
    originalPrice: 45.00,
    image: "assets/images/granny-bag.jpg",
    badge: "Trending ✨",
    rating: 4.8,
    reviewsCount: 95,
    leadTime: "Ready to ship (1-2 days)",
    description: "The ultimate aesthetic tote for farmers markets, bookstore dates, and yarn trips. Handcrafted by assembling 13 intricate floral granny squares with double-stitched reinforced strap handles for daily durability.",
    specs: {
      yarn: "100% Organic Heavyweight Cotton",
      dimensions: "14.5\" width x 15\" height",
      strapDrop: "11.5 inches comfortable shoulder drop",
      capacity: "Fits 13\" laptop, iPad, books & water bottle"
    },
    colors: [
      { name: "Cream & Sage", hex: "#d0e1d4" },
      { name: "Pastel Sherbet", hex: "#fce1e4" },
      { name: "Vintage Cocoa", hex: "#d4a373" }
    ],
    inStock: true
  },
  {
    id: "bgc-cardigan-04",
    name: "Cloud Dream Patchwork Cardigan",
    category: "wearables",
    categoryName: "Wearables",
    price: 78.00,
    originalPrice: 92.00,
    image: "assets/images/crochet-cardigan.jpg",
    badge: "Artisan Heirloom ☁️",
    rating: 4.9,
    reviewsCount: 67,
    leadTime: "Custom made in 4-6 days",
    description: "A statement heirloom knit wear. Individually crocheted cloud patchwork squares joined together into an ultra-cozy oversized slouchy silhouette. Features ribbed balloon cuffs and vintage mother-of-pearl buttons.",
    specs: {
      yarn: "Merino Wool & Soft Acrylic Blend",
      fit: "Slouchy Oversized (Fits US 4 - 14)",
      buttons: "Authentic mother-of-pearl look fasteners",
      care: "Delicate wool cycle or hand wash only"
    },
    colors: [
      { name: "Pastel Sorbet", hex: "#fed7c3" },
      { name: "Warm Vanilla", hex: "#fdf0d5" },
      { name: "Lilac Mist", hex: "#e2cfea" }
    ],
    inStock: true
  },
  {
    id: "bgc-bunny-05",
    name: "Sweet Lavender Bunny Plushie",
    category: "plushies",
    categoryName: "Amigurumi Plushies",
    price: 29.00,
    originalPrice: 34.00,
    image: "assets/images/lavender-bunny.jpg",
    badge: "New Arrival 🐰",
    rating: 4.9,
    reviewsCount: 88,
    leadTime: "Ready to ship (1-2 days)",
    description: "Meet Pip the floppy-eared bunny! Hand-crocheted with plush velvet yarn, sweet blushing cheeks, and a delicate scalloped collar. Comes with an adoption certificate and care card.",
    specs: {
      yarn: "Velvety Chenille Yarn",
      height: "9.2 inches ear to toe",
      safety: "Reinforced safety lock eyes",
      care: "Spot clean with damp cloth"
    },
    colors: [
      { name: "Lavender Mist", hex: "#dcd6f7" },
      { name: "Blush Peach", hex: "#ffcad4" },
      { name: "Cloud White", hex: "#f8f9fa" }
    ],
    inStock: true
  },
  {
    id: "bgc-coasters-06",
    name: "Petal Blossom Coaster Set (Set of 4)",
    category: "floral",
    categoryName: "Floral & Home",
    price: 22.00,
    originalPrice: 26.00,
    image: "assets/images/crochet-coasters.jpg",
    badge: "Cozy Home ☕",
    rating: 4.8,
    reviewsCount: 53,
    leadTime: "Ready to ship",
    description: "Level up your morning coffee or matcha tea with four floral scalloped mug rugs. Naturally absorbent cotton protects your desk surface while looking effortlessly charming.",
    specs: {
      yarn: "100% Pure Natural Cotton",
      quantity: "Set of 4 coasters",
      diameter: "4.8 inches diameter each",
      care: "Machine washable in laundry bag"
    },
    colors: [
      { name: "Floral Melody", hex: "#f4a261" },
      { name: "Garden Sage", hex: "#a8dadc" },
      { name: "Sweet Rose", hex: "#f28482" }
    ],
    inStock: true
  },
  {
    id: "bgc-hat-07",
    name: "Pastel Wavy Brim Bucket Hat",
    category: "wearables",
    categoryName: "Wearables",
    price: 26.50,
    originalPrice: 32.00,
    image: "assets/images/bucket-hat.jpg",
    badge: "Summer Vibes 👒",
    rating: 4.7,
    reviewsCount: 74,
    leadTime: "Ready to ship (1-2 days)",
    description: "Protect your curls in style with this whimsical scalloped bucket hat. Woven with breathable lightweight cotton thread, it folds seamlessly into any handbag without losing its wavy structure.",
    specs: {
      yarn: "100% Breathable Combed Cotton",
      size: "Stretchy crown (fits 21.5\" - 23\")",
      brim: "Wavy flutter scalloped edge",
      care: "Cold rinse, shape and lay flat"
    },
    colors: [
      { name: "Sky & Cream", hex: "#cbf3f0" },
      { name: "Strawberry Shortcake", hex: "#ffccd5" },
      { name: "Buttercup Daisy", hex: "#fff3b0" }
    ],
    inStock: true
  },
  {
    id: "bgc-keychain-08",
    name: "Twin Strawberry Bell Bag Charm",
    category: "accessories",
    categoryName: "Accessories",
    price: 14.00,
    originalPrice: 18.00,
    image: "assets/images/strawberry-keychain.jpg",
    badge: "Under $15 🍓",
    rating: 5.0,
    reviewsCount: 310,
    leadTime: "Ready to ship (Same day)",
    description: "The cutest companion for your keys, tote bag, or backpack zipper! Handcrafted pair of micro strawberries with little crochet leaves, golden bell chime, and a sturdy 14k gold-plated swivel clasp.",
    specs: {
      yarn: "Fine Cotton Micro-Thread",
      hardware: "14k Gold-plated rust-resistant alloy",
      length: "4.5 inches total drop",
      chime: "Gentle soft-sounding bell"
    },
    colors: [
      { name: "Ripe Red", hex: "#e63946" },
      { name: "Milky Pink", hex: "#ffb7b2" },
      { name: "Snow Strawberry White", hex: "#faedcd" }
    ],
    inStock: true
  }
];

// Demo sample reviews
const STORE_REVIEWS = [
  {
    name: "Chloe M.",
    location: "Los Angeles, CA",
    rating: 5,
    date: "2 days ago",
    item: "Strawberry Cow Plush",
    text: "Literally the softest plushie I have ever owned! The stitching is so tight and neat, and the packaging smelled like vanilla sweet flowers. 100/10 recommend BabyyGurll Crochet!",
    avatar: "CM"
  },
  {
    name: "Jessica P.",
    location: "Seattle, WA",
    rating: 5,
    date: "1 week ago",
    item: "Everlasting Tulip Bouquet",
    text: "Ordered this for my bestie's dorm room warming gift. She cried happy tears! They look stunning on her desk and won't ever wilt. The wrapping paper and satin ribbon are chef's kiss.",
    avatar: "JP"
  },
  {
    name: "Aaliyah K.",
    location: "Austin, TX",
    rating: 5,
    date: "2 weeks ago",
    item: "Daisy Granny Square Tote",
    text: "I take this bag everywhere. Super sturdy, straps don't stretch out even when loaded with my iPad and water bottle. Constant compliments whenever I go to coffee shops!",
    avatar: "AK"
  }
];

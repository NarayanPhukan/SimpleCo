// Seed data for BabyyGurll Crochet MERN backend (India Dedicated)
const seedProducts = [
  {
    id: "bgc-cow-01",
    name: "Strawberry Cow Amigurumi Plush",
    category: "plushies",
    categoryName: "Amigurumi Plushies",
    price: 1299,
    originalPrice: 1599,
    image: "/assets/images/strawberry-cow.jpg",
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
    price: 1499,
    originalPrice: 1799,
    image: "/assets/images/tulip-bouquet.jpg",
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
    price: 1199,
    originalPrice: 1399,
    image: "/assets/images/granny-bag.jpg",
    badge: "Trending ✨",
    rating: 4.8,
    reviewsCount: 95,
    leadTime: "Ready to ship (1-2 days)",
    description: "The ultimate aesthetic tote for college, bookstore dates, and yarn trips. Handcrafted by assembling 13 intricate floral granny squares with double-stitched reinforced strap handles for daily durability.",
    specs: {
      yarn: "100% Organic Heavyweight Cotton",
      dimensions: "14.5\" width x 15\" height",
      strapDrop: "11.5 inches comfortable shoulder drop",
      capacity: "Fits iPad, notebooks & water bottle"
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
    price: 2499,
    originalPrice: 2999,
    image: "/assets/images/crochet-cardigan.jpg",
    badge: "Artisan Heirloom ☁️",
    rating: 4.9,
    reviewsCount: 67,
    leadTime: "Custom made in 4-6 days",
    description: "A statement heirloom knitwear. Individually crocheted cloud patchwork squares joined together into an ultra-cozy oversized slouchy silhouette. Features ribbed balloon cuffs and vintage mother-of-pearl buttons.",
    specs: {
      yarn: "Merino Wool & Soft Acrylic Blend",
      fit: "Slouchy Oversized",
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
    price: 899,
    originalPrice: 1099,
    image: "/assets/images/lavender-bunny.jpg",
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
    price: 649,
    originalPrice: 799,
    image: "/assets/images/crochet-coasters.jpg",
    badge: "Cozy Home ☕",
    rating: 4.8,
    reviewsCount: 53,
    leadTime: "Ready to ship",
    description: "Level up your morning chai or coffee with four floral scalloped mug rugs. Naturally absorbent cotton protects your desk surface while looking effortlessly charming.",
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
    price: 799,
    originalPrice: 999,
    image: "/assets/images/bucket-hat.jpg",
    badge: "Summer Vibes 👒",
    rating: 4.7,
    reviewsCount: 74,
    leadTime: "Ready to ship (1-2 days)",
    description: "Protect your hair in style with this whimsical scalloped bucket hat. Woven with breathable lightweight cotton thread, it folds seamlessly into any handbag without losing its wavy structure.",
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
    price: 399,
    originalPrice: 499,
    image: "/assets/images/strawberry-keychain.jpg",
    badge: "Pocket Cute 🍓",
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

const seedUser = {
  name: "Ananya Sharma",
  email: "ananya@babyygurll.in",
  phone: "+91 98765 43210",
  password: "password123",
  memberTier: "VIP Stitcher Club 🧶",
  memberSince: "October 2026",
  savedAddresses: [
    {
      label: "Home (Apartment)",
      recipientName: "Ananya Sharma",
      phone: "+91 98765 43210",
      street: "Flat 402, Blossom Heights, Linking Road, Bandra West",
      city: "Mumbai",
      state: "Maharashtra",
      zip: "400050",
      country: "India",
      isDefault: true
    },
    {
      label: "Design Studio",
      recipientName: "Ananya Sharma",
      phone: "+91 98111 22334",
      street: "Studio 12, Indiranagar 100ft Road",
      city: "Bengaluru",
      state: "Karnataka",
      zip: "560038",
      country: "India",
      isDefault: false
    }
  ]
};

const seedOrders = [
  {
    orderId: "BGC-89241",
    userEmail: "ananya@babyygurll.in",
    dateFormatted: "Oct 8, 2026",
    statusStage: 3,
    statusLabel: "In Transit with BlueDart Express",
    statusCode: "shipped",
    carrier: "BlueDart Express Courier",
    trackingNumber: "TRK-BGC-89241-IN",
    estimatedDelivery: "Oct 12, 2026",
    shippingAddress: {
      recipientName: "Ananya Sharma",
      phone: "+91 98765 43210",
      street: "Flat 402, Blossom Heights, Linking Road, Bandra West",
      city: "Mumbai",
      state: "Maharashtra",
      zip: "400050",
      country: "India"
    },
    paymentMethod: "UPI (Google Pay)",
    subtotal: 1299,
    discount: 195,
    shippingCost: 0,
    tax: 0,
    total: 1104,
    items: [
      {
        id: "bgc-cow-01",
        name: "Strawberry Cow Amigurumi Plush",
        color: "Strawberry Pink",
        quantity: 1,
        price: 1299,
        image: "/assets/images/strawberry-cow.jpg"
      }
    ],
    timeline: [
      {
        stage: "received",
        title: "Order Placed & Yarn Selected",
        time: "Oct 8, 09:30 AM",
        completed: true,
        current: false,
        desc: "Order confirmed! Artisanal milk cotton yarn skeins allocated in studio."
      },
      {
        stage: "crafted",
        title: "Handmade Stitched with Love",
        time: "Oct 9, 02:15 PM",
        completed: true,
        current: false,
        desc: "Over 850 stitches completed by hand. Strawberry leaf horns attached and secured."
      },
      {
        stage: "packed",
        title: "Ribbon Wrapped & Sweet Scented",
        time: "Oct 9, 06:40 PM",
        completed: true,
        current: false,
        desc: "Inspected for stitch perfection, packed in eco-friendly cotton wrap with handwritten note."
      },
      {
        stage: "shipped",
        title: "Dispatched & In Transit",
        time: "Oct 10, 08:20 AM",
        completed: true,
        current: true,
        desc: "Carrier scan: BlueDart Mumbai Hub. Package out for regional transit."
      },
      {
        stage: "delivered",
        title: "Delivered to Doorstep",
        time: "Est. Oct 12, By 6:00 PM",
        completed: false,
        current: false,
        desc: "Handcrafted package will be safely delivered to your doorstep."
      }
    ]
  },
  {
    orderId: "BGC-91402",
    userEmail: "ananya@babyygurll.in",
    dateFormatted: "Oct 10, 2026",
    statusStage: 1,
    statusLabel: "Artisan Handcrafting in Progress",
    statusCode: "crafted",
    carrier: "BlueDart Express Courier",
    trackingNumber: "TRK-BGC-91402-IN",
    estimatedDelivery: "Oct 15, 2026",
    shippingAddress: {
      recipientName: "Ananya Sharma",
      phone: "+91 98765 43210",
      street: "Flat 402, Blossom Heights, Linking Road, Bandra West",
      city: "Mumbai",
      state: "Maharashtra",
      zip: "400050",
      country: "India"
    },
    paymentMethod: "Card (HDFC Bank)",
    subtotal: 799,
    discount: 0,
    shippingCost: 0,
    tax: 0,
    total: 799,
    items: [
      {
        id: "bgc-hat-07",
        name: "Pastel Wavy Brim Bucket Hat",
        color: "Sky & Cream",
        quantity: 1,
        price: 799,
        image: "/assets/images/bucket-hat.jpg"
      }
    ],
    timeline: [
      {
        stage: "received",
        title: "Order Placed & Yarn Selected",
        time: "Oct 10, 11:15 AM",
        completed: true,
        current: false,
        desc: "Order confirmed! Pastel combed cotton threads prepared."
      },
      {
        stage: "crafted",
        title: "Handmade Stitched with Love",
        time: "Oct 10, 03:30 PM",
        completed: true,
        current: true,
        desc: "Master artisan is hand-crocheting the wavy brim."
      },
      {
        stage: "packed",
        title: "Ribbon Wrapped & Sweet Scented",
        time: "Pending Crafting",
        completed: false,
        current: false,
        desc: "Will be wrapped in protective dust bag."
      },
      {
        stage: "shipped",
        title: "Dispatched & In Transit",
        time: "Pending Pickup",
        completed: false,
        current: false,
        desc: "BlueDart courier pickup scheduled."
      },
      {
        stage: "delivered",
        title: "Delivered to Doorstep",
        time: "Est. Oct 15, 2026",
        completed: false,
        current: false,
        desc: "Safe delivery to Mumbai residence."
      }
    ]
  },
  {
    orderId: "BGC-76419",
    userEmail: "ananya@babyygurll.in",
    dateFormatted: "Oct 2, 2026",
    statusStage: 4,
    statusLabel: "Delivered to Doorstep 🏡",
    statusCode: "delivered",
    carrier: "BlueDart Express Courier",
    trackingNumber: "TRK-BGC-76419-IN",
    estimatedDelivery: "Delivered on Oct 5, 2026",
    shippingAddress: {
      recipientName: "Ananya Sharma",
      phone: "+91 98765 43210",
      street: "Flat 402, Blossom Heights, Linking Road, Bandra West",
      city: "Mumbai",
      state: "Maharashtra",
      zip: "400050",
      country: "India"
    },
    paymentMethod: "UPI (Google Pay)",
    subtotal: 1548,
    discount: 0,
    shippingCost: 0,
    tax: 0,
    total: 1548,
    items: [
      {
        id: "bgc-bunny-05",
        name: "Pip the Floppy-Eared Bunny",
        color: "Lavender Mist",
        quantity: 1,
        price: 899,
        image: "/assets/images/lavender-bunny.jpg"
      },
      {
        id: "bgc-coasters-06",
        name: "Petal Blossom Coaster Set (Set of 4)",
        color: "Floral Melody",
        quantity: 1,
        price: 649,
        image: "/assets/images/crochet-coasters.jpg"
      }
    ],
    timeline: [
      {
        stage: "received",
        title: "Order Placed & Yarn Selected",
        time: "Oct 2, 10:00 AM",
        completed: true,
        current: false,
        desc: "Order confirmed."
      },
      {
        stage: "crafted",
        title: "Handmade Stitched with Love",
        time: "Oct 3, 01:20 PM",
        completed: true,
        current: false,
        desc: "Handcrafted and inspected."
      },
      {
        stage: "packed",
        title: "Ribbon Wrapped & Sweet Scented",
        time: "Oct 3, 05:00 PM",
        completed: true,
        current: false,
        desc: "Sealed in signature box."
      },
      {
        stage: "shipped",
        title: "Dispatched & In Transit",
        time: "Oct 4, 09:30 AM",
        completed: true,
        current: false,
        desc: "Shipped via BlueDart."
      },
      {
        stage: "delivered",
        title: "Delivered to Doorstep",
        time: "Oct 5, 02:40 PM",
        completed: true,
        current: true,
        desc: "Handed over to Ananya Sharma. Signed and received."
      }
    ]
  },
  {
    orderId: "BGC-62840",
    userEmail: "ananya@babyygurll.in",
    dateFormatted: "Sep 28, 2026",
    statusStage: 4,
    statusLabel: "Return Completed • Refund Processed via UPI",
    statusCode: "returned",
    carrier: "BlueDart Express Courier",
    trackingNumber: "TRK-RET-62840-IN",
    estimatedDelivery: "Returned on Oct 3, 2026",
    returnReason: "Requested exchange for Sage Green colorway",
    returnDate: "Oct 3, 2026",
    returnStatus: "₹1,099 Refunded to Google Pay UPI ID",
    shippingAddress: {
      recipientName: "Ananya Sharma",
      phone: "+91 98765 43210",
      street: "Flat 402, Blossom Heights, Linking Road, Bandra West",
      city: "Mumbai",
      state: "Maharashtra",
      zip: "400050",
      country: "India"
    },
    paymentMethod: "UPI (Google Pay)",
    subtotal: 1099,
    discount: 0,
    shippingCost: 0,
    tax: 0,
    total: 1099,
    items: [
      {
        id: "bgc-sunflower-bag-03",
        name: "Sunflower Sunshine Shoulder Bag",
        color: "Earthy Oatmeal",
        quantity: 1,
        price: 1099,
        image: "/assets/images/sunflower-tote.jpg"
      }
    ],
    timeline: [
      {
        stage: "received",
        title: "Return Requested",
        time: "Oct 1, 11:00 AM",
        completed: true,
        current: false,
        desc: "Exchange / Return authorized by customer care."
      },
      {
        stage: "crafted",
        title: "Courier Reverse Pickup",
        time: "Oct 2, 02:30 PM",
        completed: true,
        current: false,
        desc: "BlueDart courier picked up sealed parcel from Bandra West."
      },
      {
        stage: "packed",
        title: "Received at Studio Hub",
        time: "Oct 3, 10:15 AM",
        completed: true,
        current: false,
        desc: "Quality verified intact by master craft team."
      },
      {
        stage: "delivered",
        title: "Refund Issued",
        time: "Oct 3, 01:00 PM",
        completed: true,
        current: true,
        desc: "Full refund of ₹1,099 settled to customer's UPI account."
      }
    ]
  }
];

module.exports = { seedProducts, seedUser, seedOrders };

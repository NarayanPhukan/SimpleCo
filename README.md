# 🌸 BabyyGurll Crochet — MERN Stack E-Commerce Platform

A boutique handcrafted crochet e-commerce web application built using the full **MERN** stack:
* **M** — **MongoDB** (Mongoose models for Users, Saved Addresses, Products, and Orders with timelines)
* **E** — **Express.js** (REST API backend with CORS, middleware, and route controllers)
* **R** — **React.js** (Vite + React with Hooks, Context API, glassmorphic boutique UI)
* **N** — **Node.js** (Runtime powering concurrent client & server)

---

## 🚀 Live Services Running

* **Frontend (React / Vite)**: [http://localhost:3000](http://localhost:3000)
* **Backend API (Express & MongoDB)**: [http://localhost:5000](http://localhost:5000)
* **Health Check**: [http://localhost:5000/api/health](http://localhost:5000/api/health)

---

## 🎀 Core Features

1. **Artisanal Product Catalog**:
   * Amigurumi Plushies (*Strawberry Cow*, *Lavender Bunny*), Floral Bouquets (*Everlasting Tulips*), Bags (*Daisy Granny Square Tote*), and Wearables.
   * Filter chips, real-time search, price/rating sorting, and interactive color swatches.
2. **Shopping Cart Drawer**:
   * Off-canvas drawer with interactive free shipping meter (unlocks at ₹999).
   * Pan-India currency (`INR ₹`) and coupon promo engine (`BABYYGURLL`, `CUTIE10`, `FREESHIP`).
3. **User Account & Saved Addresses System**:
   * Pre-configured demo user: `Ananya Sharma` (`ananya@babyygurll.in`).
   * Clean 3-tab Account system: **Orders**, **Address**, and **Logout**.
   * Manage multiple delivery destinations (*Home*, *Design Studio*) with full Indian address and PIN code support.
4. **Order Management & Real-Time Tracking**:
   * Sub-categories: **In-Progress**, **Return**, and **Delivered**.
   * Dedicated **Track Order Live 🚚** action button under each in-progress order.
   * 5-Stage Crafting Timeline: *Order Placed* ➔ *Handmade with Love* ➔ *Ribbon Wrapped* ➔ *In Transit* ➔ *Delivered*.
   * Interactive Simulator (`Advance Next Stage ⏩`) to step through live MongoDB order stages.
   * Return and exchange requests with BlueDart reverse pickup tracking.
5. **Shiprocket Logistics API Integration**:
   * Official Shiprocket API integration for real-time courier serviceability and rates across India (BlueDart, Delhivery, DTDC).
   * Live AWB tracking and automated order dispatching.
6. **Multi-Step Checkout Flow**:
   * Step 1: Select or create saved shipping addresses.
   * Step 2: Choose shipping method (Standard vs Priority Air Express).
   * Step 3: Indian payment options: UPI (Google Pay / PhonePe), RuPay/Visa/MasterCard, Cash on Delivery (COD).
   * Step 4: Celebration screen with confetti cannon burst, unique Order ID generation, and immediate account order tracking.

---

## 🛠️ Project Structure

```
Simple-Website/
├── client/                     # React Frontend (Vite)
│   ├── src/
│   │   ├── components/         # React UI Components
│   │   ├── context/            # AuthContext & CartContext
│   │   ├── services/           # Backend API Client (fetch)
│   │   ├── App.jsx             # Main Application Controller
│   │   └── index.css           # Custom Pastel Boutique Styling
│   ├── public/assets/images/   # High-resolution crochet photography
│   └── vite.config.js          # Dev server with proxy to :5000
├── server/                     # Express & MongoDB Backend
│   ├── config/
│   │   ├── db.js               # MongoDB connection (Atlas/Local + Zero-Config fallback)
│   │   └── memoryStore.js      # Resilient in-memory MongoDB document engine
│   ├── controllers/            # Route controllers (Products, Auth, Orders)
│   ├── models/                 # Mongoose schemas (Product, User, Order)
│   ├── routes/                 # API route definitions
│   ├── data/seedData.js        # Seed catalog, demo user & orders
│   └── server.js               # Express app entry point (:5000)
├── package.json                # Root package with concurrent run scripts
└── .env                        # Environment configuration
```

---

## 💻 Available Scripts

* `npm run dev` — Concurrently runs the Express server on port 5000 and the Vite React client on port 3000.
* `npm run server` — Runs the Express backend API only.
* `npm run client` — Runs the Vite React frontend only.
* `npm run build` — Compiles the React production bundle into `client/dist`.

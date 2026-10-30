# BabyyGurll Admin — Native Android App 🧶

A modern, native Android administrative application built with **Kotlin** and **Jetpack Compose (Material 3)**, created exclusively for the store owner and studio team of **BabyyGurll Crochet Studio**.

The app connects directly to the BabyyGurll backend REST API (`Node.js/Express + MongoDB`), providing instant, real-time control over order fulfillment, product catalog, promotional coupons, and boutique announcement settings from any Android smartphone or tablet.

---

## 🎨 Theme & Design System

The app implements the bespoke boutique color palette:
- **Midnight Navy (`#021861`)**: Primary app bars, surfaces, bold headers, and branding
- **Royal Blue (`#255DCE`)**: Primary buttons, active state indicators, floating action buttons, and accents
- **Crimson (`#DE1110`)**: Promotional highlights, alert badges, and critical destructive actions
- **Soft Ivory (`#F8F9FD`)**: Neutral, clean background for high readability

---

## 🚀 Key Features

### 1. ⚡ Instant Admin Access (No Login Required)
- **Zero Login Friction**: The app opens directly into the studio dashboard with instant access to all orders, products, coupons, and settings.
- **Direct Management**: Eliminates passwords, PIN codes, and authentication gates so you can immediately view and fulfill orders on the go.
- **Dynamic Endpoint Switcher**: Switch backend endpoints anytime between 24/7 cloud backends (Render / Vercel), local emulator (`10.0.2.2:5000`), or local Wi-Fi.

### 2. 📊 Studio Overview & KPIs Dashboard
- **Total Revenue**: Live revenue calculated in Indian Rupees (₹).
- **Total Orders**: All-time order count.
- **Pending Dispatch**: Highlighted counter for orders awaiting fulfillment (`received`, `crafted`, `packed`).
- **Active Products**: Count of in-stock items ready for sale.
- **Average Order Value (AOV)**: Dynamic basket size calculation.
- **Quick Action Cards**: 1-tap shortcuts to pending orders, product creation, and catalog manager.
- **Recent Orders Carousel**: Instant status timeline and customer preview.

### 3. 📦 Orders & Fulfillment Management
- **Status Filtering**: Filter orders by *All*, *Pending Dispatch*, *Order Received*, *Crafted*, *Packed*, *Shipped*, and *Delivered*.
- **Live Search**: Instant lookup by Order ID (`#BBY-...`), Customer Name, Phone, Email, or AWB tracking number.
- **Interactive Fulfillment Advancement**: 1-tap button to progress order to next stage (*Crafted → Packed → Shipped → Delivered*).
- **Courier & Tracking Updater**: Set or update shipping carrier (BlueDart, Delhivery, DTDC, India Post, etc.), AWB tracking number, and estimated arrival date.
- **Customer Contact Actions**: Direct 1-tap buttons to call customer phone or draft an email via native Android intent handlers.
- **Order Timeline Visualization**: Chronological step-by-step visual tracker for each stage.

### 4. 🧵 Product Catalog Management
- **Category Navigation**: Filter pieces by *Plushies*, *Floral & Home*, *Bags & Totes*, *Wearables*, and *Accessories*.
- **1-Tap Stock Switch**: Instantly mark items as **In Stock** or **Sold Out** directly from the list.
- **Product Editor (Create & Edit)**:
  - Title, category, price (₹), and original compare-at price (₹)
  - Promotional badge text (e.g. *Bestseller*, *Limited Edition*, *Handmade*)
  - Handcrafted production lead time
  - Detailed product story and dimensions
  - Built-in photo gallery selector with preview
- **Safe Deletion**: Deletion with confirmation dialog.

### 5. 🏷️ Coupons & Promotions Manager
- View all active boutique promotional codes.
- Supports **Percentage discounts (% off)**, **Flat amounts (₹ off)**, and **Free Shipping** triggers.
- Configure minimum order qualifying values.
- Set codes as **Public** (displayed on the website top bar) or **Secret / VIP**.
- Real-time coupon redemption tracker.

### 6. ⚙️ Store Operations & Settings
- **Live Announcement Bar**: Update top-bar banner headline, CTA badge text (e.g. `CODE: BABYYGURLL FOR 15% OFF`), and toggle visibility on the live storefront with 1 tap.
- **Shipping Rates**: Manage free delivery threshold (e.g. orders over ₹999) and flat rates for Standard and Express shipping.
- **Live PIN Code Serviceability Tool**: Enter any 6-digit Indian postal code to test whether Shiprocket and partner couriers can deliver there.
- **Studio Support Details**: Update public email, phone/WhatsApp, and studio address.
- **Dynamic Server URL**: Change the API endpoint anytime without rebuilding the APK.

### 7. 🔄 Auto-Update Detection System
- **Automatic Release Scanning**: Automatically queries `/api/settings/app-version` on app launch and background refresh to detect newer releases.
- **Version Code & Name Comparison**: Compares installed build (`v1.0.0`, Build 1) against the latest release manifest.
- **Update Dialog & Changelog**: Displays an interactive update modal with full release notes, version progression (`v1.0.0 → v1.1.0`), and direct download/install button.
- **Critical / Mandatory Updates**: Flags critical security or database breaking releases with an enforced update barrier.
- **Manual "Check for Updates"**: Accessible on-demand in the Settings screen with real-time feedback.

### 8. 🛡️ Error Detection & System Diagnostics System
- **Global Uncaught Exception Handler (`AppCrashHandler`)**: Intercepts fatal thread crashes, formats complete stack traces, and records them safely to prevent silent app exits.
- **Network & API Interceptor**: Diagnoses connection timeouts, DNS lookup failures, socket errors, and HTTP 4xx/5xx responses with human-readable troubleshooting guidance.
- **In-Memory & Persistent `ErrorLogManager`**: Tracks recent warning, error, and informational events with timestamps, tags, and stack traces.
- **Live System Health Monitor**:
  - **Database Status**: MongoDB connection state and latency (ms).
  - **Razorpay Status**: Gateway mode (Live vs Test) and credential verification.
  - **Shiprocket Status**: Shipping API account connectivity check.
  - **Firebase Identity**: Real-time status for project `babygurl-crochet-dev`.
- **1-Tap Diagnostics Actions**:
  - **Copy Report**: Formats a complete device, OS, network, and error telemetry report to the Android system clipboard.
  - **Clear Logs**: 1-tap reset for both client-side and server-side diagnostics logs.
  - **Self-Test Diagnostics**: Instant verification of all core subsystems.

---

## 🛠️ Architecture & Tech Stack

| Component | Technology |
| :--- | :--- |
| **Language** | Kotlin 2.0.21 |
| **UI Framework** | Jetpack Compose (Material 3) |
| **Architecture** | MVVM (Model-View-ViewModel) + StateFlow & SharedFlow |
| **Networking** | Retrofit 2.11.0 + OkHttp 4.12.0 with Logging Interceptor |
| **Serialization** | Google Gson |
| **Image Loading** | Coil 2.7.0 (Compose AsyncImage with caching) |
| **Navigation** | AndroidX Navigation Compose 2.8.4 |
| **Min SDK** | Android 8.0 (API Level 26) |
| **Target SDK** | Android 15 (API Level 35) |

---

## 📂 Project Structure

```
android-admin/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── res/
│       │   ├── values/ (strings.xml, colors.xml, themes.xml)
│       │   ├── xml/ (backup and extraction rules)
│       │   ├── drawable/ (vector assets & launcher backgrounds)
│       │   └── mipmap-anydpi-v26/ (adaptive icons)
│       └── java/com/babyygurll/admin/
│           ├── BabyyGurllAdminApp.kt
│           ├── MainActivity.kt
│           ├── data/
│           │   ├── api/
│           │   │   ├── AdminApiService.kt
│           │   │   └── NetworkModule.kt
│           │   ├── model/
│           │   │   └── Models.kt
│           │   └── repository/
│           │       ├── AdminRepository.kt
│           │       └── PreferencesManager.kt
│           └── ui/
│               ├── navigation/
│               │   └── Screen.kt
│               ├── theme/
│               │   ├── Color.kt
│               │   ├── Theme.kt
│               │   └── Type.kt
│               ├── viewmodel/
│               │   └── AdminViewModel.kt
│               └── screens/
│                   ├── ServerConfigDialog.kt
│                   ├── LoginScreen.kt
│                   ├── DashboardScreen.kt
│                   ├── OrdersScreen.kt
│                   ├── OrderDetailScreen.kt
│                   ├── ProductsScreen.kt
│                   ├── ProductEditScreen.kt
│                   ├── CouponsScreen.kt
│                   └── SettingsScreen.kt
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/
│       └── gradle-wrapper.properties
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew
└── gradlew.bat
```

---

## 📱 How to Run in Android Studio

1. **Launch Android Studio**:
   - Open Android Studio.
   - Choose **File > Open...** (or *Open* from the welcome screen).
   - Select the directory:
     ```
     /home/zerosync/Documents/Simple-Website/android-admin
     ```

2. **Gradle Sync**:
   - Android Studio will automatically recognize the Gradle build files and sync the dependencies.
   - If prompted for a JDK, select **Java 17** (or Java 21).

3. **Connecting to your Local Backend**:
   - Make sure your Node.js backend server is running (`npm start` or `node server/server.js` on port `5000`).
   - **When running on Android Emulator**:
     - The default URL is already configured to:
       ```
       http://10.0.2.2:5000/
       ```
       *(In Android Emulator, `10.0.2.2` aliases your development computer's `localhost`).*
   - **When running on a Physical Android Device (connected via USB or Wi-Fi)**:
     - Connect your phone and computer to the same Wi-Fi network.
     - Tap **Server Settings** (gear icon) on the login screen of the app.
     - Change the URL to your computer's local Wi-Fi IP:
       ```
       http://<YOUR_LOCAL_IP>:5000/
       ```
       *(e.g., `http://192.168.1.15:5000/`)*.

4. **Sign In**:
   - Enter your store admin password to enter the Studio Portal.


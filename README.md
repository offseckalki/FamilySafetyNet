# 🛡️ Family Safety Net

Family Safety Net is a consent-based personal security and family monitoring system designed to provide real-time visibility, location tracking, and emergency awareness for connected family devices.

## 🚀 Features

### 📱 Android Client
- Real-time GPS tracking
- Background location updates
- Firebase integration
- Secure device communication
- Lightweight and optimized design

### 🖥️ Admin Dashboard
- Live device monitoring
- Realtime location visualization
- Connected device management
- Map-based tracking interface
- Firebase-powered backend support

### 🔒 Security Features
- Consent-based tracking
- Secure authentication
- Controlled admin access
- Realtime synchronization

---

# 🏗️ Tech Stack

## Frontend
- Android (Java/Kotlin)
- HTML/CSS/JavaScript

## Backend
- Firebase Realtime Database
- Firebase Authentication
- Firebase Hosting

## Deployment
- GCP E2 Instance
- Vercel
- Render

---

# 📂 Project Structure

```bash
family-safety-net/
│
├── android-client/
│   ├── app/
│   ├── services/
│   └── firebase/
│
├── admin-dashboard/
│   ├── public/
│   ├── src/
│   └── components/
│
├── backend/
│   ├── api/
│   └── firebase-functions/
│
└── README.md
```

---

# ⚙️ Installation

## Clone Repository

```bash
git clone https://github.com/yourusername/family-safety-net.git
cd family-safety-net
```

---

## 📱 Android Client Setup

Open the `android-client` folder in Android Studio.

Build the project:

```bash
./gradlew build
```

---

## 🖥️ Admin Dashboard Setup

```bash
cd admin-dashboard
npm install
npm run dev
```

---

# 🔥 Firebase Setup

1. Create a Firebase project
2. Enable:
   - Authentication
   - Realtime Database
   - Cloud Messaging
3. Add Android app credentials
4. Replace Firebase config values

Example configuration:

```json
{
  "apiKey": "YOUR_API_KEY",
  "authDomain": "YOUR_AUTH_DOMAIN",
  "databaseURL": "YOUR_DATABASE_URL",
  "projectId": "YOUR_PROJECT_ID",
  "storageBucket": "YOUR_STORAGE_BUCKET",
  "messagingSenderId": "YOUR_MESSAGING_SENDER_ID",
  "appId": "YOUR_APP_ID"
}
```

---

# 📍 Usage

1. Install the Android client on authorized devices
2. Login using configured credentials
3. Launch the admin dashboard
4. Monitor connected devices in realtime

---

# ⚠️ Ethical Usage

This project is intended strictly for:
- Family safety
- Personal device monitoring
- Consent-based tracking

Do NOT use this project for:
- Unauthorized surveillance
- Non-consensual tracking
- Illegal monitoring activities

Always comply with local privacy laws and regulations.

---

# 🛠️ Future Improvements

- SOS emergency alerts
- Geofencing
- Push notifications
- End-to-end encryption
- Device health monitoring
- Battery optimization

---

# 👨‍💻 Author

**Drash Tyagi**  
Offensive Security Enthusiast & Developer

---

# 📜 License

This project is licensed under the MIT License.

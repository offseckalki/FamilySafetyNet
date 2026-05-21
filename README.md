Family Safety Net

A personal family security and tracking system designed for consent-based monitoring, emergency awareness, and real-time visibility of connected family devices.

Overview

Family Safety Net is a security-focused platform consisting of:

Android Client App – Installed on family devices
Admin Dashboard – Used to monitor connected devices
Firebase Backend – Handles authentication, realtime data, and storage

The goal of the project is to provide a centralized system for:

Real-time GPS tracking
Device monitoring
Emergency visibility
Secure family connectivity
Features
Android Client
Real-time location updates
Firebase integration
Background tracking support
Device identification
Secure communication with backend
Lightweight and battery-conscious implementation
Admin Dashboard
Live device monitoring
Device management panel
Realtime location updates
Map-based visualization
Connected device overview
Firebase-powered backend communication
Security Focus
Consent-based usage
Secure Firebase communication
Authentication support
Controlled device access
Tech Stack
Frontend
Android (Java/Kotlin)
HTML/CSS/JavaScript (Dashboard)
Backend
Firebase Realtime Database
Firebase Authentication
Firebase Hosting
Deployment
GCP E2 Instance
Vercel (optional frontend deployment)
Render (optional backend deployment)
Firebase Configuration
{
  "apiKey": "YOUR_API_KEY",
  "authDomain": "YOUR_AUTH_DOMAIN",
  "databaseURL": "YOUR_DATABASE_URL",
  "projectId": "YOUR_PROJECT_ID",
  "storageBucket": "YOUR_STORAGE_BUCKET",
  "messagingSenderId": "YOUR_MESSAGING_SENDER_ID",
  "appId": "YOUR_APP_ID"
}
Project Structure
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
Installation
Android Client
git clone https://github.com/yourusername/family-safety-net.git
cd android-client

Open the project in Android Studio and run:

./gradlew build
Admin Dashboard
cd admin-dashboard
npm install
npm run dev
Firebase Setup
Create a project in Firebase
Enable:
Authentication
Realtime Database
Cloud Messaging
Add Android app credentials
Replace Firebase configuration values in the project
Usage
Install the Android client on authorized devices
Login using configured credentials
Open the admin dashboard
Monitor connected devices in realtime
Ethical Usage

This project is intended strictly for:

Family safety
Personal device monitoring
Consent-based tracking

Do not use this project for:

Unauthorized surveillance
Stalking
Non-consensual monitoring
Illegal activities

Always comply with local laws and privacy regulations.

Future Improvements
End-to-end encrypted communication
SOS emergency system
Geofencing alerts
Battery optimization improvements
Push notification system
Device health monitoring
Author

Drash Tyagi
Offensive Security Enthusiast & Developer

License

This project is licensed under the MIT License.


# 💬 Secure-Dm – Realtime Chat App

Secure-Dm is a KMP(kotlin multi-platform) project, full-stack chat application designed for reliable and efficient communication. Built with a strong focus on design, performance, data consistency, and real-time messaging, Secure-Dm supports offline-first architecture and scalable backend integrations.

---



## 🖼️ Screenshots
### DESKTOP

| Home        | Chat Interface| Settings         |
|-----------------------|-------------------------|------------------------|
| ![Home](https://github.com/user-attachments/assets/ec8bb0d3-2f8c-456b-9a86-aa5dc34f471f) | ![Chat interface](https://github.com/user-attachments/assets/0b6e9d1e-e9a9-4d91-b84f-5aaeab4e1820) | ![Settings](https://github.com/user-attachments/assets/d443923f-d8ba-4f70-ac4d-51213da8dd94) |


---

### ANDROID

| Home        | Chat Interface| Settings         |
|-----------------------|-------------------------|------------------------|
| ![Home](https://github.com/user-attachments/assets/affcaebb-f612-4c02-bc78-49a8bb332958) | ![Chat Interface](https://github.com/user-attachments/assets/85eeb1d9-4725-4b1d-82c8-6306f3776e6a) | ![settings](https://github.com/user-attachments/assets/8b49f790-6837-44ce-be2a-2458d7779433) |


---


## ✨ Features

- ✅ **Realtime Messaging** using **Supabase Realtime** and **PostgreSQL**
- ✅ **Realtime Notification** using **Firebase** and **supabase edge functions** 
- ✅ **Link Previews** using **Ksoup** Prefetching metadata from remote urls and display necessary details
- 📥 **Message Caching** with automatic syncing
- 🔁 **Retry Queue** for failed messages
- 📤 **Media Support** – Images, Audio, Documents
- 🔐 **Secure Authentication** with providers like Google
- 🌐 **Kotlin Multiplatform** compatible (Android/Desktop and IOS)
- 🔧 Background Workers via **WorkManager** and chaining
- 🔄 Smart Workflows with conditional enqueuing

---

## 🛠️ Tech Stack

| Layer          | Technology                                   |
|----------------|-----------------------------------------------|
| Frontend       | Kotlin, Jetpack Compose            |
| Backend        | Supabase (PostgreSQL, Realtime, Storage,FCM)     |
| Desktop Client | Kotlin + CMP                    |
| Auth           | Firebase Authentication (Email/Password, Google)      |
| Media Storage  | Supabase Storage                             |

---

## 🚧 Offline Strategy

- All messages are cached locally
- Messages are queued if offline and automatically synced
- WorkManager handles retries and dependency chaining

---

## 🔊 Audio Integration

- Records audio via Android's `MediaRecorder`  
- Encodes using `AMR_NB` or custom encoder
- Playback via VLCJ (desktop) with remote streaming support

---

## 🔐 Notification Flow(Android only)

- supabase edge function detects new messages and are carried out by Firebase Api.
- Android client then recieves the payload and notifies user accordingly.

---

## 📈 Future Enhancements

- Read receipts and typing indicators
- End-to-end encryption
- Video messages
---

# Custom Video Downloader App  

The Custom Video Downloader App is a lightweight Android application that allows users to download videos efficiently. It includes advanced features such as pause, resume, and speed monitoring, making it ideal for handling large files seamlessly.

## Features  

- **Video Downloading**: Users can download videos by providing a video link.  
- **Pause and Resume Downloads**: Seamlessly pause and resume video downloads without losing progress.  
- **Speed Monitoring**: Real-time display of download speed and estimated time remaining.  
- **Progress Notifications**: Displays download progress and provides actionable buttons (Pause, Resume, Cancel) via notifications.  
- **Multiple Actions Support**: Users can control downloads directly from the notification panel (pause, resume, and cancel).  

---

## Key Technologies and Libraries Used  

- **Kotlin**: Primary programming language for the app.  
- **OkHttp**: Handles HTTP requests, including range-based requests for resuming downloads.  
- **Android Service**: Manages background downloading to ensure the download continues even if the app is minimized.  
- **Notifications**: Displays download progress and controls in the notification bar.  
- **File Handling**: Manages file streams to write the downloaded content to external storage.  

---

## How It Works  

1. **Starting a Download**:  
   - The download starts when the user provides a valid video URL.
   - A background service (`DownloadService`) handles the download, ensuring it runs even if the app is minimized.

2. **Pause and Resume Downloads**:  
   - The app uses HTTP range-based headers to support pause and resume functionality.
   - Users can pause, resume, or cancel downloads directly from the app or the notification.

3. **Real-Time Speed Monitoring and Progress**:  
   - The app calculates the current download speed and displays the estimated time remaining.
   - Users can see the download progress and speed updates in the notification.

---

## Getting Started  

Follow these steps to set up and run the Custom Downloader app locally:

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/Ahmed-Ashraf24/VideoDownloader.git


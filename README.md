CardioDataViewer
================

CardioDataViewer is an Android application for making available cardiologically relevant data, eg. ECG data. 
Intended to be used with wearable eHealth devices to gather empirical data in the use of them and possibly unveal issues not considered before.

## Work-in-Progress
- find, evaluate and implement interface to library for displaying dynamic graphs
  - view sine wave in Android Application for initial testing purposes (test performance of dynamic graph viewing)

## Lessons Learned
- Most libraries found for displaying graphs in Android were not suited for displaying dynamic graphs.
- Virtual Android devices emulated by the Android SDK had noticeable performance issues, especially with regards to their FPS rate. This was especially true if a non-optimized environment was used and could be improved slightly by using eg. Intel® Hardware Accelerated Execution Manager. Still, as the main goal was to test display performance of graph libraries, the lack of display responsiveness in emulated devices proved to be a considerable obstacle.

## Features
- Must-Have
  - Display of Single-Channel ECG
  - Display of previously recorded data
  - Interface option to start and stop measurement
  - Communication with microcontroller board via sockets or REST-API
- Important
  - Dynamic display of ECG data while measuring
  - wireless connection (eg. Bluetooth)
- Nice-to-Have
  - Mulit-Channel ECG
  - Signal graph remains static relative to display (like professional ECG)
  - Export functionality
  - Display of additional sensor values (pulse oximeter, accelerometer etc.)

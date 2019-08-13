# Depression Detection using Speech Analysis (Mobile App)
## Introduction
This project comprises 2 main parts - an Android application and a backend server. 

The Android application provides a frontend for users to record and visualise real-time audio. 
The prediction will be displayed on the screen after processing on the backend server is complete.
The backend server determines the prediction or probability of the user being depressed and returns the value to the application.

In this GitHub repository, the code for the Android application, backend server and prototypes, the audio files, spectrograms can be found. 
Interim and final presentations and reports can be found in the repository as well.

## AndroidApp
Contains code necessary to run Android application.
### Instructions for Use
Step 1: Open project in Android Studio.

Step 2: Open restClient.java.

Step 3: Open a Command Prompt window and run _ipconfig_. 

Step 4: Copy the IPv4 address, and replace _private static final String BASE_URL = "http://172.17.41.97:5000"_ with _private static final String BASE_URL = "http://IPv4-address:5000"_ in restClient.java.

Step 5: Run Android application on Android device.


## BackendServer
Contains code necessary to run backend server.
### Instructions for Use
Step 1: Open app.py in a text editor (e.g. Sublime Text or Notepad++ or VS Code).

Step 2: Open a Command Prompt window and run _ipconfig_. 

Step 3: Copy the IPv4 address, and replace _app.run(host='172.41.17.97')_ with _app.run(host='IPv4-address')_ in app.py.

Step 4: Run python app.py in Anaconda Prompt.


## FilteredImages
Contains all spectrograms for each participant in ParticipantsAudio folder.


## FilteredImages2
Contains one spectrogram for each participant in ParticipantsAudio folder.


## Notebooks
Contains Jupyter Notebooks which were used in prototyping/testing phase. 

Note that code for accessing directories may differ based on location of each folder.


## Reports
Contains Interim and Final Report.


## Slides
Contains Interim and Final Presentation.


## Credits
© Kellie Sim and Tan Hong Ray 2019 

Ngee Ann Polytechnic

Product Design and Development (ESFYP) PS06

Supervisors: Dr Harry Nguyen and Dr Pham The Hanh

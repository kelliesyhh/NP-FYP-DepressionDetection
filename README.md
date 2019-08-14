# Depression Detection using Speech Analysis (Mobile App)
## Introduction
This project comprises 2 main parts - an Android application and a backend server. 
The Android application provides a frontend for users to record and visualise real-time audio. 
The prediction will be displayed on the screen after processing on the backend server is complete.
The backend server determines the prediction or probability of the user being depressed and returns the value to the application.
In this GitHub repository, the code for the Android application, backend server and prototypes, the audio files, spectrograms can be found. 
Interim and final presentations and reports can be found in the repository as well.

## AndroidApp
### Instructions for Use
Step 1: Open project in Android Studio.

Step 2: Open restClient.java.

Step 3: Open a Command Prompt window and run ipconfig. 

Step 4: Copy the IPv4 address, and replace private static final String BASE_URL = "http://172.17.41.97:5000" with private static final String BASE_URL = "http://IPv4-address:5000" in restClient.java

Step 5: Run Android application on Android device


## BackendServer
### Instructions for Use
Step 1: Open app.py in a text editor (e.g. Sublime Text or Notepad++ or VS Code).

Step 2: Open a Command Prompt window and run ipconfig. 

Step 3: Copy the IPv4 address, and replace app.run(host='172.41.17.97') with app.run(host='IPv4-address') in app.py.

Step 4: Run python app.py in Anaconda Prompt


## FilteredImages
Contains all spectrograms for each participant in ParticipantsAudio folder


## FilteredImages2
Contains one spectrogram for each participant in ParticipantsAudio folder


## ParticipantsAudio
Contains .wav files recorded by each participant


## Notebooks
Contains Jupyter Notebooks which were used in prototyping/testing phase


## Credits
© Kellie Sim and Tan Hong Ray 2019 

Ngee Ann Polytechnic

Product Design and Development (ESFYP) PS06

Supervisors: Dr Harry Nguyen and Dr Pham The Hanh

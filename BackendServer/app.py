from flask import Flask, request, jsonify, flash, redirect, url_for, session
from flask import send_from_directory
import sklearn
from sklearn.preprocessing import StandardScaler
from sklearn.linear_model import LogisticRegression
from sklearn.model_selection import train_test_split
from scipy.io import arff
import numpy as np
import csv
import keras
from keras.models import load_model
import pandas as pd
import tensorflow as tf
# from flask.ext.session import Session
import os
from werkzeug import secure_filename
# from google.cloud import storage
import sys
import wave
import subprocess
import pickle
from numpy.lib import stride_tricks
from PIL import Image
import scipy.io.wavfile as wav
import librosa  
import errno
import glob
from flask import send_file
import noisereduce as nr
import soundfile as sf
import io
import matplotlib.pyplot as plt
import urllib.request
from noisereduce.generate_noise import band_limited_noise
import IPython


cwd = os.getcwd()
UPLOAD_FOLDER = os.path.join(os.getcwd(), "uploads")
ALLOWED_EXTENSIONS = set(['txt', 'png', 'jpg', 'jpeg', 'pcm', 'wav'])
modelDir = os.path.join(cwd, "model")
# OPENSMILE_MODEL = os.path.join(modelDir, "DepressionAnalysisModel - OpenSmileData.h5")
DP_MODEL = os.path.join(modelDir, "CNN-NR-KF.h5")
# OPENSMILE_MODEL = os.path.join(modelDir, "OpnSmModel.sav")
# OPENSMILE_SCALAR = os.path.join(modelDir, "scaler.sav")

MODEL = load_model(DP_MODEL)

# OPENSMILE_DIR = os.path.join(cwd, "../../opensmile-2.3.0")
# OPENSMILE_CONFIG_DIR = os.path.join(OPENSMILE_DIR, "config")
# OPENSMILE_CONFIG_FILE = os.path.join(OPENSMILE_CONFIG_DIR, "IS09_emotion.conf")

# SCRIPT_DIR = os.path.join(cwd, "script")
# SCRIPT_FILE = os.path.join(SCRIPT_DIR, "IS09Extract.sh")

# print(OPENSMILE_MODEL)

# processedDir = os.path.join(cwd, "processed")
# sampletestCSV1 = os.path.join(processedDir, "SingleRow.csv")
# sampletestCSV2 = os.path.join(processedDir, "SingleRow2.csv")

MODEL._make_predict_function()
graph = tf.get_default_graph()

# loaded_model = pickle.load(open(DP_MODEL, 'rb'))
# scaler = pickle.load(open(OPENSMILE_SCALAR, 'rb'))

def sortFirst(val):
    return val[0]

def getImagesDir(mainDirectory):
    # ImgDict = {}
    ImgList = []
    
    # for folder in os.listdir(mainDirectory):
    theDir = mainDirectory
    images = os.listdir(theDir)
    listOfImgDir = []
    for img in images:
#             listOfImgDir.append(Image.open(os.path.join(theDir, img)))
        listOfImgDir.append(os.path.join(theDir, img))
#         print(listOfImgDir)
#         print(images)
    # folderName = folder.split('_')
    # ImgDict[folderName[0]] = listOfImgDir
    ImgList.append(listOfImgDir)
    imgList = ImgList.sort(key = sortFirst, reverse = False)
    return ImgList

def importImages(listOfImgDir):
    myFolder = []
    for folder in listOfImgDir:
        myImages = []
        for image in folder:
            myImages.append(np.array(Image.open(image)))
        myFolder.append(np.array(myImages))
    return myFolder

def get_prediction(data):
    # result = np.loadtxt(csv_file, delimiter=",")
    # result = result.reshape(1, result.shape[0])
    # global graph

    with graph.as_default():
        predictor = MODEL.predict(data, verbose=1)

    # res = predictor.argmax(axis=-1)
    res = predictor

    tf.keras.backend.clear_session()

    return res

def proc_pcm(pcm_file):
    with open(pcm_file, 'rb') as pcmfile:
        pcmdata = pcmfile.read()
    with wave.open(pcm_file.replace(".pcm", ".wav"), 'wb') as wavfile:
        wavfile.setparams((1, 2, 44100, 0, 'NONE', 'NONE'))
        wavfile.writeframes(pcmdata)
    return pcm_file.replace(".pcm", ".wav")

def processDP(file, filepath):
    print("processDP Function")
    print("file: " + str(file))
    print("filepath: " + str(filepath))
    
    # load data
    rate, data = wav.read(file)
    data = data / 32768
    
    # add noise
    noise_len = 7 # seconds
    noise = band_limited_noise(min_freq=400, max_freq=8000, samples=len(data), samplerate=rate)*10
    noise_clip = noise[:rate*noise_len]
    audio_clip_band_limited = data+noise
    
    # denoise
    noise_reduced = nr.reduce_noise(audio_clip=audio_clip_band_limited, noise_clip=noise_clip, verbose=False)
    #newfile = str(file) + "new"
    #wav.write(file, rate, noise_reduced)
    sf.write(file, noise_reduced, rate)
    print("audio denoised, file: " + str(file))

    fileName = file.split('\\')
    fileName = fileName[len(fileName) - 1]
    fileName = fileName.split('.wav')[0]

    # inputDir = filepath
    outputDir = os.path.join(filepath, "imgOut")
    
    os.mkdir(outputDir)
    print("outputDir: " + str(outputDir))

    samples, samplerate = librosa.load(file, sr=8000)
    #INSERT LOOP FOR FRAMING 10s DATA
    totalSamples = len(samples)
    blockPerSegment = 80000 #10s
    cNumOfSegments = int(-(-totalSamples//blockPerSegment))
    fNumOfSegments = int(totalSamples//blockPerSegment)
    segments = []
    counter = 0

    for i in range(fNumOfSegments):
    #     segments.append(1)
        segments.append({(i)*blockPerSegment + (1 if i > 0 else 0), (i+1)*blockPerSegment})

    # segments
    for x, y in segments:
        if(x == 0):
            shortSamples = samples[x:y]
        else:
            shortSamples = samples[y:x]        
        
        frameSize = 200 #25ms
        hopsize = 80 #10ms
        overlap = 120 #15ms
        
        win = np.hanning(frameSize) #Forcuses on the peak (in the middle)
        s = np.append(np.zeros(overlap), shortSamples)
        # cols for windowing
        cols = np.ceil((len(s) - frameSize) / hopsize) + 1
        # zeros at end (thus samples can be fully covered by frames)
        s = np.append(s, np.zeros(overlap))
        
        frames = stride_tricks.as_strided(s, shape=(int(cols), frameSize),
                                          strides=(s.strides[0]*hopsize,
                                                   s.strides[0])).copy()
        frames *= win
        rfft = np.fft.rfft(frames)
        timebins, freqbins = np.shape(rfft)
        scale = np.linspace(0, 1, freqbins) ** 1
        scale *= (freqbins-1)/max(scale)
        scale = np.unique(np.round(scale))
        
        # create spectrogram with new freq bins
        newspec = np.complex128(np.zeros([timebins, len(scale)]))
        for i in range(0, len(scale)):
            if i == len(scale)-1:
                newspec[:, i] = np.sum(rfft[:, int(scale[i]):], axis=1)
            else:
                newspec[:, i] = np.sum(rfft[:, int(scale[i]):int(scale[i+1])], axis=1)
                
        # list center freq of bins
        allfreqs = np.abs(np.fft.fftfreq(freqbins*2, 1./8000)[:freqbins+1])
        
        freqs = []
        for i in range(0, len(scale)):
            if i == len(scale)-1:
                freqs += [np.mean(allfreqs[int(scale[i]):])]
            else:
                freqs += [np.mean(allfreqs[int(scale[i]):int(scale[i+1])])]
                
        ims = 20.*np.log10(np.abs(newspec)/10e-6)  # amplitude to decibel
        
        ims = np.transpose(ims)
        ims = np.flipud(ims)  # weird - not sure why it needs flipping
        
#       image = Image.new()
        image = Image.fromarray(ims)
        image = image.convert('L')  # convert to grayscale
        if(x == 0):
            image.save(outputDir + "/" + fileName + "("+ str(x) + "-"+ str(y) + ")"+".png")
        else:
            image.save(outputDir + "/" + fileName + "("+ str(y) + "-"+ str(x) + ")"+".png")
            
        image.close()

    return outputDir

# print(result2)


# print(get_prediction(sampletestCSV1))


# def processOpenSMILE(file, dir):
#
#
#
#     return arffFile

# y_classes = predictor.argmax(axis=-1)
# print(y_classes)

app = Flask(__name__)
# sess = Session()

def allowed_file(filename):
    return '.' in filename and \
        filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS

#404 Error
@app.errorhandler(404)
def page_not_found(e):
    # your processing here
    res = jsonify({"Error 404": str("%s" % e)})
    res = flash("%s", e)
    return res

# root
@app.route("/")
def index():
    """
    this is a root dir of my server
    :return: str
    """
    return "INVALID REQUEST"


# GET
@app.route('/users/<user>')
def hello_user(user):
    """
    this serves as a demo purpose
    :param user:
    :return: str
    """
    return jsonify({"response": str("Hello %s!" % user)})   
        
@app.route('/users/<userID>/analysis/<sessionID>', methods=['GET', 'POST'])
def get_depression_prediction(userID, sessionID):
    if request.method == 'POST':
        int_message = 1
        if 'file' not in request.files:
            flash("No file part")
            return jsonify({"Error": "No file part"})
        file = request.files['file']
        if file.filename == '':
            flash('No selected file')
            return jsonify({"Error": "No File Found"})
        if file and allowed_file(file.filename):
            filename = secure_filename(file.filename)
            userDir = os.path.join(app.config['UPLOAD_FOLDER'], userID)
            if not os.path.exists(userDir):
                os.makedirs(userDir)
            sessionDir = os.path.join(userDir, sessionID)
            if not os.path.exists(sessionID):
                os.makedirs(sessionDir)
            filepath = os.path.join(sessionDir, filename)
            if os.path.isfile(os.path.join(sessionDir, filename)):
                count = 1
                while os.path.isfile(os.path.join(sessionDir, filename + str(count))):
                    count += 1
                filepath = os.path.join(sessionDir, filename + str(count))
            file.save(filepath)
            if('.pcm' in filepath):
                wav = proc_pcm(filepath)
            else:
                wav = filepath    

            # testValues = processOpenSMILE(wav, sessionDir)
            testValues = processDP(wav, sessionDir)

            print("testValues Returns: " + str(testValues))

            vImagesDir = getImagesDir(testValues)
            vImages = importImages(vImagesDir)
            vImagesNP = np.array(vImages)

            v = []

            for x in vImages:
                for each in x:
                    v.append(each)

            np_v = np.array(v)
            newNPX_v = np_v.reshape(np_v.shape[0], 101, 1000, 1)

            result = get_prediction(newNPX_v)

            totalPercent = (sum(result) / len(result)) * 100
            # result = get_prediction_data(testValues).argmax(axis=-1)

            #SAMPLE PREDICTION
            # result = get_prediction(sampletestCSV1).argmax(axis=-1)
            print("Percentage: " + str(totalPercent[0]))

            return jsonify({"Prediction": str(totalPercent[0])})
            # ProcessUploadedFile
            # LoadModel
            # Predict
            # return jsonify({"Success": str(url_for('uploaded_file', filename=filename))})


# @app.route('/googleCloud/Test', methofds=['POST'])
# def post_google_cloud_test():
    # upload_blob(fyp-depressiondetect.appspot.com, )



# def upload_blob(bucket_name, source_file_name, destination_blob_name):
#     """Uploads a file to the bucket."""
#     storage_client = storage.Client()
#     bucket = storage_client.get_bucket(bucket_name)
#     blob = bucket.blob(destination_blob_name)
#
#     blob.upload_from_filename(source_file_name)
#
#     print('File {} uploaded to {}.'.format(
#         source_file_name,
#         destination_blob_name))


# running web app in local machine
if __name__ == '__main__':
    app.debug = True
    app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER
    app.secret_key = 'super secret key'
    app.config['SESSION_TYPE'] = 'filesystem'
    # sess.init_app(app)
    # app.run(host='0.0.0.0') #original
    # app.run(host='192.168.1.11') #home
    app.run(host='172.17.46.52') #NP wifi
    # app.run(host='0.0.0.0', port=5000)

#
# @app.route('/')
# def hello_world():
#     return 'Hello World!'


# if __name__ == '__main__':
#     app.run()


from __future__ import absolute_import, division, print_function, unicode_literals

# TensorFlow and tf.keras
import tensorflow as tf
from tensorflow import keras

# Helper libraries
import numpy as np
import matplotlib.pyplot as plt
import pathlib
import os
import cv2
import random

data_dir = "dataset-resized"

categories = ['cardboard','glass','metal','paper','plastic','trash']
IMG_SIZE = 224

training_data = []
def create_training_data():
    for category in categories:
        path = os.path.join(data_dir, category)
        print(path)
        class_num = categories.index(category)
        for img in os.listdir(path):
            try:        
                img_array = cv2.imread(os.path.join(path,img), cv2.IMREAD_COLOR)
                new_array = cv2.resize(img_array, (IMG_SIZE, IMG_SIZE))
                training_data.append([new_array, class_num])
            except Exception as e:
                pass

create_training_data()
print(len(training_data))

# training_data = np.array(training_data)
random.shuffle(training_data)

# print(training_data.shape)
train_images = []
train_labels = [] 

for image, label in training_data:
    train_images.append(image)
    train_labels.append(label)

train_images = np.array(train_images).reshape(-1, IMG_SIZE, IMG_SIZE)
train_labels = np.array(train_labels)

print(train_labels.shape)
print(train_images.shape)
model = keras.Sequential([
    keras.layers.Flatten(input_shape=(IMG_SIZE,IMG_SIZE)),
    keras.layers.Dense(128, activation='relu'),
    keras.layers.Dense(6, activation='softmax')
])

model.compile(optimizer='adam',
    loss='sparse_categorical_crossentropy',
    metrics=['accuracy'])

model.fit(train_images, train_labels, epochs=10)

# prediction = model.predict([train_images[2]])

# print(prediction[0])

# prediction = model.predict(test_images)

# data_dir = tf.keras.utils.get_file(origin='https://github.com/garythung/trashnet/blob/master/data/dataset-resized.zip',fname='dataset-resized', untar='True' )

# data_dir = pathlib.Path(data_dir)

# image_count = len(list(data_dir.glob('*/*.jpg')))

# print(image_count)
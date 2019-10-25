from __future__ import absolute_import, division, print_function, unicode_literals

import tensorflow as tf

from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Dense, Conv2D, Flatten, Dropout, MaxPooling2D
from tensorflow.keras.preprocessing.image import ImageDataGenerator

import os
import numpy as np
import matplotlib.pyplot as plt

batch_size = 128
epochs = 150
IMG_HEIGHT = 150
IMG_WIDTH = 150

data_dir = os.path.join(os.path.dirname('dataset-resized'), 'dataset-resized')

train_dir = os.path.join(data_dir, 'train')
validate_dir = os.path.join(data_dir, 'validate')
categories = ['cardboard','glass','metal','paper','plastic','trash']

total_train = 0
total_validate = 0


for category in categories:
    path = os.path.join(data_dir, 'train/' + category)
    total_train = total_train + len(os.listdir(path))
    # total_train = total_train + len(os.listdir(path))
    print("total " + category + " images: ", len(os.listdir(path)) )

for category in categories:
    path = os.path.join(data_dir, 'validate/' + category)
    total_validate = total_validate + len(os.listdir(path))
    # total_train = total_train + len(os.listdir(path))
    print("total " + category + " images: ", len(os.listdir(path)) )

print(total_train)

train_image_generator = ImageDataGenerator(rescale=1./255) # Generator for our training data

validate_image_generator = ImageDataGenerator(rescale=1./255) # Generator for our training data


train_data_gen = train_image_generator.flow_from_directory(batch_size=batch_size,
                                                           directory=train_dir,
                                                           shuffle=True,
                                                           target_size=(IMG_HEIGHT, IMG_WIDTH),
                                                           class_mode='binary')

print(train_data_gen)

validate_data_gen = train_image_generator.flow_from_directory(batch_size=batch_size,
                                                           directory=validate_dir,
                                                           shuffle=True,
                                                           target_size=(IMG_HEIGHT, IMG_WIDTH),
                                                           class_mode='binary')

print(validate_data_gen)


# This function will plot images in the form of a grid with 1 row and 5 columns where images are placed in each column.
def plotImages(images_arr):
    fig, axes = plt.subplots(1, 5, figsize=(20,20))
    axes = axes.flatten()
    for img, ax in zip( images_arr, axes):
        ax.imshow(img)
        ax.axis('off')
    plt.tight_layout()
    plt.show()

# plotImages(sample_training_images[:5])


model = Sequential([
    Conv2D(16, 3, padding='same', activation='relu', input_shape=(IMG_HEIGHT, IMG_WIDTH ,3)),
    MaxPooling2D(),
    Conv2D(32, 3, padding='same', activation='relu'),
    MaxPooling2D(),
    Conv2D(64, 3, padding='same', activation='relu'),
    MaxPooling2D(),
    Flatten(),
    Dense(512, activation='relu'),
    Dense(6, activation='softmax')
])

model.compile(optimizer='adam',
              loss='sparse_categorical_crossentropy',
              metrics=['accuracy'])

model.summary()

# history = model.fit_generator(
#     train_data_gen,
#     steps_per_epoch=total_train // batch_size,
#     epochs=epochs,
#     validation_data=validate_data_gen,
#     validation_steps=total_validate // batch_size
# )

sample_training_images, sample_training_labels = next(train_data_gen)
sample_validate_images, sample_validate_labels = next(validate_data_gen)

sample_training_images = sample_training_images / 255
sample_validate_images = sample_validate_images / 255
history = model.fit(sample_training_images, sample_training_labels, epochs = epochs, validation_data=(sample_validate_images, sample_validate_labels))
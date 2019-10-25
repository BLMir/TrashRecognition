import os
import random
import shutil

data_dir = os.path.join(os.path.dirname('dataset-modified-small'), 'dataset-modified-small')

categories = ['cardboard','glass','metal','paper','plastic','trash']

total_train = 0
try: 
    os.makedirs(os.path.join(data_dir, 'train'))
    os.makedirs(os.path.join(data_dir, 'validate'))
    for folder in categories:
        os.makedirs(os.path.join(data_dir, 'validate/' + folder ))
        os.makedirs(os.path.join(data_dir, 'train/' + folder ))

except Exception as e:
    pass

for category in categories:
    path = os.path.join(data_dir, category)
    new_path = os.path.join(data_dir, 'train/' + category)
    print(new_path)
    print(path)
    random.shuffle(os.listdir(path))
    # shutil.move(path, new_path)
    train_images= 0.90 * float(len(os.listdir(path)))
    validate_images = float(len(os.listdir(path))) - train_images

    print(int(train_images))
    print(int(validate_images))

    count1 = 0
    count2 = 0
    for img in os.listdir(path)[:int(train_images)]:
        print(img)
        count1 = count1 + 1
        print(os.path.join(data_dir, 'train/' + category + '/' + img))
        print(os.path.join(data_dir, category + '/' + img))
        shutil.move(os.path.join(data_dir, category + '/' + img),os.path.join(data_dir, 'train/' + category + '/' + img))
    print(count1)
    for img in os.listdir(path)[-int(validate_images):]:
        print(img)
        count2 = count2 + 1
        shutil.move(os.path.join(data_dir, category + '/' + img),os.path.join(data_dir, 'validate/' + category + '/' + img))
    print(count2)
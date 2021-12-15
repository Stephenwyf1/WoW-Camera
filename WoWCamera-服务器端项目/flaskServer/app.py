import cv2
from flask import Flask, request, render_template, send_from_directory, make_response
from IRCNN import ircnn_test
import os

# import paddlehub as hub
# import paddle

app = Flask(__name__)
UPLOAD_FOLDER = 'static/upload'
RESULT_FOLDER = 'static/result'
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER
app.config['RESULT_FOLDER'] = RESULT_FOLDER
basedir = os.path.abspath(os.path.dirname(__file__))


# readingPicturesWritingPoems = hub.Module(directory="/root/projects/AIPoem/reading_pictures_writing_poems")
# readingPicturesWritingPoems = hub.Module(directory="./AIPoem/reading_pictures_writing_poems")
# @app.route('/')
# def hello_world():
#     return 'Hello World!'

@app.route('/')
def hello_world():
    return render_template('uploadFile_denoise.html')


def denoise(path, img_name):
    img = ircnn_test.denoise(path)
    save_path = os.path.join(basedir, RESULT_FOLDER)
    cv2.imwrite(save_path + "/" + img_name, img)


# def AIPoem(path):
# results = readingPicturesWritingPoems.WritingPoem(image=path)
# # [{'image': 'banana.jpg', 'Poetrys': '辣椒石榴臭，枇杷苦瓜香。物各有时节，人各有时节。'}]
# poem = results[0]['Poetrys']
# return poem

@app.route('/uploadFile/<string:type>', methods=['get', 'post'])
def uploadFile(type):
    print(basedir)

    img = request.files['file']
    print(img)

    path = os.path.join(basedir, app.config['UPLOAD_FOLDER'])
    img_name = img.filename
    file_path = os.path.join(path, img_name)
    img.save(file_path)

    response = None
    if type == 'denoise':
        denoise(file_path, img_name)
        response = return_img(img_name)
    # elif type == 'poem':
    #     response = AIPoem(file_path)

    return response


def return_img(filename):
    file_dir = os.path.join(basedir, app.config['RESULT_FOLDER'])

    if filename is None:
        pass
    else:
        image_data = open(os.path.join(file_dir, '%s' % filename), "rb").read()
        response = make_response(image_data)
        response.headers['Content-Type'] = 'image/jpg'
        return response


# ************************************************************************************************

@app.route('/upload', methods=['post'])
def upload():
    img = request.files.get('file')
    print(type(img))
    # username = request.form.get("name")
    print(basedir)
    path = os.path.join(basedir, app.config['UPLOAD_FOLDER'])
    file_path = os.path.join(path, img.filename)
    img.save(file_path)
    # print('上传头像成功，上传的用户是：' + username)
    return 'success'


@app.route('/test', methods=['get', 'post'])
def test():
    print(request.args)
    return 'sucess'


@app.route('/download/<string:filename>', methods=['GET'])
def download(filename):
    if request.method == "GET":
        if os.path.isfile(os.path.join('upload', filename)):
            return send_from_directory('upload', filename, as_attachment=True)
    return 'success'


# show photo
@app.route('/show/<string:filename>', methods=['GET'])
def show_photo(filename):
    file_dir = os.path.join(basedir, app.config['UPLOAD_FOLDER'])
    if request.method == 'GET':
        if filename is None:
            pass
        else:
            image_data = open(os.path.join(file_dir, '%s' % filename), "rb").read()
            response = make_response(image_data)
            response.headers['Content-Type'] = 'image/png'
            return response
    else:
        pass


if __name__ == '__main__':
    app.run(host="0.0.0.0")

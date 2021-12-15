import socket
import threading
import cv2
import numpy as np
import Enhance
import Denoise
import paddlehub as hub
import paddle
# from IRCNN import test
# import tensorflow as tf

# 加载模型
# model = model.attempt_load("./best.pt")
# yolov5_model = torch.load('./model.pkl')
# rotten_detection_model = tf.keras.models.load_model("./v1.0.h5")
# paddle.enable_static()
readingPicturesWritingPoems = hub.Module(directory="/root/projects/AIPoem/reading_pictures_writing_poems")


# 图片处理结果路径
save_location = "/root/projects/oppo_camera/pythonProcessResult/"


def main():
    # 创建服务器套接字
    serversocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    # 获取本地主机名称
    host = socket.gethostname()
    # 设置一个端口
    port = 12345
    # 将套接字与本地主机和端口绑定
    serversocket.bind((host, port))
    # 设置监听最大连接数
    serversocket.listen(5)
    # 获取本地服务器的连接信息
    myaddr = serversocket.getsockname()
    print("服务器地址:%s" % str(myaddr))
    # 循环等待接受客户端信息
    while True:
        # 获取一个客户端连接
        clientsocket, addr = serversocket.accept()
        print("连接地址:%s" % str(addr))
        try:
            msg = ''
            flag = ""
            while True:
                """
                classification:
                image:
                detection:
                """
                # 读取recvsize个字节
                rec = clientsocket.recv(1024 * 1024)
                # 解码
                msg += rec.decode("utf-8")

                # 文本接受是否完毕，因为python socket不能自己判断接收数据是否完毕，
                # 所以需要自定义协议标志数据接受完毕
                if msg.strip().endswith('poem'):
                    flag = "poem"
                    msg = msg[:-4]
                    break
                elif msg.strip().endswith('image'):
                    flag = "image"
                    msg = msg[:-5]
                    break
                elif msg.strip().endswith('denoise'):
                    flag = "denoise"
                    msg = msg[:-7]
                    break
                elif msg.strip().endswith('enhance'):
                    flag = "enhance"
                    msg = msg[:-7]
                    break
            if flag == 'poem':
                results = readingPicturesWritingPoems.WritingPoem(image = msg)
                # [{'image': 'banana.jpg', 'Poetrys': '辣椒石榴臭，枇杷苦瓜香。物各有时节，人各有时节。'}]
                poem = results[0]['Poetrys']
                clientsocket.send(("%s" % poem).encode("utf-8"))
                clientsocket.close()
                print("任务结束.....")
#             elif flag == 'denoise':
#                 result_img = test.denoise(msg)
#                 cv2.imwrite(save_location + flag + '_result.jpg', result_img)
#                 clientsocket.close()
#                 print("任务结束.....")
            else:
                t = ServerThreading(clientsocket, flag, msg)  # 为每一个请求开启一个处理线程
                t.start()

            pass
        except Exception as identifier:
            print(identifier)
            pass
        pass
    serversocket.close()
    pass


class ServerThreading(threading.Thread):
    # words = text2vec.load_lexicon()
    def __init__(self, clientsocket, flag, msg, recvsize=1024 * 1024, encoding="utf-8"):
        threading.Thread.__init__(self)
        self._socket = clientsocket
        self._flag = flag
        self._msg = msg
        self._recvsize = recvsize
        self._encoding = encoding
        pass

    def run(self):
        print("开启线程.....")
        try:
            # predict_label = predict_class(msg)
            # 测试，模拟神经网络处理时间较长
            # time.sleep(10)
            # 发送数据
            # self._socket.send(("%s" % predict_label).encode(self._encoding))
            flag = self._flag
            msg = self._msg
            if flag == 'image':
                return_image(self._socket, 'imgs/123.jpg')
            elif flag == 'denoise':
                img = cv2.imread(msg)
                result_img = Denoise.fastNlMeans_denoise(img)
                cv2.imwrite(save_location + flag + '_result.jpg', result_img)
            elif flag == 'enhance':
                img = cv2.imread(msg)
                result_img = Enhance.skin_white(img)
                cv2.imwrite(save_location + flag + '_result.jpg', result_img)
            else:
                error_info = "指定的神经网络模型不存在！请检查输入的type参数"
                self._socket.send(("%s" % error_info).encode(self._encoding))
            # 发送数据
            # self._socket.send(("%s" % predict_label).encode(self._encoding))
            pass
        except Exception as identifier:
            self._socket.send("500".encode(self._encoding))
            print(identifier)
            pass
        finally:
            self._socket.close()
        print("任务结束.....")

        pass

    def __del__(self):
        pass


# def predict_class(msg):
#     # 打开java服务器发送的图片
#     sender_image = Image.open(msg)
#     print(f"发送的图片的shape：{sender_image.size}")
#     # 将java服务器发送的图片转换成黑白图
#     sender_image_binary = sender_image.convert('1')
#     print(f"发送的图片(转换为黑白图后)的shape：{sender_image_binary.size}")
#     # 将图片裁剪成模型的输入尺寸
#     crop_img = sender_image_binary.crop((0, 0, 28, 28))
#
#     img = np.array(crop_img)
#     print(f"发送的图片(转换为黑白图且裁剪后)的shape：{img.shape}")
#
#     img = np.expand_dims(img, axis=0)
#     # 调用模型处理
#     predictions = model.predict(img)
#     predict_label = np.argmax(predictions)
#     print(f"预测的结果标签为：{predict_label}")
#     return predict_label


def return_image(_socket, filepath):
    print('发送图片启动')
    # filepath = 'imgs/123.jpg'  # 输入需要传输的图片名 xxx.jpg
    fp = open(filepath, 'rb')  # 打开要传输的图片
    while True:
        data = fp.read(1024)  # 读入图片数据
        if not data:
            print('{0} send over...'.format(filepath))
            break
        _socket.send(data)  # 以二进制格式发送图片数据



if __name__ == "__main__":
    main()
    # execute_yolov5("./s2.jpg")
    # detection_and_rotten("./s2.jpg")

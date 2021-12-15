import cv2


def fastNlMeans_denoise(img):
    dst = cv2.fastNlMeansDenoisingColored(img, None, 10, 10, 7, 21)
    return dst


if __name__ == '__main__':
    img = cv2.imread('test.jpg')

    dst = cv2.fastNlMeansDenoisingColored(img, None, 10, 10, 7, 21)

#     plt.subplot(121), plt.imshow(img)
#     plt.subplot(122), plt.imshow(dst)
#     plt.show()

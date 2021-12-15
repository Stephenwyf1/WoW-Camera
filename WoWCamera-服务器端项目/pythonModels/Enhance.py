import cv2


def skin_white(img):
    value1 = 3
    value2 = 1
    opacity = 0.1
    dx = value1 * 5
    fc = value1 * 12.5

    temp_img = cv2.bilateralFilter(img, dx, fc, fc)
    temp_img = temp_img - img + 128
    temp_img = cv2.GaussianBlur(temp_img, (2 * value2 - 1, 2 * value2 - 1), 0)
    temp_img = img + temp_img - 128
    dst = cv2.addWeighted(img, opacity, temp_img, (1 - opacity), 0)
    # print(type(dst))
    return dst


if __name__ == '__main__':
    img = cv2.imread('test.jpg')
    dst = skin_white(img)
    cv2.imwrite('result.jpg', dst)

from collections import namedtuple
import tensorflow as tf
import time
import os
import sys
import matplotlib.pyplot as plt
from IRCNN import ircnn_model
import glob as gb
import cv2
import numpy as np
from IRCNN import read_tfrecord

FLAGS = tf.app.flags.FLAGS

tf.app.flags.DEFINE_string('tfrecord_path', './data/data.tfrecords', '')
tf.app.flags.DEFINE_string('test_set_path', './', '')
tf.app.flags.DEFINE_string('mode', 'Eval', 'train or eval.')
tf.app.flags.DEFINE_string('eval_data_path', './test',
                           'Filepattern for eval data')
tf.app.flags.DEFINE_string('save_eval_path', './output/', '')
tf.app.flags.DEFINE_integer('max_iter', 160000, '')
tf.app.flags.DEFINE_bool('is_color', True, '')
tf.app.flags.DEFINE_integer('image_size', 64, 'Image side length.')
tf.app.flags.DEFINE_integer('batch_size', 128, '')
tf.app.flags.DEFINE_integer('sigma', 25, '')
# tf.app.flags.DEFINE_string('log_dir',
#                            './log_dir')
# tf.app.flags.DEFINE_string('log_dir', 'log_dir',
#                            'Directory to keep the checkpoints. Should be a '
#                            'parent directory of FLAGS.train_dir/eval_dir.')
# tf.app.flags.DEFINE_string('checkpoint_dir', './checkpoints', '') # only test
tf.app.flags.DEFINE_string('checkpoint_dir', './IRCNN/checkpoints', '') # flask


def eval(hps, path):
    images = tf.placeholder(dtype=tf.float32, shape=[None, None, None, 3])
    labels = tf.placeholder(dtype=tf.float32, shape=[None, None, None, 3])
    model = ircnn_model.IRCNN(hps, images, labels, FLAGS.mode)
    model.build_graph()
    saver = tf.train.Saver()

    # swts = ['*.jpg', '*.png', '*.JPG', '*.bmp']
    # path_lists = []
    # for swt in swts:
    #     path_lists.extend(gb.glob(os.path.join(FLAGS.eval_data_path, swt)))
    try:
        ckpt_state = tf.train.get_checkpoint_state(FLAGS.checkpoint_dir)
        print(f"ckpt_state:{ckpt_state}")
    except tf.errors.OutOfRangeError as e:
        tf.logging.error('Cannot restore checkpoint: %s', e)

    if not (ckpt_state and ckpt_state.model_checkpoint_path):
        tf.logging.info('No model to eval yet at %s', FLAGS.checkpoint_dir)
    zeros = np.zeros([1, 100, 100, 3])
    with tf.Session(config=tf.ConfigProto(device_count={'cpu':0})) as sess:
        saver.restore(sess, ckpt_state.model_checkpoint_path)
        sess.run(model.clear, feed_dict={images: zeros, labels: zeros})

        gt = cv2.imread(path)
        gt = cv2.cvtColor(gt, cv2.COLOR_BGR2RGB)
        gt = np.expand_dims(gt, axis=0)
        gt = gt.astype(np.float32) / 255.
        tf_psnr = tf.image.psnr(labels, model.clear, 1.)
        tf_ssim = tf.image.ssim(labels, model.clear, 1.)
        noisy = gt + FLAGS.sigma / 255. * np.random.standard_normal(gt.shape)
        img, psnr, ssim = sess.run([model.clear, tf_psnr, tf_ssim], feed_dict={images: noisy, labels: gt})
        image_name = os.path.basename(path)
        print('%s, PSNR = %4.2f dB, SSIM = %4.4f' % (image_name, psnr[0], ssim[0]))
        img = img * 255
        img[img < 0] = 0
        img[img > 255] = 255
        img = img.astype('uint8')
        noisy = (noisy - noisy.min()) / (noisy.max() - noisy.min())
        noisy = noisy * 255
        noisy[noisy < 0] = 0
        noisy[noisy > 255] == 255
        noisy = noisy.astype('uint8')
        # img = np.concatenate([noisy,img], axis=2)
        # cv2.imwrite(os.path.join(FLAGS.save_eval_path, image_name),
        #             cv2.cvtColor(np.squeeze(img), cv2.COLOR_RGB2BGR))
        img = cv2.cvtColor(np.squeeze(img), cv2.COLOR_RGB2BGR)
        return img
        # cv2.imwrite(os.path.join(FLAGS.save_eval_path, image_name),img)


def denoise(img_path):
    hps = ircnn_model.HParams(batch_size=FLAGS.batch_size,
                              min_lrn_rate=0.00001,
                              lrn_rate=0.001,
                              num_conv=7,
                              weight_decay_rate=0.0001,
                              optimizer='adam')

    img = eval(hps, img_path)
    return img


if __name__ == '__main__':

    img = denoise("./test/test.png")


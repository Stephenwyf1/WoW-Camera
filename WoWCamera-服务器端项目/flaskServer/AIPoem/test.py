import paddlehub as hub
import paddle

if __name__ == "__main__":
    # paddle.enable_static()
    readingPicturesWritingPoems = hub.Module(directory="./reading_pictures_writing_poems")
    readingPicturesWritingPoems.WritingPoem(image = "/root/projects/oppo_camera/androidPictures/temp.jpg")
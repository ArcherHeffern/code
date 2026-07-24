import cv2
import numpy as np
import pytesseract

# Load the img


def read_image(file: str):
    img = cv2.imread(file)

    # Cvt to hsv
    hsv = cv2.cvtColor(img, cv2.COLOR_BGR2HSV)

    # Get binary-mask
    msk = cv2.inRange(hsv, np.array([0, 0, 175]), np.array([179, 255, 255]))
    krn = cv2.getStructuringElement(cv2.MORPH_RECT, (5, 3))
    dlt = cv2.dilate(msk, krn, iterations=1)
    thr = 255 - cv2.bitwise_and(dlt, msk)

    # OCR
    d = pytesseract.image_to_string(thr, config="--psm 10")
    print(d)


def main():
    file = "flood.png"
    out = pytesseract.image_to_string(
        file, config="--psm 10 -c tessedit_char_whitelist=01"
    )
    print(out)

    read_image(file)


IMAGE = "./flood.png"

if __name__ == "__main__":
    main()

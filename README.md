# C23-PS443-capstone-bangkit
# Machine Learning environment

Building a face detection model using TensorFlow and the VGG16 architecture. Once the faces are detected, the next step is to extract the Regions of Interest (ROIs) based on the coordinates of the bounding boxes. These ROIs represent specific areas on the forehead and cheeks. To process the vital signs, the extracted ROIs are transformed into RGB data, which is then used for rPPG signal processing. The rPPG algorithm analyzes the red channel in the ROI regions to estimate the user’s vital signs. 

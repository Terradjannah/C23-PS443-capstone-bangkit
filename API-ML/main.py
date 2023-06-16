import os
import uvicorn
import traceback
import tensorflow as tf
import requests
import io

from pydantic import BaseModel
from urllib.request import Request
from fastapi import FastAPI, Response, HTTPException, Header, Depends
from fastapi.security import OAuth2PasswordBearer
from utils import load_image_into_numpy_array

model = tf.keras.models.load_model('./facedetector.h5')

app = FastAPI()
@app.get("/")
def index():
    return "connection established"

class ImageUrl(BaseModel):
    url: str

token_header = OAuth2PasswordBearer(tokenUrl="token")
@app.post("/predict_image")
def predict_image(data: ImageUrl, response: Response, token: str = Depends(token_header)):
    print("Received token:", token)
    try:
        # Checking image
        response = requests.get(data.url) # type: ignore
        image = Image.open(io.BytesIO(response.content)) # type: ignore
        
        image = np.array(image)
        print("Image shape:", image.shape)
        
        #image preprocessing
        processed_image = preprocess_image(image)
        
        #Prepare data 
        input_data = np.expand_dims(processed_image, axis=0) / 255.0  # normalize if needed
        
        #Predict the data
        result = model.predict(input_data)
        
        #raw result
        print("Raw prediction result:", result)
        
        #result
        readable_result = parse_result(result)

        print("Parsed prediction result:", readable_result)
        return {"result": readable_result}
    
    except Exception as e:
        traceback.print_exc()
        response.status_code = 500
        return {"error": "Internal Server Error", "detail": str(e)}

from PIL import Image
import numpy as np

def preprocess_image(image):
    image = Image.fromarray((image * 255).astype(np.uint8))
    image = image.resize((120, 120))
    image = np.array(image)
    return image

def parse_result(result):
    predict = result[1][0]  
    class_id = np.argmax(predict)
    return int(class_id)  

# Starting the server
port = os.environ.get("PORT", 8080)
print(f"Listening to http://0.0.0.0:{port}")
uvicorn.run(app, host='0.0.0.0',port=port) # type: ignore
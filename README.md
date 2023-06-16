# Melathy (Machine Learning Healty)
This project aims to create a backend API using Node.js and a Machine Learning API using Python. We will use Firestore as a database to store user profiles, prediction results, and prediction history. In the API backend, we will create several endpoints such as registration, login, forgot password, reset password, get user data, upload profile photo, upload image to predict, get profile photo, and get prediction history. We will use cookies to store user access tokens. We will deploy the two APIs we have created to Cloud Run using Cloud Build, so that if there are updates to our APIs, they can be changed quickly. We will also utilize Google Cloud Storage to store profile photos and objects needed for prediction. With this project, we aim to create a robust and reliable backend system to support the functions of registration, authentication, user profile management, and prediction using machine learning.

## Project Setup Instructions

Here are some instructions to note to setup this project:

## Key service account settings
Copy the key service account Firestore and Google cloud storage to `Admin-Firestore.json` and `Admin-GCS.json` in the folder `Credentials`

## Environment Variable Settings

Copy and fill the `.env` file with the appropriate data:

```plaintext
port = [port used]
Jwt_Secret = [code for JWT]
GCLOUD_PROJECT_ID = [Project ID in the cloud]
GCLOUD_KEY_FILE_FIRESTORE = [location key service acount firestore]
GCLOUD_KEY_FILE_GCS = [location key service account GCS]
GCS_BUCKET_NAMEVP = [bucket name for user photo files in predict]
GCS_BUCKET_NAMEPP = [bucket name for profile photo file]
```

## Sender Email Settings Reset Token
```plaintext
In the helper folder there is an index.js file. On lines 10 and 11, fill in the user / email used to send the reset token. To pass, enter the APP Password in your Google account.
```

## ML API URL Settings

In the controller folder and userController.js file, change the url on line 278 to the url for the ML API. Here's an example of the setting:
```plaintext
const apiBResponse = await axios.post('http://127.0.0.1:8080/predict_image', { url: publicUrl}, { headers: { Authorization: `Bearer ${req.cookies['jwt_token']}` }} );
'http://127.0.0.1:8080/predict_image' needs to match the url for the ML API used.
```



require('dotenv').config();
const validator = require('validator');
const passwordValidator = require('password-validator');
const bcrypt = require('bcryptjs');
const JWT = require('jsonwebtoken');
const {kirimEmail} = require('../helpers/index')
const uuid = require('uuid');
const db = require('../Database/Database');
const {Storage} = require('@google-cloud/storage');
const axios = require('axios');
const { FieldValue } = require('@google-cloud/firestore');
const moment = require('moment-timezone');


const storage = new Storage({
    projectId: process.env.GCLOUD_PROJECT_ID,
    keyFilename: process.env.GCLOUD_KEY_FILE_GCS
  });

const bucketpp = storage.bucket(process.env.GCS_BUCKET_NAMEPP);
const bucketvp = storage.bucket(process.env.GCS_BUCKET_NAMEVP);

var schema = new passwordValidator();
schema
.is().min(8)                                    
.is().max(20)                                    
.has().uppercase()                               
.has().lowercase()                               
.has().digits(2)                                  
.has().not().spaces()


exports.Daftaruser = async (req, res) => {

    const { email, password, no_telp, jenis_kelamin, nama, berat, tinggi, umur } = req.body;
    if (!validator.isEmail(email)) {
        return res.status(400).json({ message: 'Invalid email address' });
    } else if (!schema.validate(password)) {
        return res.status(400).json({ message: 'Invalid password' });
    };
    
    const userRef = db.collection('users').doc(email).get(); 
    if(userRef.exists){
        return res.status(404).json({ 
            message: 'email already exists' 
    });
    }else{
        const hashedPassword = bcrypt.hashSync(password, 10);
        const id = uuid.v4();
        const user = {
            id: id,
            email: email,
            password: hashedPassword,
            no_telp: no_telp,
            nama: nama,
            jenis_kelamin: jenis_kelamin,
            berat: berat,
            tinggi: tinggi,
            umur: umur,
            profilePhotoUrl: 'not found data',
            ImageUrl: 'not found data'
        };
        await usersDb.doc(email).set(user);
        return res.status(201).json({message: "uuser successfully registered"});
    }  
}

exports.Loginuser = async (req, res) => {
    const { email, password } = req.body;

    const users = db.collection("users");
    const userRef = await users.where('email', '==', email).get();

    if (!userRef.empty) {
        const doc = userRef.docs[0];
        const userData = doc.data();
        const passwordIsValid = await bcrypt.compare(password, userData.password);
        
        if (passwordIsValid) {
            if (userData.email) { 
                const data = {
                    id: userData.id,
                    email: userData.email
                }
                const token = JWT.sign(data, process.env.JWT_Secret)
                res.cookie('jwt_token', token, {
                    httpOnly: true,
                    maxAge: 7 * 24 * 60 * 60 * 1000,
                    path: '/',
                    secure: false});
                return res.status(200).json({
                    message: "Login success",
                    token: token,
                    id: userData.id
                });
            } else {
                return res.status(400).json({
                    message: "Email is missing"
                });
            }
        } else {
            return res.status(401).json({
                message: 'Incorrect password or email'
            });
        }
    } else {
        return res.status(404).json({
            message: 'User not found'
        });
    }
}

exports.getUser = async (req, res) => {
    console.log("req.user:", req.user);
    if(!req.user) {
        return res.status(400).json({ 
            message: 'unauthorization'
        });
    }
    const userRef = await db.collection("users").doc(req.user.email).get();    
    if (userRef.exists) {
        return res.status(200).json({ 
            message: 'user data acquired',
            data: userRef.data() 
        });
    } else {
        return res.status(404).json({ 
            message: 'User not found'
        });
    }
}

exports.forgotPassword = async (req, res) => {
    const{email}= req.body

    const userRef = await db.collection("users").doc(email).get();
    if (!userRef.exists){
        return res.status(404).json({ 
            status: false,
            message: "email tidak ditemukan"
        });
    }

    const token = await JWT.sign({iduser: userRef.data().id}, process.env.JWT_Secret, { expiresIn: '1h' });

    await userRef.update({ resetPasswordLink: token });
    
    try {
        const mailOptions = {
            from: 'melathy',
            to: email,
            subject: 'Link Reset Password',
            html: `<p>Silahkan klik link dibawah ini untuk reset password</p>\n<p>${process.env.CLIENT_URL}/resetpassword/${token}</p>`
        };
        await kirimEmail(mailOptions);
    } catch (error) {
        console.error(`Failed to send email: ${error}`);
        return res.status(500).json({ 
            message: 'Gagal mengirim email reset password'
        });
    }

    return res.status(200).json({ 
        message: 'link reset password berhasil terkirim'
    });
}

exports.resetPassword =  async (req, res) => {
    const password =req.body.password;
    const token = req.params.token;

    if (!schema.validate(password)) {
        return res.status(400).json({ message: 'Invalid password' });
    };
    
    const userRef = db.collection("users").where('resetPasswordLink', '==', token).get();;
    if (userRef.empty) {
        return res.status(400).json({
            status: 'false',
            message: 'Token tidak valid'
        })
    }
    try{
        const hashpass = bcrypt.hashSync(password, 10);
        const doc = userRef.docs[0];
        await users.doc(doc.id).update({password: hashpass, resetPasswordLink: null})
        return res.status(201).json({
            status: 'true',
            message: 'password berhasil diganti'
        })
    }catch (error){
        return res.status(500).json({
            status: 'false',
            message: 'error reset password'
        });
    }  
}

exports.uploadProfilePhoto =  async (req, res, next) => {
    if(!req.user || !req.user.email){
        return res.status(401).send('invalid user');
    }
    if (!req.file) {
      res.status(400).send('No file uploaded.');
      return;
    }
    const blob = bucketpp.file(`${req.user.id}/profile_photo.jpg`);
    const blobStream = blob.createWriteStream({
      resumable: false,
      metadata: {
        cacheControl: 'no-cache',
      },
    });
  
    blobStream.on('error', err => {
      next(err);
    });
  
    blobStream.on('finish', async () => {
      const publicUrl = `https://storage.googleapis.com/${bucketpp.name}/${blob.name}`;
      const userRef = await db.collection('users').where('email', '==', req.user.email).get();
      if (userRef.empty) {
        res.status(404).send('User not found');
        return;
      } 

      userRef.docs[0].ref.update({ profilePhotoUrl: publicUrl })
      res.status(200).send(publicUrl);
    });
  
    blobStream.end(req.file.buffer);
  };
  
  exports.getProfilePhoto = async (req, res) => {
    if( !req.user.id){
        return res.status(401).send('invalid user');
    }
    const userRef = await db.collection('users').where('id', '==', req.user.id).get();
    
    if (userRef.empty) {
        res.status(404).send('User not found');
        return;
    }
    const user = userRef.docs[0];
    res.status(200).send(user.data().profilePhotoUrl);
};

exports.uploadImage = async (req, res, next) => {
    if (!req.file) {
        res.status(400).send('No file uploaded.');
        return;
    }
    
    const blob = bucketvp.file(`${req.user.id}/image_user.jpg`);
    const blobStream = blob.createWriteStream({
        resumable: false,
        metadata: {
            cacheControl: 'no-cache',
        },
    });

    blobStream.on('error', err => {
        next(err);
    });

    blobStream.on('finish', async () => {
        const publicUrl = `https://storage.googleapis.com/${bucketvp.name}/${blob.name}`;

        const userRef = await db.collection('users').where('email', '==', req.user.email).get();
        
        if (userRef.empty) {
            res.status(404).send('User not found');
            return;
        }

        userRef.docs[0].ref.update({ ImageUrl: publicUrl });
        //const apiBResponse = await axios.post('https://api-ml-umyc4436xa-et.a.run.app/predict_image', { url: publicUrl}, { headers: { Authorization: `Bearer ${req.cookies['jwt_token']}` }});  
        const apiBResponse = await axios.post('http://127.0.0.1:8080/predict_image', { url: publicUrl}, { headers: { Authorization: `Bearer ${req.cookies['jwt_token']}` }});  
        
        if (apiBResponse.status !== 200) {
            res.status(500).send('Failed to process image with API B');
            return;
        }

        let predictionUser
        console.log('Prediction Result: ', apiBResponse.data.result)
        if (apiBResponse.data.result == 3){
            predictionUser = 'it is a human';
            console.log('Prediction Result: ', 'it is a human')
        }else if(apiBResponse.data.result == 2){
            predictionUser = 'its not human';
            console.log('Prediction Result: ', 'its not human')
        }else {
            predictionUser = 'there is error';
            console.log('Prediction Result: ', 'there is error')
        }
        
        const predictionUserRef = db.collection('user_Prediction').doc(req.user.email);
        const timestamp = moment().tz("Asia/Jakarta").format('YYYY-MM-DDTHH:mm:ss');
        await predictionUserRef.set({history: FieldValue.arrayUnion({timestamp: timestamp, prediction: predictionUser, imageURL: publicUrl})},{merge: true});

        res.status(200).send({imageUrl: publicUrl, prediction: predictionUser});

    });

    blobStream.end(req.file.buffer);
};

exports.logoutUser = (req, res) => {
    res.clearCookie('jwt_token');
    return res.status(200).json({
        message: "Logout successful"
    });
};

exports.history = async (req, res) => {
    const historyUserRef = await db.collection('user_Prediction').doc(req.user.email).get();
    if(!historyUserRef){
        return res.status(404).json({message: 'history not found'})
    }else{
        let history = historyUserRef.data().history;
        let historyarray = [];
        for(let i = 0; i < history.length; i++){
            let timestamp = history[i].timestamp;
            let prediction = history[i].prediction;
            let imageURL = history[i].imageURL;
            
            const historyuser = {
                timestamp: timestamp,
                prediction: prediction,
                imageURL: imageURL
            }
            
            historyarray.push(historyuser);
        }
        res.status(200).send({history: historyarray});
    }   
}


const express = require('express');
const app = express();
const router = express.Router();
const passport = require('passport');
app.use(passport.initialize());
const { Daftaruser, Loginuser, getUser, forgotPassword, resetPassword, uploadProfilePhoto, getProfilePhoto, uploadImage, history, logoutUser} = require('../controller/user.controller');
const multer = require('multer');

const multerUpload = multer({
    storage: multer.memoryStorage(),
    limits: {
      fileSize: 25 * 1024 * 1024,
    },
});


router.post('/registrasi', Daftaruser);
router.post('/login', Loginuser);
router.get('/user', passport.authenticate('jwt', {session: false}), getUser);
router.put('/forgotpassword', forgotPassword);
router.put('/resetpassword/:token', resetPassword);
router.post('/uploadProfilePhoto', multerUpload.single('photo'),passport.authenticate('jwt', {session:false}), uploadProfilePhoto);
router.get('/getProfilePhoto', passport.authenticate('jwt', {session:false}),  getProfilePhoto);
router.post('/uploadImage',  multerUpload.single('image'),passport.authenticate('jwt', {session:false}), uploadImage);
router.get('/logout', logoutUser);
router.get('/history', passport.authenticate('jwt', {session:false}), history);
module.exports = router
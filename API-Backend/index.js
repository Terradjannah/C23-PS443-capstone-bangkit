require('dotenv').config();
const express = require('express');
const bodyParser = require('body-parser');
const cookieParser = require('cookie-parser');
const session = require('express-session');
const flash = require('connect-flash');
const app = express();
const passport = require('passport');
require('./auth/auth');

const routeUser = require('./routes/user');

//app.use(session({ secret: 'somesecret', cookie: { maxAge: 7 * 24 * 60 * 60 * 1000 }}));
app.use(flash());
app.use(passport.initialize());
app.use((req, res, next) => {
    //console.log(req.body);
    next();
});
// app.use(cors());

app.use(cookieParser());
app.use(bodyParser.json());
app.use('/', routeUser);

const port = process.env.PORT || 5000;
app.listen(port, (req, res) =>{
    console.log(`server berjalan ${process.env.PORT}`)
})

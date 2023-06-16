require('dotenv').config();
const JwtStrategy = require('passport-jwt').Strategy;
const ExtractJwt = require('passport-jwt').ExtractJwt;
const passport = require('passport');
const db = require('../Database/Database');

const secret = process.env.JWT_Secret;
const opts = {
    jwtFromRequest: ExtractJwt.fromExtractors([(req) => {
      var token = null;
      if (req && req.cookies)
      {
          token = req.cookies['jwt_token'];
      }
      return token;
    }]),
    secretOrKey: secret
  };

  passport.use('jwt', new JwtStrategy(opts, async (jwt_payload, done) => {
    try {
      const users = await db.collection('users').where('id', '==', jwt_payload.id).get();
        
      if(!users.empty){
        const doc = users.docs[0];
        const userData = doc.data();
        if(userData.email === jwt_payload.email){
          done(null, userData);
        }else{
          done(null, false, {message : 'token payload not match'});
        }
        
      } else {
        done(null, false, {message: 'User Not Found'});
      }
    } catch (error) {
      done(error, false);
    }
  }));


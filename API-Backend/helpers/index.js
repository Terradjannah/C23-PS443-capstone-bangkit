const nodemailer = require('nodemailer')

exports.kirimEmail = dataEmail => {
    let transporter = nodemailer.createTransport({
        host: "smtp.gmail.com",
        port: 587,
        secure: false, 
        requireTLS: true,
        auth: {
          user: , 
          pass: 
        },
    });

    return transporter.sendMail(dataEmail)
        .then(info => {
            console.log(`email terkirim:${info.messageId}`);
            return info;
        })
        .catch(err => {
            console.error(`email gagal terkirim:${err}`);
            throw err;
        });
};

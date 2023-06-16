const Firestore = require('@google-cloud/firestore');
require('dotenv').config();

const db = new Firestore({
  projectId: process.env.GCLOUD_PROJECT_ID,
  keyFilename: process.env.GCLOUD_KEY_FILE_FIRESTORE
});

module.exports = db;
const admin = require("firebase-admin");
const sgMail = require("@sendgrid/mail");
const {onCall} = require("firebase-functions/v2/https");
const {defineString} = require("firebase-functions/params");

admin.initializeApp();

const SENDGRID_API_KEY = defineString("SENDGRID_API_KEY");

exports.sendResetCode = onCall(async (request) => {
  const apiKey = SENDGRID_API_KEY.value();

  if (!apiKey) {
    throw new Error("SendGrid API Key not configured");
  }

  sgMail.setApiKey(apiKey);

  const {email, code} = request.data || {};

  if (!email || !code) {
    throw new Error("email or code missing");
  }

  const msg = {
    to: email,
    from: "jmj987654321@gmail.com",
    subject: "重設密碼驗證碼",
    text: `您的驗證碼是：${code}\n\n請於 5 分鐘內使用`,
  };

  await sgMail.send(msg);

  return {success: true};
});

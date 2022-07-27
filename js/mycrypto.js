//PKCS#1 padding   
function encryptByRSA(string, key) {
    pubkey = 'MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCztkK+sbF4LzuKPshL9DmKbMq6mvvT7s+GVVmURiZNC8m4AwheBDje4RTdbDXqnhZSSS8MtziszfWPhvf1q3SnpkTa7G9+U8p835UG7SQSH6f3mOrJWMHqyYzDyOnNKc/V5am72D6qNgjwlr5FAHj2O3RstdaIcRW+HPuNjNNGqwIDAQAB';
    let jsencrypt = new JSEncrypt();
    jsencrypt.setPublicKey(pubkey);
    return jsencrypt.encrypt(string);//base64
}
function encryptByAES(string, key, ivstr) {
    //key = '1234567890123456';
    let ckey = CryptoJS.enc.Utf8.parse(key);
    let encrypted = CryptoJS.AES.encrypt(string, ckey, {
        mode: CryptoJS.mode.CBC,
        padding: CryptoJS.pad.Iso10126,
        iv: CryptoJS.enc.Utf8.parse(ivstr)
    });
    return encrypted.toString() // base64
    //return encrypted.ciphertext.toString();//hex
}
function decryptByAES(string, key, ivstr) {
    let ckey = CryptoJS.enc.Utf8.parse(key);
    let decrypted = CryptoJS.AES.decrypt(string, ckey, {
        mode: CryptoJS.mode.CBC,
        padding: CryptoJS.pad.Pkcs7,
        iv: CryptoJS.enc.Utf8.parse(ivstr)
    });
    var resultStr = CryptoJS.enc.Utf8.stringify(decrypted);
    return resultStr;
}
function getAesKey(len) {
    //len = len || 32;
    var $chars = 'ABCDEFGHJKMNPQRSTWXYZabcdefhijkmnprstwxyz2345678'; /****默认去掉了容易混淆的字符oOLl,9gq,Vv,Uu,I1****/
    var maxPos = $chars.length;
    var keyStr = '';
    for (i = 0; i < len; i++) {
        keyStr += $chars.charAt(Math.floor(Math.random() * maxPos));
    }
    return keyStr;
}
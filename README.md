# emv-2fa-android-nfc

Using EMV credit/debit card as a 2FA authenticator, via Android NFC communication.

A video demo is avaiable in [HERE](https://youtu.be/gKVcz9QQTvU)

In case you don't know, EMV stands for Europay, MasterCard, and Visa. 

In order to combat fraudulent transactions, EMV supports three card authentication methods
- SDA: Static Data Authentication
- DDA: Dynamic Data Authentication
- CDA: Combined Data Authentication

SDA is just a (static) certificate stored on the card, your device can read the data and you can verify the data is signed by a genuine card issuer. It's sort of like reading the 16-digit card number from the card, but fancier.

DDA is one step further that the card will use the private key of the corresponding certificate to generate a digital signature. You can send a challenge to it and it will perform a signature on it.

CDA is a variation of DDA. I am not too familar with CDA so I won't talk about it in here.

For DDA, there are a total of three different certificates involved:
1. CA cert - the full cert is not really stored in the card, the card just returns an index number, the full list of certs can be found in [HERE](https://www.eftlab.com/knowledge-base/243-ca-public-keys/)
2. Issuer cert - the card issuer's certificate, stored in full in the card, the cert is signed by the CA cert
3. ICC cert - this is a card specific cert, each card have a different ICC cert and is signed by the issuer cert. Both public key and privated key of the ICC cert are stored inside in the card (but of course you can only read the public key)

## What can we do with DDA?

As my repo title suggests, we can use DDA as a hardware 2FA device. And it can work as follows:

1. **Register**: you present your card to the mobile app, the mobile app reads the CA/Isser/ICC certs, and sends it to the server. The server then performs basic sanity checks on the CA/Issuer/ICC certs and then stores the data
2. **Challenge**: when the user needs to login, the server sends a random challenge to the card (via the mobile app), and then the card responds by signing the message. The mobile app then sends the signature back to the server
4. **Verify**: the server checks the signature against the corresponding certificates 

## Limitation

1. Short key size

The majority RSA key length of the certs (CA/Issuer/ICC) ranging from 1024 to 1408 which is pretty short. I personally think EMV should upgrade to a longer key length as soon as possible.

2. Short challenge size

The random challenge in DDA is limited to 4-byte only (please correct me if I am wrong). While the mobile app and the user can witness the mobile app is really interacting with a card, the server will not know if the response is just a reply message or not. 

3. Malware-app risk

Although in my app, we are doing card authentication only, we are not doing any transaction. But in theory, the app can read the 16-digit card number (but not CVV) and trick the card into performing a transaction. Presenting a card to a terminal or an app requires a certain level of trust. You trust the terminal is doing what it is supposed to do.

Having said that, if you present your card to your own mobile phone, I think the risk is lower than presenting your card to an unknown terminal in a coffee shop which you have no control over. 

4. Certificate expiration

The ICC expiry date is set to the expiry date of the credit card. Therefore, once the credit card expires, the cert will expire too. However, since there is no "clock" in the card, the app has to tell the card what time it is right now, so theoretically, we can bypass the time-bound if we want to.

5. Regulatory risk

I have to declare I am not sure if EMV allows anyone to read the card data in this way. 





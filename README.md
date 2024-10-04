# Warning

This is a sample application that details how to verify a VivoKey NFC Signature from the NDEF record of a qualifying VivoKey product. There is no support and no warranty for this code. This repo and all the code contained within it is for educational purposes only. It is not meant to be used in a production environment.

# NFC Signature Sample App

This app demonstrates how to verify a VivoKey NFC Signature from the NDEF record of a qualifying VivoKey product. It reads the NFC tag, extracts the signature from the tag’s URL, and verifies it against the VivoKey Auth API.

## Features

- **NFC Tag Reading**: Reads NDEF records from VivoKey NFC transponders.
- **Signature Extraction**: Extracts the signature from the URL embedded in the NDEF record.
- **Signature Validation**: Validates the extracted signature via the VivoKey `/validate` API endpoint.
- **JWT Handling**: Displays the decoded JWT (JSON Web Token) if the signature is valid.
- **Error Handling**: Provides user feedback on invalid or expired signatures.

## Prerequisites

- **Android Studio** with Kotlin and Compose support.
- **NFC-enabled device** to interact with the VivoKey product.
- **API key** from VivoKey. See instructions below.

## Get your own API Key

The code contains a test API key, but you should be testing with your own key. Get your own API key from [VivoKey API](https://vivokey.com/api).

## How It Works

1. The app listens for an NFC tag scan.
2. When a tag is detected, the app extracts the URL from the tag’s NDEF record.
3. The URL contains the signature, which is sent to VivoKey’s `/validate` API endpoint.
4. Based on the API response, the app either:
   - Displays the decoded JWT in a readable format if the validation is successful.
   - Shows an error message if the signature is invalid or expired.

## How to Run

1. Clone the repository:
   ```
   bash
   git clone https://github.com/your-username/NFCSignatureSample.git
   cd NFCSignatureSample
   ```

2. Open the project in **Android Studio**.

3. Replace the placeholder API key with your own in the `MainActivity.kt` file.

4. Build and run the app on an NFC-enabled device.

## Dependencies

- **Kotlin**: The app is written in Kotlin and uses Android’s Jetpack Compose for UI.
- **NFC**: The app requires the NFC hardware feature to interact with VivoKey NFC transponders.
- **VivoKey API**: For signature validation and JWT handling.

## Disclaimer

This app is intended for **educational purposes only** and is not production-ready. It is provided "as is" without warranty of any kind. The VivoKey API key in the code is for testing only — please replace it with your own.


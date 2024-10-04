package com.vivokey.nfcsignaturesample

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import android.widget.TextView
import android.widget.EditText
import android.util.Base64
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity(), NfcAdapter.ReaderCallback {

    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var nfcStatusTextView: TextView
    private lateinit var jwtDisplayTextView: TextView
    private lateinit var apiKeyInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize the views
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        nfcStatusTextView = findViewById(R.id.nfcStatus)
        jwtDisplayTextView = findViewById(R.id.jwtDisplay)
        apiKeyInput = findViewById(R.id.apiKeyInput)  // Input for API key
    }

    override fun onResume() {
        super.onResume()
        enableNFC()
    }

    override fun onPause() {
        super.onPause()
        disableNFC()
    }

    private fun enableNFC() {
        val options = Bundle()
        nfcAdapter.enableReaderMode(this, this, NfcAdapter.FLAG_READER_NFC_A, options)
        nfcStatusTextView.text = "Waiting for NFC tag..."
    }

    private fun disableNFC() {
        nfcAdapter.disableReaderMode(this)
    }

    override fun onTagDiscovered(tag: Tag?) {
        runOnUiThread {
            nfcStatusTextView.text = "NFC tag detected, processing..."
        }

        val ndef = Ndef.get(tag)
        if (ndef != null) {
            ndef.connect()
            val ndefMessage = ndef.ndefMessage

            if (ndefMessage == null) {
                runOnUiThread {
                    nfcStatusTextView.text = "NDEF message is null or empty"
                }
                ndef.close()
                return
            }

            val records = ndefMessage.records
            for (record in records) {
                val payload = String(record.payload)

                // Directly search for the signature using regex in the payload
                processPayloadForSignature(payload)
            }
            ndef.close()
        } else {
            runOnUiThread {
                nfcStatusTextView.text = "No NDEF data found on the tag"
            }
        }
    }

    private fun processPayloadForSignature(payload: String) {
        // Regex to extract signature from payload for both formats
        val regex = Regex("([A-Fa-f0-9]{14})-([A-Fa-f0-9]{6})-([A-Fa-f0-9]{16})|([A-Fa-f0-9]{16})-([A-Fa-f0-9]{6})-([A-Fa-f0-9]{32})")
        val match = regex.find(payload)

        if (match != null) {
            val signature = match.value
            runOnUiThread {
                nfcStatusTextView.text = "Signature found, validating..."
            }
            // Call the VivoKey API with the extracted signature
            validateSignature(signature)
        } else {
            runOnUiThread {
                nfcStatusTextView.text = "No valid signature found in payload"
            }
        }
    }

    private fun validateSignature(signature: String) {
        val url = URL("https://auth.vivokey.com/validate")
        val jsonInputString = """{"signature": "$signature"}"""

        // Retrieve the API key from the EditText
        val apiKey = apiKeyInput.text.toString()

        Thread {
            try {
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json; utf-8")
                conn.setRequestProperty("Accept", "application/json")
                // Use the API key from the user input
                conn.setRequestProperty("x-api-vivokey", apiKey)
                conn.doOutput = true

                conn.outputStream.use { os ->
                    val input = jsonInputString.toByteArray(Charsets.UTF_8)
                    os.write(input, 0, input.size)
                }

                val response = conn.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(response)

                when (jsonResponse.getString("result")) {
                    "success" -> {
                        val token = jsonResponse.getString("token")
                        val decodedJWT = decodeJWT(token)
                        runOnUiThread {
                            displayJWT(decodedJWT)
                            nfcStatusTextView.text = "Validation successful!"
                        }
                    }
                    else -> {
                        runOnUiThread {
                            nfcStatusTextView.text = "Validation failed: ${jsonResponse.getString("result")}"
                        }
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    nfcStatusTextView.text = "Error occurred: ${e.message}"
                }
            }
        }.start()
    }

    private fun decodeJWT(jwt: String): String {
        // Decode JWT for display
        val parts = jwt.split(".")
        val payload = String(Base64.decode(parts[1], Base64.URL_SAFE), Charsets.UTF_8)
        return JSONObject(payload).toString(4) // Pretty print JSON
    }

    private fun displayJWT(decodedJWT: String) {
        // Display decoded JWT on screen
        jwtDisplayTextView.text = decodedJWT
    }
}

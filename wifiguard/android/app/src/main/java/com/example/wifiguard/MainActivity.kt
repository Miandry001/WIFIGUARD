package com.example.wifiguard

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.net.wifi.WifiNetworkSuggestion
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var status: TextView
    private var callback: ConnectivityManager.NetworkCallback? = null

    private val cm by lazy { getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager }
    private val wm by lazy { applicationContext.getSystemService(WIFI_SERVICE) as WifiManager }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val inputUser = findViewById<EditText>(R.id.inputUser)
        val inputPass = findViewById<EditText>(R.id.inputPass)
        val checkAppOnly = findViewById<CheckBox>(R.id.checkAppOnly)
        status = findViewById(R.id.textStatus)

        findViewById<Button>(R.id.btnConnect).setOnClickListener {
            val user = inputUser.text.toString().trim()
            val pass = inputPass.text.toString()
            if (user.isEmpty() || pass.isEmpty()) {
                setStatus("Fenoy ny anarana sy ny mot de passe.")
                return@setOnClickListener
            }
            setStatus("Manamarina...")
            Thread {
                try {
                    val (code, body) = fetchWifi(user, pass)
                    when (code) {
                        200 -> {
                            val j = JSONObject(body)
                            runOnUiThread {
                                connectWifi(j.getString("ssid"), j.getString("password"), checkAppOnly.isChecked)
                            }
                        }
                        401 -> setStatus("Anarana na mot de passe diso.")
                        429 -> setStatus("Fanandramana be loatra. Andraso kely.")
                        else -> setStatus("Hadisoana serveur ($code).")
                    }
                } catch (e: Exception) {
                    // Ilaina ny internet (data mobile na WiFi hafa) mba hahatratrarana ny serveur
                    setStatus("Tsy tratra ny serveur. Jereo ny internet (data mobile).")
                }
            }.start()
        }

        findViewById<Button>(R.id.btnDisconnect).setOnClickListener {
            disconnectWifi()
            setStatus("Voaala ny WiFi.")
        }
    }

    private fun setStatus(msg: String) = runOnUiThread { status.text = msg }

    /** POST /wifi -> (kaody HTTP, vatana) */
    private fun fetchWifi(user: String, pass: String): Pair<Int, String> {
        val conn = URL(BuildConfig.SERVER_URL + "/wifi").openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.connectTimeout = 10_000
        conn.readTimeout = 10_000
        conn.doOutput = true
        conn.setRequestProperty("Content-Type", "application/json")
        val payload = JSONObject().put("username", user).put("password", pass).toString()
        conn.outputStream.use { it.write(payload.toByteArray()) }
        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val body = stream?.bufferedReader()?.use { it.readText() } ?: ""
        conn.disconnect()
        return code to body
    }

    private fun connectWifi(ssid: String, password: String, appOnly: Boolean) {
        disconnectWifi()
        if (appOnly) connectAppOnly(ssid, password) else connectSystemWide(ssid, password)
    }

    /** Safidy 1: ny app ihany no mampiasa ity WiFi ity. Mifandray avy hatrany. */
    private fun connectAppOnly(ssid: String, password: String) {
        val specifier = WifiNetworkSpecifier.Builder()
            .setSsid(ssid)
            .setWpa2Passphrase(password)
            .setIsHiddenSsid(true)          // SSID afenina
            .build()
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .setNetworkSpecifier(specifier)
            .build()
        val cb = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                cm.bindProcessToNetwork(network)
                setStatus("Mifandray amin'ny \"$ssid\" (ho an'ity app ity ihany).")
            }
            override fun onUnavailable() = setStatus("Tsy afaka nifandray (tsy hita ny WiFi na nolavina).")
            override fun onLost(network: Network) {
                cm.bindProcessToNetwork(null)
                setStatus("Very ny fifandraisana.")
            }
        }
        callback = cb
        cm.requestNetwork(request, cb, 30_000)
        setStatus("Mitady ny WiFi afenina...")
    }

    /** Safidy 2: ny telefaona manontolo no mampiasa azy (fanekena indray mandeha avy amin'ny mpampiasa). */
    private fun connectSystemWide(ssid: String, password: String) {
        val suggestion = WifiNetworkSuggestion.Builder()
            .setSsid(ssid)
            .setWpa2Passphrase(password)
            .setIsHiddenSsid(true)
            .setIsAppInteractionRequired(false)
            .build()
        wm.removeNetworkSuggestions(emptyList())   // mamafa ny taloha
        val result = wm.addNetworkSuggestions(listOf(suggestion))
        setStatus(
            when (result) {
                WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS ->
                    "Voatahiry ny WiFi. Ekeo ny fanamarihana (notification) raha mivoaka; ho mifandray ho azy ny telefaona."
                WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_APP_DISALLOWED ->
                    "Voasakana ny fahazoan-dalana. Alefaso any amin'ny Paramètres ny app."
                else -> "Tsy tafiditra ny WiFi (kaody $result)."
            }
        )
    }

    private fun disconnectWifi() {
        callback?.let { runCatching { cm.unregisterNetworkCallback(it) } }
        callback = null
        cm.bindProcessToNetwork(null)
        runCatching { wm.removeNetworkSuggestions(emptyList()) }
    }
}

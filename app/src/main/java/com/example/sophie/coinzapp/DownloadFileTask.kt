package com.example.sophie.coinzapp

import android.os.AsyncTask
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection
private val tag = "DownloadFileTask"
class DownloadFileTask(private val caller : DownloadCompleteListener) : AsyncTask<String, Void, String>(){

        override fun doInBackground(vararg urls: String): String = try {
            loadFileFromNetwork(urls[0])
        } catch (e: IOException) {
            "Unable to load content. Please check your network connection"
        }

        private fun loadFileFromNetwork(urlString : String) : String {
            val stream: InputStream = downloadUrl(urlString) //open input string from given url
            var result = stream.bufferedReader().use { it.readText() } // read input stream and build string as result
            return result
            Log.d(tag, "File loaded from network successfully" )
        }

        @Throws(IOException::class)
        private fun downloadUrl(urlString: String) : InputStream {
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection

            conn.readTimeout = 10000 //ms
            conn.connectTimeout = 15000
            conn.requestMethod = "GET"
            conn.doInput = true
            conn.connect()
            return conn.inputStream
        }

        override fun onPostExecute(result : String){
            super.onPostExecute(result)
            caller.downloadComplete(result)
        }
}
//end class

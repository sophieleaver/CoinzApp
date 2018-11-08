package com.example.sophie.coinzapp

import android.os.AsyncTask
import com.example.sophie.coinzapp.DownloadCompleteRunner.result
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class DownloadFileTask(private val caller : DownloadCompleteListener) : AsyncTask<String, Void, String>(){

        override fun doInBackground(vararg urls: String): String = try {
            loadFileFromNetwork(urls[0])
        } catch (e: IOException) {
            "Unable to load content. Please check your network connection"
        }

        private fun loadFileFromNetwork(urlString : String) : String {
            val stream : InputStream = downloadUrl(urlString)
            //val result :String = readStream(stream)
            //var result : String? = null
            //var result = stream.bufferedReader().use { it.readText() }  // defaults to UTF-8
            val inputAsString : String =  stream.bufferedReader().use { it.readText() }
            return inputAsString
            //return inputAsString
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

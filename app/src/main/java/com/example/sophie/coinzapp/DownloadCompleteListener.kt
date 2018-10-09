package com.example.sophie.coinzapp

interface DownloadCompleteListener { // callback function to be called when the download is complete
    fun downloadComplete(result : String)
}

object DownloadCompleteRunner : DownloadCompleteListener {
    var result : String? = null
    override fun downloadComplete(result : String) {
        this.result = result
    }
}


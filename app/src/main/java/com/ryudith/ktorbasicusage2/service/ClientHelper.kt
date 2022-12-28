package com.ryudith.ktorbasicusage2.service

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import com.ryudith.ktorbasicusage2.R
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.jvm.javaio.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.KeyStore
import java.security.cert.CertificateFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager
import kotlin.io.copyTo

class ClientHelper
{
    companion object
    {
        private var instance: ClientHelper? = null
        fun createInstance (context: Context): ClientHelper
        {
            if (instance == null)
            {
                instance = ClientHelper()
                instance!!.initialize(context)
            }
            return instance!!
        }
    }

    private var client: HttpClient? = null
    fun initialize (context: Context)
    {
        client = HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json()
            }

            engine {
                config {
                    val cf = CertificateFactory.getInstance("X.509")
                    val cert = context.resources.openRawResource(R.raw.localhost_android)
                    try {
                        val ca = cf.generateCertificate(cert)

                        val keyStoreType = KeyStore.getDefaultType()
                        val keyStore = KeyStore.getInstance(keyStoreType)
                        keyStore.load(null, null)
                        keyStore.setCertificateEntry("ca", ca)

                        val tmfAlgo = TrustManagerFactory.getDefaultAlgorithm()
                        val tmf = TrustManagerFactory.getInstance(tmfAlgo)
                        tmf.init(keyStore)

                        val sslContext = SSLContext.getInstance("TLS")
                        sslContext.init(null, tmf.trustManagers, null)

                        sslSocketFactory(sslContext.socketFactory, tmf.trustManagers[0] as X509TrustManager)
                    } finally {
                        cert.close()
                    }
                }
            }
        }
    }


    suspend fun sendHttpsRequest (): String?
    {
        try
        {
            val response = client!!.get {
//                url("https://google.com")
                url("https://10.0.2.2/simple_php/method_get.php")
            }

            return response.body()
        }
        catch (e: Exception)
        {
            Log.d("DEBUG_DATA", "https exception: ${e.toString()}")
            return null
        }
    }

    suspend fun sendPostRequest (name: String, age: Int, about: String, infoType: String = "profile", task: String = "update"): String?
    {
        try
        {
            val response = client!!.post {
                url("https://10.0.2.2/simple_php/method_post.php")
                parameter("infoType", infoType)
                parameter("task", task)

                setBody(FormDataContent(Parameters.build {
                    append("name", name)
                    append("age", age.toString())
                    append("about", about)
                }))
            }

            return response.body()
        }
        catch (e: Exception)
        {
            Log.d("DEBUG_DATA", "https exception: ${e.toString()}")
            return null
        }
    }

    suspend fun sendUploadRequest (
        context: Context,
        name: String,
        email: String,
        profile: Uri?,
        photo: List<Uri>,
        infoType: String = "history",
        task: String = "add",
        callback: (sentBytes: Long, totalBytes: Long) -> Unit
    ): String?
    {
        try
        {
            val response = client!!.post {
                url("http://10.0.2.2/simple_php/upload_file.php")
                parameter("infoType", infoType)
                parameter("task", task)

                setBody(MultiPartFormDataContent(formData {
                    append("name", name)
                    append("email", email)

                    // single file
                    if (profile != null)
                    {
                        profile.getFile(context)?.let { profileFile ->
                            profileFile.getMimeType()?.let { fileMimeType ->
                                append("profile", profileFile.readBytes(), Headers.build {
                                    append(HttpHeaders.ContentType, fileMimeType)
                                    append(HttpHeaders.ContentDisposition, "filename=\"${profileFile.name}\"")
                                })
                            }
                        }
                    }

                    // multi file
                    for (uri in photo)
                    {
                        if (uri.path == null) continue

                        val file = uri.getFile(context)
                        if (file == null || !file.exists()) continue

                        val mimeType = file.getMimeType() ?: continue

                        append("photo[]", file.readBytes(), Headers.build {
                            append(HttpHeaders.ContentType, mimeType)
                            append(HttpHeaders.ContentDisposition, "filename=\"${file.name}\"")
                        })
                    }
                }))

                // callback
                onUpload { bytesSentTotal, contentLength ->
                    callback(bytesSentTotal, contentLength)
                }
            }

            return response.body()
        }
        catch (e: Exception)
        {
            return null
        }
    }

    suspend fun sendPutRequest (info: InfoEntity, infoType: String = "profile", task: String = "update"): String?
    {
        try
        {
            val response = client!!.put {
                url("https://10.0.2.2/simple_php/method_post.php")
                parameter("infoType", infoType)
                parameter("task", task)

                contentType(ContentType.Application.Json)
                setBody(info)
            }

            return response.body()
        }
        catch (e: Exception)
        {
            return null
        }
    }

    suspend fun sendPatchRequest (info: InfoEntity, infoType: String = "profile", task: String = "update"): String?
    {
        try
        {
            val response = client!!.patch {
                url("https://10.0.2.2/simple_php/method_post.php")
                parameter("infoType", infoType)
                parameter("task", task)

                contentType(ContentType.Application.Json)
                setBody(info)
            }

            return response.body()
        }
        catch (e: Exception)
        {
            return null
        }
    }
}


// extension functions
fun Uri.getRealName (context: Context): String?
{
    val cursor = context.contentResolver.query(this, null, null, null, null)
    if (cursor == null || !cursor.moveToFirst()) return null

    val indexName = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
    val realName = cursor.getString(indexName)
    cursor.close()

    return realName
}

fun Uri.getFile (context: Context): File?
{
    val fileDescriptor = context.contentResolver.openFileDescriptor(this, "r", null)
    if (fileDescriptor == null) return null

    val file = File(context.cacheDir, getRealName(context)!!)
    val fileOutputStream = FileOutputStream(file)

    val fileInputStream = FileInputStream(fileDescriptor.fileDescriptor)
    fileInputStream.copyTo(fileOutputStream)
    fileDescriptor.close()

    return file
}

fun File.getMimeType (): String?
{
    return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
}






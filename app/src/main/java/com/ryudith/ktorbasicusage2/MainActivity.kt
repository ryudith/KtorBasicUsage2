package com.ryudith.ktorbasicusage2

import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.ryudith.ktorbasicusage2.service.ClientHelper
import com.ryudith.ktorbasicusage2.service.InfoEntity
import com.ryudith.ktorbasicusage2.service.UploadResponseEntity
import com.ryudith.ktorbasicusage2.ui.theme.KtorBasicUsage2Theme
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KtorBasicUsage2Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val clientHelper = ClientHelper.createInstance(applicationContext)

                    LaunchedEffect(key1 = Unit) {
                        // https request
//                        val httpsResponse = clientHelper.sendHttpsRequest()
//                        Log.d("DEBUG_DATA", "https response: ${httpsResponse}")

                        // post request
//                        val postResponse = clientHelper.sendPostRequest("Ryudith", 11, "Testing")
//                        Log.d("DEBUG_DATA", "post response: ${postResponse}")

                        // put request
                        val info = InfoEntity("Ryudith", "ryudith@localhost.com")
                        val putResponse = clientHelper.sendPutRequest(info)
                        Log.d("DEBUG_DATA", "put response: ${putResponse}")

                        // patch request
                        val patchResponse = clientHelper.sendPatchRequest(info)
                        Log.d("DEBUG_DATA", "patch response: ${patchResponse}")
                    }

                    /*// upload file
                    val coroutineScope = rememberCoroutineScope()
                    val mimeTypeFilter = arrayOf("image/jpg", "image/jpeg", "image/png", "image/gif")

                    // single file
                    val bitmapImage = remember { mutableStateOf<ImageBitmap?>(null) }
                    val profile = remember { mutableStateOf<Uri?>(null) }
                    val selectProfileActivity = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) {
                        profile.value = it

                        if (Build.VERSION.SDK_INT < 28)
                        {
                            bitmapImage.value = MediaStore.Images.Media.getBitmap(contentResolver, it).asImageBitmap()
                        }
                        else
                        {
                            val source = ImageDecoder.createSource(contentResolver, it!!)
                            bitmapImage.value = ImageDecoder.decodeBitmap(source).asImageBitmap()
                        }
                    }

                    // multi file
                    val photo = remember { mutableStateListOf<Uri>() }
                    val selectPhotoActivity = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenMultipleDocuments()) { result ->
                        photo.clear()
                        result.forEach {
                            photo.add(it)
                        }
                    }

                    // callback
                    val uploadProgress = remember { mutableStateOf(0.0f) }
                    val photoResponse = remember { mutableStateListOf<String>() }


                    // UI
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = if (profile.value == null) painterResource(R.drawable.ic_launcher_background) else BitmapPainter(bitmapImage.value!!),
                            contentDescription = "Profile image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .width(150.dp)
                                .height(150.dp)
                                .clip(CircleShape)
                                .clickable { selectProfileActivity.launch(mimeTypeFilter) }
                        )

                        Button(onClick = {
                            coroutineScope.launch {
                                selectPhotoActivity.launch(mimeTypeFilter)
                            }
                        }) {
                            Text(text = "Select Photo")
                        }

                        Button(onClick = {
                            coroutineScope.launch {
                                val name = "Ryudith"
                                val email = "ryudith@localhost.com"
                                val uploadResponse = clientHelper.sendUploadRequest(applicationContext, name, email, profile.value, photo)
                                { sentBytes, totalBytes ->
                                    uploadProgress.value = sentBytes.toFloat() / totalBytes
                                }
                                photo.clear()
                                Log.d("DEBUG_DATA", "upload response: ${uploadResponse}")

                                val uploadResponseObject = Json.decodeFromString<UploadResponseEntity>(uploadResponse!!)
                                photoResponse.addAll(uploadResponseObject.photo)
                            }
                        }) {
                            Text(text = "Upload")
                        }

                        LinearProgressIndicator(uploadProgress.value, Modifier.fillMaxWidth(0.9f))

                        if (photoResponse.size > 0)
                        {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .padding(),
                                contentPadding = PaddingValues(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                items(photoResponse, itemContent = { photo ->
                                    Image(
                                        painter = rememberAsyncImagePainter(ImageRequest.Builder(
                                            LocalContext.current).data(photo).build()),
                                        contentDescription = "Photo",
                                        modifier = Modifier.fillMaxSize().height(120.dp)
                                    )
                                })
                            }
                        }
                    }*/
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    KtorBasicUsage2Theme {
        Greeting("Android")
    }
}
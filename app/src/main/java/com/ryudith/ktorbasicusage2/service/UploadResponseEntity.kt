package com.ryudith.ktorbasicusage2.service

@kotlinx.serialization.Serializable
data class UploadResponseEntity(
    val name: String,
    val email: String,
    val profile: String,
    val photo: List<String>,
    val jsonObject: String,
    val debugData: String,
)

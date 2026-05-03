package com.example.nutriscan5.data.repository

import android.content.Context
import android.net.Uri
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

object CloudinaryRepository {
    private const val CLOUD_NAME = "dydjziagu"
    private const val UPLOAD_PRESET = "nutriscan"

    suspend fun uploadImage(context: Context, imageUri: Uri): String = withContext(Dispatchers.IO) {
        val boundary = "Boundary-${UUID.randomUUID()}"
        val uploadUrl = URL("https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload")
        val connection = (uploadUrl.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            useCaches = false
            setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
        }

        try {
            DataOutputStream(connection.outputStream).use { output ->
                writeTextPart(output, boundary, "upload_preset", UPLOAD_PRESET)
                writeTextPart(output, boundary, "folder", "nutriscan/community")

                val mimeType = context.contentResolver.getType(imageUri) ?: "image/jpeg"
                val fileName = "review_${System.currentTimeMillis()}.jpg"

                output.writeBytes("--$boundary\r\n")
                output.writeBytes(
                    "Content-Disposition: form-data; name=\"file\"; filename=\"$fileName\"\r\n"
                )
                output.writeBytes("Content-Type: $mimeType\r\n\r\n")

                context.contentResolver.openInputStream(imageUri)?.use { input ->
                    input.copyTo(output)
                } ?: throw IllegalStateException("Could not open image for upload.")

                output.writeBytes("\r\n")
                output.writeBytes("--$boundary--\r\n")
                output.flush()
            }

            val responseCode = connection.responseCode
            val responseText = if (responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() }
                    ?: "Upload failed with code $responseCode"
            }

            if (responseCode !in 200..299) {
                val errorMessage = runCatching {
                    JSONObject(responseText)
                        .optJSONObject("error")
                        ?.optString("message")
                }.getOrNull()

                throw IllegalStateException(
                    errorMessage ?: responseText
                )
            }

            val json = JSONObject(responseText)
            json.optString("secure_url").ifBlank {
                throw IllegalStateException("Cloudinary upload succeeded but returned no secure_url.")
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun writeTextPart(
        output: DataOutputStream,
        boundary: String,
        fieldName: String,
        value: String
    ) {
        output.writeBytes("--$boundary\r\n")
        output.writeBytes("Content-Disposition: form-data; name=\"$fieldName\"\r\n\r\n")
        output.writeBytes(value)
        output.writeBytes("\r\n")
    }
}

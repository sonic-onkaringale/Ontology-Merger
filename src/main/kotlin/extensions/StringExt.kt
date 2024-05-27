package extensions

import okhttp3.MediaType
import okhttp3.RequestBody


fun String.toRequestBody(mediaType: MediaType): RequestBody
{

    return RequestBody.create(mediaType, this)
}
fun String.asInitials(limit: Int = 2): String
{

    val buffer = StringBuffer()
    trim().split(" ").filter {
        it.isNotEmpty()
    }.joinTo(
            buffer = buffer,
            limit = limit,
            separator = "",
            truncated = "",
            ) { s ->
        s.first().uppercase()
    }
    return buffer.toString()
}
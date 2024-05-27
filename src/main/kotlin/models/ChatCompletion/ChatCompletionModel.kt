package org.onkaringale.models.ChatCompletion

import com.google.gson.annotations.SerializedName

data class ChatCompletionRequest(

    @SerializedName("model") var model: String? = null,
    @SerializedName("messages") var messages: ArrayList<Messages> = arrayListOf(),
    @SerializedName("temperature") var temperature: Double? = null,
    @SerializedName("max_tokens") var maxTokens: Int? = null,
    @SerializedName("stream") var stream: Boolean? = null
)

data class ChatCompletionResponse(

    @SerializedName("id") var id: String? = null,
    @SerializedName("object") var objectType: String? = null,
    @SerializedName("created") var created: Int? = null,
    @SerializedName("model") var model: String? = null,
    @SerializedName("choices") var choices: ArrayList<Choices> = arrayListOf(),
    @SerializedName("usage") var usage: Usage? = Usage()

)

data class Choices(

    @SerializedName("index") var index: Int? = null,
    @SerializedName("message") var message: Messages = Messages(),
    @SerializedName("finish_reason") var finishReason: String? = null

)


data class Usage(

    @SerializedName("prompt_tokens") var promptTokens: Int? = null,
    @SerializedName("completion_tokens") var completionTokens: Int? = null,
    @SerializedName("total_tokens") var totalTokens: Int? = null

)


data class Messages(

    @SerializedName("role") var role: String? = null,
    @SerializedName("content") var content: String? = null

)
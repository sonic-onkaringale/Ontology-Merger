package org.onkaringale.api

import org.onkaringale.models.ChatCompletion.ChatCompletionRequest
import org.onkaringale.models.ChatCompletion.ChatCompletionResponse
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST


object Apis
{

    interface LlmApi
    {
        @Headers("Content-Type: application/json")
        @POST("v1/chat/completions")
        fun chatCompletion(@Body chatCompletionRequest: ChatCompletionRequest): Call<ChatCompletionResponse>

    }

    private var llmApi: LlmApi? = null

    fun getLLMApi(): LlmApi
    {
        if (llmApi == null)
        {
            llmApi = Retrofit
                .Builder()
                .baseUrl("http://localhost:1234")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(LlmApi::class.java)
        }
        return llmApi!!
    }

    private var multipleLLMApiInstance: List<LlmApi>? = null
    fun getMultipleLLMApis(): List<LlmApi>
    {
        if (multipleLLMApiInstance == null)
        {
            multipleLLMApiInstance = listOf(
                constructRetrofit("http://localhost:1234"),
            )
        }
        return multipleLLMApiInstance!!
    }

    fun constructRetrofit(baseUrl: String): LlmApi
    {
        return Retrofit
            .Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LlmApi::class.java)
    }

}


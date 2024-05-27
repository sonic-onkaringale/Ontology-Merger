package org.onkaringale.matching


import extensions.executeSync
import org.apache.jena.ontology.OntClass
import org.onkaringale.api.Apis
import org.onkaringale.models.ChatCompletion.ChatCompletionRequest
import org.onkaringale.models.ChatCompletion.Messages

object SemanticSimilarity
{
    val role = String
    val message = String
    private const val API_KEY = "your-api-key"
    private const val API_URL = "https://api.openai.com/v1/completions"

    //    List of Messages with Pair of <Role,Message>
    val messages = ArrayList<Pair<String, String>>()



    @Throws(Exception::class)
    fun areSemanticallySimilar(
        class1:OntClass,
        class2:OntClass
    ): Boolean
    {
        var class1Label: String=""
        var class1Description: String="null"
        var class2Label: String=""
        var class2Description: String="null"

        class1Label = class1.localName
        class2Label = class2.localName

        if (!class1.getComment(null).isNullOrBlank())
        {
            class1Description=class1.getComment(null)
        }
        if (!class2.getComment(null).isNullOrBlank())
        {
            class1Description=class2.getComment(null)
        }

        try
        {
//            println(class1.listProperties().toList().toString())
//            println(class1.listInstances().toList().toString())
//            println(class1.listDeclaredProperties().toList().toString())


        }catch (_:Exception)
        {

        }

        val isSimilar = askApi(class1Label, class1Description, class2Label, class2Description)
//        println("$class1Label , $class2Label  isMatch : $isSimilar")
        return isSimilar
    }

    @Throws(Exception::class)
    fun areSemanticallySimilar(
        class1Label: String,
        class1Description: String,
        class2Label: String,
        class2Description: String
    ): Boolean
    {
        return askApi(class1Label, class1Description, class2Label, class2Description)
    }

    private fun askApi(
        class1Label: String,
        class1Description: String,
        class2Label: String,
        class2Description: String
    ): Boolean
    {
        val llmApi = Apis.getLLMApi()
        val response = llmApi.chatCompletion(
            ChatCompletionRequest(
                "lmstudio-community/Meta-Llama-3-8B-Instruct-GGUF",
                arrayListOf(
                    Messages(
                        "system", "I will be providing two ontology classes class 1 and class 2.\n" +
                                "You have to answer Yes if class 2 is subclass of class 1, else answer No.\n" +
                                "Only answer in Yes or No."
                    ),
                    Messages(
                        "user",
                        "Class 1 Label : $class1Label,\n" +
                                "Class 1 description : $class1Description \n" +
                                "\n" +
                                "Class 2 Label : $class2Label,\n" +
                                "Class 2 description : $class2Description \n" +
                                "\n" +
                                "Is Class 2 subclass of class 1"
                    )
                ),
                0.7,
                -1,
                false
            )
        ).executeSync()
        if (response != null)
        {
            if (response.choices.last().message.content!!.lowercase().contains("yes"))
                return true
        }

        return false

//        val client = OkHttpClient()
//        val mediaType = "application/json".toMediaType()
//        val jsonBody = JsonObject()
//        jsonBody.apply {
//            put("model","lmstudio-community/Meta-Llama-3-8B-Instruct-GGUF")
//
//
//        }
//
//        val body =
//            "{ \n    \"model\": \"lmstudio-community/Meta-Llama-3-8B-Instruct-GGUF\",\n    \"messages\": [ \n        { \"role\": \"user\", \"content\": \"My Name is Onkar\" },\n      { \"role\": \"user\", \"content\": \"What is my name\" }\n    ], \n    \"temperature\": 0.7, \n    \"max_tokens\": -1,\n    \"stream\": false\n}"
//
//        val request = Request.Builder()
//            .url("http://localhost:1234/v1/chat/completions")
//            .post(
//                body.toRequestBody(
//                    mediaType
//                )
//            )
//            .addHeader("Content-Type", "application/json")
//            .build()
//        val response = client.newCall(request).execute()

    }

}

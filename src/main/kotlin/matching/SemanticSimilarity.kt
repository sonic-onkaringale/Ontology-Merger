package org.onkaringale.matching


import OntologyDetails
import com.knuddels.jtokkit.Encodings
import com.knuddels.jtokkit.api.*
import extensions.executeSync
import org.apache.jena.ontology.OntClass
import org.onkaringale.api.Apis
import org.onkaringale.models.ChatCompletion.ChatCompletionRequest
import org.onkaringale.models.ChatCompletion.Message


object SemanticSimilarity
{

    //    List of Messages with Pair of <Role,Message>
    val messages = ArrayList<Message>()


    @Throws(Exception::class)
    fun areSemanticallySimilar(
        class1: OntClass,
        class2: OntClass
    ): Boolean
    {
        var class1Label: String? = ""
        var class1Description: String = "null"
        var class2Label: String? = ""
        var class2Description: String = "null"

        class1Label = class1.getLabel(null) ?: class1.localName
        class2Label = class2.getLabel(null) ?: class2.localName
        if (class1Label == null || class2Label == null)
            return false


        if (!class1.getComment(null).isNullOrBlank())
        {
            class1Description = class1.getComment(null)
        }
        if (!class2.getComment(null).isNullOrBlank())
        {
            class1Description = class2.getComment(null)
        }

//        try
//        {
////            println(class1.listProperties().toList().toString())
////            println(class1.listInstances().toList().toString())
////            println(class1.listDeclaredProperties().toList().toString())
//
//
//        }
//        catch (_: Exception)
//        {
//
//        }

        val isSimilar = askApiOneToOne(class1Label, class1Description, class2Label, class2Description)
//        println("$class1Label , $class2Label  isMatch : $isSimilar")
        return isSimilar
    }

    fun areSemanticallySimilarGroupSearch(classes1: ArrayList<OntClass>, class2: OntClass): ArrayList<OntClass>
    {
        val narrowedSearchSpace = ArrayList<OntClass>()
        val batches = ArrayList<ArrayList<OntClass>>()
        if (getTotalTokenCount(classes1, class2) < OntologyDetails.tokenLimit)
        {
            val output = askApiOneToMany(classes1, class2)
            if (!output.isNullOrBlank())
            {
                val indexes = output.split(",")
                indexes.forEach { it ->
                    val i = it.toIntOrNull()
                    if (i != null)
                        narrowedSearchSpace.add(classes1[i])
                }
            }
        }
        else
        {
            println("Entering Batch Mode with ${classes1.size} classes")

            getBatches(class2, classes1, batches)
            println("Batching Finished with ${batches.size} batches")
            batches.forEach { batch ->
                val output = askApiOneToMany(batch, class2)
                if (!output.isNullOrBlank())
                {
                    val indexes = output.split(",")
                    indexes.forEach { it ->
                        val i = it.toIntOrNull()
                        if (i != null)
                            narrowedSearchSpace.add(batch[i])
                    }
                }
            }


        }
        return narrowedSearchSpace
    }

    private fun getBatches(
        class2: OntClass,
        classes1: ArrayList<OntClass>,
        batches: ArrayList<ArrayList<OntClass>>
    )
    {
        var tokenCount = 0
        var currentBatch = ArrayList<OntClass>()
        val totalTokenCountWithoutInput2 = getTotalTokenCountWithoutInput2(class2)
        for (i in classes1.indices)
        {
            var currentTokenCount = countTokens(getLabel(classes1[i]) ?: "")
            if (tokenCount + currentTokenCount + totalTokenCountWithoutInput2 > OntologyDetails.tokenLimit)
            {
                batches.add(currentBatch)
                println("Created batch of ${currentBatch.size}")
                currentBatch = ArrayList<OntClass>()
                tokenCount = 0
            }
            currentBatch.add(classes1[i])
            tokenCount += currentTokenCount
        }
        //            Add the last batch as it is surely below token limit
        if (currentBatch.isNotEmpty())
            batches.add(currentBatch)
    }

    fun getLabel(ontClass: OntClass): String?
    {
        return ontClass.getLabel(null) ?: ontClass.localName
    }

    fun getSystemMessageGroupSearch(): String
    {
        return "I will be providing a single class from Ontology ${OntologyDetails.ontology2} as Input 1 and multiple classes from Ontology ${OntologyDetails.ontology1} as Input 2.\n" +
                "The classes of Ontology ${OntologyDetails.ontology1} will be indexed.\n" +
                "You need to check if Input 1 is " +
                "similar to or a subclass of " +
                "any of the classes in Input 2 and output their indexes, separated by commas.\n" +
                "Only output the indexes. If there are none, output \"empty\" word."
    }

    fun getUserMessageGroupSearch(classes1: ArrayList<OntClass>, class2: OntClass): String?
    {
        if (classes1.isNotEmpty() && getLabel(class2) != null)
        {
            val input1 = "Input 1 : ${getLabel(class2)}"
            var input2 = "Input 2 : \n"

            for (i in classes1.indices)
            {
                if (getLabel(classes1[i]) != null)
                {
                    input2 += "\n $i. ${getLabel(classes1[i])} ${if (i == 0) "" else ","}"
                }
            }
            return input1 + "\n" + input2
        }
        return null
    }

    fun getTotalTokenCountWithoutInput2(class2: OntClass): Int
    {

        val input1 = "Input 1 : ${getLabel(class2)}"
        var input2 = "Input 2 : \n"


        return countTokens(getSystemMessageGroupSearch()) + countTokens(input1 + "\n" + input2)

    }


    fun getTotalTokenCount(classes1: ArrayList<OntClass>, class2: OntClass): Int
    {
        val userMessage = getUserMessageGroupSearch(classes1, class2) ?: return 0
        return countTokens(getSystemMessageGroupSearch() + userMessage)
    }

    fun countTokens(string: String): Int
    {
        val registry: EncodingRegistry = Encodings.newDefaultEncodingRegistry()
        val enc: Encoding = registry.getEncoding(EncodingType.CL100K_BASE)
        return enc.countTokens(string)
    }


    private fun askApiOneToOne(
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
                    Message(
                        "system", "I will be providing two ontology classes class 1 and class 2.\n" +
                                " Class 1 belongs to ${OntologyDetails.ontology1} Ontology and Class 2 belongs to ${OntologyDetails.ontology2} Ontology." +
                                "You have to answer Yes if class 2 is similar to or subclass of class 1, else answer No.\n" +
                                "Only answer in Yes or No."
                    ),
                    Message(
                        "user",
                        "Class 1 Label : $class1Label,\n" +
                                "Class 1 description : $class1Description \n" +
                                "\n" +
                                "Class 2 Label : $class2Label,\n" +
                                "Class 2 description : $class2Description \n" +
                                "\n" +
                                "Is Class 2 " +
//                                "similar to or" +
                                "subclass of " +
                                "class 1"
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

    private fun askApiOneToMany(
        classes1: ArrayList<OntClass>, class2: OntClass
    ): String?
    {
        if (getTotalTokenCount(classes1, class2) == 0)
            return null
        val llmApi = Apis.getLLMApi()
        val response = llmApi.chatCompletion(
            ChatCompletionRequest(
                "lmstudio-community/Meta-Llama-3-8B-Instruct-GGUF",
                arrayListOf(
                    Message(
                        "system", getSystemMessageGroupSearch()
                    ),
                    Message(
                        "user",
                        getUserMessageGroupSearch(classes1, class2)
                    )
                ),
                0.7,
                -1,
                false
            )
        ).executeSync()
        if (response != null)
        {
            if (response.choices.last().message.content == null)
                println("Null Content Received From LLM")
            val output = response.choices.last().message.content?.lowercase()
            if (output?.contains("empty") == true || output?.isBlank() == true)
                return null
            return output
        }

        return null


    }

}

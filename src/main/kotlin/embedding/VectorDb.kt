package embedding

import com.google.gson.JsonObject
import kotlinx.coroutines.*
import tech.amikos.chromadb.Client
import tech.amikos.chromadb.Collection
import tech.amikos.chromadb.EmbeddingFunction
import tech.amikos.chromadb.OpenAIEmbeddingFunction
import utils.Commons
import utils.queryElite

class VectorDb
{
    private var client: Client = Client("http://localhost:8000")
    private val apiKey = "lm-studio"
    private val ef: EmbeddingFunction =
        OpenAIEmbeddingFunction
            .Instance()
            .withModelName("CompendiumLabs/bge-large-en-v1.5-gguf")
            .withOpenAIAPIKey(apiKey)
            .withApiEndpoint("http://localhost:1234/v1/embeddings")
            .build();
    private val collection = client.createCollection("anatomy", null, true, ef)

    fun getClient(): Client
    {
        return client
    }

    fun getEmbeddingFunction(): EmbeddingFunction
    {
        return ef
    }

    fun getCollection(): Collection?
    {
        return collection
    }


    @OptIn(DelicateCoroutinesApi::class)
    fun add(documents:List<String>, ids:List<String>, metadata:List<Map<String,String>>?=null)
    {
        if (documents.size!=ids.size)
        {
            println("Documents and Ids size didn't match")
            return
        }
        if (metadata!=null)
        {
            if (documents.size!=metadata.size)
            {
                println("Documents and Metadata size didn't match")
                return
            }
        }

        println("Batching Started for ${documents.size} items")
        val batchSize =20
        val batchedDocuments = Commons.splitIntoBatches(documents,batchSize)
        val batchedIds = Commons.splitIntoBatches(ids,batchSize)
        val batchedMetadata = if (metadata==null) null else Commons.splitIntoBatches(metadata,batchSize)
        println("Batching Completed with ${batchedDocuments.size} batches with one batch of $batchSize items")
        for (i in batchedDocuments.indices)
        {
            val coroutineScope = GlobalScope.async {
                collection.add(
                    null,
                    batchedMetadata?.get(i),
                    batchedDocuments[i],
                    batchedIds[i]
                )
                if (i%5==0)
                    println("Batch $i Inserted")
                delay(300)
            }
            runBlocking {
                coroutineScope.await()
            }
        }
    }


    fun query(query:String,noResult:Int =10,whereMetadata:Map<String,String>?=null): Collection.QueryResponse?
    {
        return collection.query(mutableListOf(query), noResult, whereMetadata, null, null)
    }

    fun queryElite(query:String,noResult:Int =10,whereMetadata:JsonObject?=null,whereDocument:JsonObject?): Collection.QueryResponse?
    {
        return collection.queryElite(getEmbeddingFunction(),mutableListOf(query), noResult, whereMetadata, whereDocument, null)
    }

    fun reset()
    {
        client.reset()
    }
}
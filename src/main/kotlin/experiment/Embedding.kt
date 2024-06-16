package experiment

import embedding.VectorDb
import java.util.*
import kotlin.collections.HashMap

class Embedding
{
    fun init()
    {
        //    Initialize Vector DB
        //    val vectorDb = VectorDb()
        //    vectorDb.reset()

        //    vectorDb.add(listOf("Finger Nail"), listOf("human_finger_nail"), listOf(mapOf(Pair("type","Human"))))

        //    infiniteQuery(vectorDb)
    }

    private fun infiniteQuery(vectorDb: VectorDb)
    {
//    tech.amikos.chromadb.model.QueryEmbedding().where(
//
//    )
        val whereMetadata = HashMap<String, String>().apply {
            put("\$e", "{}")
        }
        while (true)
        {
            val reader = Scanner(System.`in`)
            println("Enter Query : ")
            val query = readLine()
            if (query != null)
            {
                if (query == "/q")
                    break
//            println(vectorDb.query(query, 20,whereMetadata) ?: "null")
                println(
                    vectorDb.queryElite(
                        query, 100, null,
                        null
                    )
                )
//            vectorDb.getCollection().upsert()
            }


        }
    }
}
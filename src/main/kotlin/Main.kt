package org.onkaringale

import embedding.VectorDb
import graphs.OntGraphUtils.convertToAdjacencyList
import graphs.OntGraphUtils.graphReports
import kotlinx.coroutines.*
import merge.MergeOntologiesBestEntry
import natureInspired.LoadBalancing
import org.apache.jena.ontology.OntModelSpec
import org.apache.jena.rdf.model.ModelFactory
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.collections.HashMap


@OptIn(DelicateCoroutinesApi::class)
fun main()
{
//    Initialize Vector DB
//    val vectorDb = VectorDb()
//    vectorDb.reset()

//    vectorDb.add(listOf("Finger Nail"), listOf("human_finger_nail"), listOf(mapOf(Pair("type","Human"))))

//    infiniteQuery(vectorDb)

//    simulateLoadBalance()


    val path1 = "C:\\Users\\ingal\\Downloads\\owl\\human.owl"
    val class1 = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM)
    class1.read(path1)

    val class1Copy = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM)
    class1Copy.read(path1)


    val path2 = "C:\\Users\\ingal\\Downloads\\owl\\mouse.owl"
    val class2 = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM)
    class2.read(path2)

    val graph1 = convertToAdjacencyList(class1)
    val graph2= convertToAdjacencyList(class2)
    graphReports(class1, class2, graph1, graph2)




    println("Started merging.")
    val humanMouseOnt = MergeOntologiesBestEntry(path1,path2)
    humanMouseOnt.mergeOntologies()
    println("Merging Complete.")

    println(convertToAdjacencyList(class1).toString())
    val outputStream = FileOutputStream(File("C:\\Users\\ingal\\Downloads\\owl\\mergedBAnatomy.owl"), false)
    class1.write(outputStream)


}

@OptIn(DelicateCoroutinesApi::class)
private fun simulateLoadBalance()
{
    val result = GlobalScope.async {
        LoadBalancing.simulate()
    }
    runBlocking {
        result.await()

    }
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




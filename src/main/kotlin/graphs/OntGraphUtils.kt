package graphs

import embedding.VectorDb
import org.apache.jena.ontology.OntClass
import org.apache.jena.ontology.OntModel
import org.apache.jena.rdf.model.Resource
import org.onkaringale.graphs.AdjacencyList
import org.onkaringale.graphs.Vertex
import utils.Commons
import utils.log
object OntGraphUtils
{
    fun countSubClasses(clazzOnt: OntClass, doesConsiderValidLabel:Boolean=true): Int
    {
        var count = 0
        fun countRecursion(clazz: OntClass)
        {
            clazz.listSubClasses().toList().forEach {

                if (!doesConsiderValidLabel||Commons.getLabel(it)!=null)
                    count++
                countRecursion(it)
            }

        }
        countRecursion(clazzOnt)
        return count
    }

    fun countUniqueSubClasses(clazzOnt: OntClass, model: OntModel, doesConsiderValidLabel:Boolean=true): Int
    {

        val hashSet=HashSet<String>()
        fun countRecursion(clazz: OntClass)
        {
            clazz.listSubClasses().toList().forEach {
                val hash = Commons.getHashOfClass(it,model)
                if (!hashSet.contains(hash))
                {
                    if (!doesConsiderValidLabel||Commons.getLabel(it)!=null)
                        hashSet.add(hash)
                }
                countRecursion(it)
            }

        }
        countRecursion(clazzOnt)
        return hashSet.count()
    }

    fun convertToAdjacencyList(
        model: OntModel,
        isPreReportEnabled: Boolean = false,
        vectorDb: VectorDb? = null,
        isTrueFiltering: Boolean = true
    ): AdjacencyList<String>
    {
        val adjacencyList = AdjacencyList<String>()

        val resourceToVertexMap = mutableMapOf<Resource, Vertex<String>>()

        val classHashMap = HashMap<String, OntClass>()
        if (isTrueFiltering)
        {
            model.listClasses().forEachRemaining {
                if (Commons.getLabel(it) != null)
                {
                    classHashMap[Commons.getHashOfClass(it, model)] = it
                }
            }
        }

        if (isPreReportEnabled)
        {
            log("=======Pre Report============")
            log("Ontologies : " + model.listOntologies().toList().toString())
//    log(model.listClasses().toList().toString())
            log("AllOntProperties : " + model.listAllOntProperties().toList().toString())
            log("NamedClasses : " + model.listNamedClasses().toList().toString())
            log("AnnotationProperties : " + model.listAnnotationProperties().toList().toString())
            log("ImportedOntologyURIs : " + model.listImportedOntologyURIs().toList().toString())
            log("SubModels : " + model.listSubModels().toList().toString())
            log("DataRanges : " + model.listDataRanges().toList().toString())
            log("Restrictions : " + model.listRestrictions().toList().toString())
            log("DatatypeProperties : " + model.listDatatypeProperties().toList().toString())
            log("AllOntProperties : " + model.listAllOntProperties().toList().toString())
            log("UnionClasses : " + model.listUnionClasses().toList().toString())
            log("ComplementClasses : " + model.listComplementClasses().toList().toString())
            log("ObjectProperties : " + model.listObjectProperties().toList().toString())
            log("=============================")
        }
//    For VectorDb
        val documents = ArrayList<String>()
        val ids = ArrayList<String>()
        val metadata = ArrayList<Map<String, String>>()


        // Iterate over all resources in the model
        model.listSubjects().forEachRemaining { resource ->
            if (resource.isAnon) return@forEachRemaining  // Skip blank nodes


            val label = resource.getProperty(org.apache.jena.vocabulary.RDFS.label)?.string
            val name = label ?: resource.localName
            var isValidInHashMap = false
            if (isTrueFiltering)
            {
                if (name != null && classHashMap.contains(Commons.getHashOfClass(name, model)))
                {
                    isValidInHashMap = true
                }
            }
            else
                isValidInHashMap = true
            if (name != null && isValidInHashMap)
            {
                val vertex = adjacencyList.createVertex(name)
                resourceToVertexMap[resource] = vertex

                if (vectorDb != null)
                {
                    documents.add(name)
                    ids.add(Commons.getHashOfClass(name, model))
                    metadata.add(HashMap<String, String>().apply {
                        put("type", Commons.getOntologyName(model))
                    })
                }
            }
        }

        // Iterate over all statements to add edges
        model.listStatements().forEachRemaining { statement ->
            try
            {
                val source = statement.subject
                val destination = statement.`object`.asResource() ?: return@forEachRemaining

                if (source.isAnon || destination.isAnon) return@forEachRemaining  // Skip blank nodes

                val sourceVertex = resourceToVertexMap[source]
                val destinationVertex = resourceToVertexMap[destination]

                if (sourceVertex != null && destinationVertex != null)
                {
//                adjacencyList.addDirectedEdge(sourceVertex, destinationVertex) //Real
                    adjacencyList.addDirectedEdge(destinationVertex, sourceVertex)
                }
            }
            catch (_: Exception)
            {

            }

        }

//    Adding in Vector Db

        if (vectorDb != null)
        {
            log("Started Insertion in Vector Database ${documents.size} .")
            vectorDb.add(documents, ids, metadata)
            log("Finished Insertion in Vector Database")
        }

        return adjacencyList
    }


    fun printBestEntry(
        model: OntModel,
        graph: AdjacencyList<String>,
        classHashMap: HashMap<String, OntClass>
        , graphName:String="Graph ")
    {
        log()
        log(
            "$graphName ${Commons.getOntologyName(model)} with total classes of ${
                model.listClasses().toList().filter { Commons.getLabel(it) != null }.size
            }"
        )
        val bestEntry = Commons.getHashOfClass(
//        graph.findTopNBestEntryPoints(2)[0].data
            graph.findBestEntryPoint()?.data!!, model
        )

        log(
            "Best Entry Point : $bestEntry with ${
                classHashMap[bestEntry]?.let {
                    OntGraphUtils.countUniqueSubClasses(
                        it,
                        model
                    )
                }
            } unique sub-classes and can reach " +
                    "${
                        classHashMap[bestEntry]?.let {
                            OntGraphUtils.countSubClasses(
                                it
                            )
                        }
                    }" +
                    " subclasses"
        )
        log()
        log("Best Entry Point SubClasses")
        classHashMap[bestEntry]!!
            .listSubClasses()
            .toList()
            .apply {
                log("Subclass Count $size")
            }
            .forEach {
                log(Commons.getLabel(it))
            }
        log()
    }

    fun graphReports(
        class1: OntModel,
        class2: OntModel,
        graph1: AdjacencyList<String>,
        graph2: AdjacencyList<String>
    )
    {
        val class1HashMap = HashMap<String, OntClass>()
        val class2HashMap = HashMap<String, OntClass>()

        class1.listClasses().forEachRemaining {
            if (Commons.getLabel(it) != null)
            {
                class1HashMap[Commons.getHashOfClass(it, class1)] = it
            }
        }

        class2.listClasses().forEachRemaining {
            if (Commons.getLabel(it) != null)
            {
                class2HashMap[Commons.getHashOfClass(it, class2)] = it
            }
        }

        printBestEntry(class1, graph1, class1HashMap, graphName = "G1")


        printBestEntry(class2, graph2, class2HashMap, "G2")

        log("Disconnected Sub Graphs in G1 Found : ${graph1.findDisconnectedSubgraphs().size}")
        log("Disconnected Sub Graphs in G2 Found : ${graph2.findDisconnectedSubgraphs().size}")
    }
}


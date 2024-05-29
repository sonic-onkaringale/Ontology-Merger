//package org.onkaringale
//
//import org.apache.jena.ontology.OntClass
//import org.apache.jena.ontology.OntModel
//import org.apache.jena.ontology.OntModelSpec
//import org.apache.jena.ontology.OntProperty
//import org.apache.jena.rdf.model.ModelFactory
//import org.apache.jena.rdf.model.Resource
//import org.onkaringale.graphs.AdjacencyList
//import org.onkaringale.graphs.Vertex
//import org.onkaringale.matching.SemanticSimilarity.areSemanticallySimilar
//import java.io.File
//import java.io.FileOutputStream
//
//
//fun main()
//{
////    val path = "C:\\Users\\ingal\\Downloads\\owl\\demo.owl"
////    val path = "C:\\Users\\ingal\\Downloads\\owl\\evidenceontology-master\\evidenceontology-master\\eco-basic.owl"
////    val model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM)
////    model.read(path)
////    val g = convertToAdjacencyList(model)
////    println(g.toString())
//
//    val path1 = "C:\\Users\\ingal\\Downloads\\owl\\car2.owl"
//    val class1 = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM)
//    class1.read(path1)
//
//    val class1Copy = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM)
//    class1Copy.read(path1)
//
//
//    val path2 = "C:\\Users\\ingal\\Downloads\\owl\\car1.owl"
//    val class2 = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM)
//    class2.read(path2)
//
//    println(convertToAdjacencyList(class1).toString())
//    println(convertToAdjacencyList(class2).toString())
//
//
//    println("Started merging.")
//    mergeOntologies(class1Copy, class1, class2)
//    println("Merging Complete.")
//
//    println(convertToAdjacencyList(class1).toString())
//    val outputStream = FileOutputStream(File("C:\\Users\\ingal\\Downloads\\owl\\mergedCars.owl"), false)
//    class1.write(outputStream)
//
//
//}
//
//fun convertToAdjacencyList(model: OntModel): AdjacencyList<String>
//{
//    val adjacencyList = AdjacencyList<String>()
//
//    val resourceToVertexMap = mutableMapOf<Resource, Vertex<String>>()
//
//    println("=======Pre Report============")
//    println("Ontologies : " + model.listOntologies().toList().toString())
////    println(model.listClasses().toList().toString())
//    println("AllOntProperties : " + model.listAllOntProperties().toList().toString())
//    println("NamedClasses : " + model.listNamedClasses().toList().toString())
//    println("AnnotationProperties : " + model.listAnnotationProperties().toList().toString())
//    println("ImportedOntologyURIs : " + model.listImportedOntologyURIs().toList().toString())
//    println("SubModels : " + model.listSubModels().toList().toString())
//    println("DataRanges : " + model.listDataRanges().toList().toString())
//    println("Restrictions : " + model.listRestrictions().toList().toString())
//    println("DatatypeProperties : " + model.listDatatypeProperties().toList().toString())
//    println("AllOntProperties : " + model.listAllOntProperties().toList().toString())
//    println("UnionClasses : " + model.listUnionClasses().toList().toString())
//    println("ComplementClasses : " + model.listComplementClasses().toList().toString())
//    println("ObjectProperties : " + model.listObjectProperties().toList().toString())
//    println("=============================")
//    // Iterate over all resources in the model
//    model.listSubjects().forEachRemaining { resource ->
//        if (resource.isAnon) return@forEachRemaining  // Skip blank nodes
//
//        val localName = resource.localName
//        val vertex = adjacencyList.createVertex(localName)
//        resourceToVertexMap[resource] = vertex
//    }
//
//
//    // Iterate over all statements to add edges
//    model.listStatements().forEachRemaining { statement ->
//        try
//        {
//            val source = statement.subject
//            val destination = statement.`object`.asResource() ?: return@forEachRemaining
//
//            if (source.isAnon || destination.isAnon) return@forEachRemaining  // Skip blank nodes
//
//            val sourceVertex = resourceToVertexMap[source]
//            val destinationVertex = resourceToVertexMap[destination]
//
//            if (sourceVertex != null && destinationVertex != null)
//            {
////                adjacencyList.addDirectedEdge(sourceVertex, destinationVertex)
//                adjacencyList.addDirectedEdge(destinationVertex, sourceVertex)
//            }
//        }
//        catch (_: Exception)
//        {
//
//        }
//
//    }
//
//    return adjacencyList
//}
//
//
//@Throws(java.lang.Exception::class)
//fun mergeOntologies(model1Copy: OntModel, model1: OntModel, model2: OntModel)
//{
//
//    val classesToMerge = model2.listClasses().toList()
//    for (class2 in classesToMerge)
//    {
//        mergeClassRecursive(model1Copy, model1, class2)
//    }
//    mergeAllProperties(model1, model2)
//}
//
//@Throws(java.lang.Exception::class)
//private fun mergeClassRecursive(model1Copy: OntModel, model1: OntModel, class2: OntClass)
//{
//    val bestMatch = findBestMatchRecursive(
//        model1Copy.listClasses().toList(),
//        model1.listClasses().toList().filter { it ->
//            isMerged(it)
//        }, class2
//    )
//    if (bestMatch != null)
//    {
//        val newClass2 = model1.createClass(class2.uri)
//        newClass2.addVersionInfo("${newClass2.versionInfo ?: ""}\n mergedFromLLM")
//        bestMatch.addSubClass(newClass2)
//        println("Merged ${class2.localName} with ${bestMatch.localName}")
//        // Recursively merge subclasses of class2 directly under newClass2
//        val subclasses = class2.listSubClasses().toList()
//        for (subclass in subclasses)
//        {
//            addSubClassRecursive(model1, newClass2, subclass)
//        }
//    }
//    else
//    {
////        model1.add(class2)
//    }
//}
//
//private fun isMerged(it: OntClass) = if (it.versionInfo?.contains("mergedFromLLM") != true)
//{
//    true
//}
//else
//{
//    false
//}
//
//@Throws(java.lang.Exception::class)
//private fun findBestMatchRecursive(
//    classList1Copy: List<OntClass>,
//    classList1: List<OntClass>,
//    candidateClass: OntClass
//): OntClass?
//{
//    var bestMatch: OntClass? = null
////    HashMap of Uri
//    val hashMap = HashMap<String, OntClass?>()
//
////    Map keys
//    for (i in classList1Copy.indices)
//    {
//        hashMap[classList1Copy[i].uri] = null
//    }
////    Put references
//    for (i in classList1.indices)
//    {
////        TODO add doesContainKey to find out the additional classes left
//        hashMap[classList1[i].uri] = classList1[i]
//    }
//
//
//    for (i in classList1Copy.indices)
//    {
//        if (
////            areSyntacticallySimilar(classList1Copy[i], candidateClass) ||
////            areStructurallySimilar(classList1Copy[i], candidateClass) ||
//            areSemanticallySimilar(classList1Copy[i], candidateClass))
//        {
//            if (classList1Copy[i].localName != hashMap[classList1Copy[i].uri]?.localName)
//            {
//                println("Sending Didn't Match,  Copy : ${classList1Copy[i].localName} , Actual : ${classList1[i].localName}")
//            }
//
//            bestMatch = hashMap[classList1Copy[i].uri]
//            val subClassesCopy = classList1Copy[i].listSubClasses().toList()
//            if (hashMap[classList1Copy[i].uri] == null)
//                println("Null found in hashmap")
//            val subClasses = hashMap[classList1Copy[i].uri]!!.listSubClasses().toList().filter { it ->
//                isMerged(it)
//            }
//            val deeperMatch = findBestMatchRecursive(subClassesCopy, subClasses, candidateClass)
//            if (deeperMatch != null)
//            {
//                bestMatch = deeperMatch
//            }
//        }
//    }
////    for (class1 in classList1)
////    {
////        if (areSyntacticallySimilar(class1, candidateClass) ||
////            areStructurallySimilar(class1, candidateClass) ||
////            areSemanticallySimilar(class1, candidateClass))
////        {
////            bestMatch = class1
////            val subClasses = class1.listSubClasses().toList()
////            val deeperMatch = findBestMatchRecursive(subClasses, candidateClass)
////            if (deeperMatch != null)
////            {
////                bestMatch = deeperMatch
////            }
////        }
////    }
//    return bestMatch
//}
//
//@Throws(java.lang.Exception::class)
//private fun addSubClassRecursive(model1: OntModel, parentClass: OntClass, subclass: OntClass)
//{
//    val newSubclass = model1.createClass(subclass.uri)
//
//    if (!parentClass.getComment(null).isNullOrBlank())
//        newSubclass.addComment(model1.createLiteral(parentClass.getComment(null)))
//
//    parentClass.listProperties().forEachRemaining { action ->
//
//        newSubclass.addProperty(action.predicate, action.`object`)
//    }
//
//    newSubclass.addVersionInfo("${newSubclass.versionInfo ?: ""}\n mergedFromLLM")
//
//
//    parentClass.addSubClass(newSubclass)
//
//
//    // Recursively add subclasses of subclass under newSubclass
//    val subSubclasses = subclass.listSubClasses().toList()
//    for (subSubclass in subSubclasses)
//    {
//        addSubClassRecursive(model1, newSubclass, subSubclass)
//    }
//}
//
//
//fun mergeAllProperties(model1: OntModel, model2: OntModel)
//{
//    val allProperties2 = model2.listAllOntProperties().toList()
//
//    for (prop2 in allProperties2)
//    {
//        val prop1 = model1.getOntProperty(prop2.uri)
//
//        if (prop1 == null)
//        {
//            // Property doesn't exist in model1, so create it
//            val newProp1 = model1.createOntProperty(prop2.uri)
//            copyPropertyDetails(model1, newProp1, prop2)
//            println("Added property ${prop2.uri} from model2 to model1")
//        }
//        else
//        {
//            // Property exists, merge additional information
//            copyPropertyDetails(model1, prop1, prop2)
//        }
//    }
//}
//
//private fun copyPropertyDetails(model1: OntModel, prop1: OntProperty, prop2: OntProperty)
//{
//    // Copy domains
//    val domains2 = prop2.listDomain().toList()
//    for (domain in domains2)
//    {
//        if (!prop1.hasDomain(domain))
//        {
//            prop1.addDomain(domain)
//            println("Added domain ${domain.uri} to property ${prop1.uri}")
//        }
//    }
//
//    // Copy ranges
//    val ranges2 = prop2.listRange().toList()
//    for (range in ranges2)
//    {
//        if (!prop1.hasRange(range))
//        {
//            prop1.addRange(range)
//            println("Added range ${range.uri} to property ${prop1.uri}")
//        }
//    }
//
//    // Copy other annotation properties
//    val annotationProps = model1.listAnnotationProperties().toList()
//    for (annProp in annotationProps)
//    {
//        val annotations = prop2.listPropertyValues(annProp).toList()
//        for (annotation in annotations)
//        {
//            if (!prop1.hasProperty(annProp, annotation))
//            {
//                prop1.addProperty(annProp, annotation)
//                println("Added annotation ${annProp.uri} with value ${annotation} to property ${prop1.uri}")
//            }
//        }
//    }
//}
//
//
//

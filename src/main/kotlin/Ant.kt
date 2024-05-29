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
//import kotlin.random.Random
//
//const val PHEROMONE_INIT = 1.0
//const val PHEROMONE_DECAY = 0.5
//const val PHEROMONE_INCREMENT = 1.0
//const val ALPHA = 1.0
//const val BETA = 2.0
//const val ITERATIONS = 100
//
//data class Ant(val path: MutableList<OntClass>, var current: OntClass)
//
//fun main() {
//    val path1 = "C:\\Users\\ingal\\Downloads\\owl\\car2.owl"
//    val class1 = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM)
//    class1.read(path1)
//
//    val class1Copy = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM)
//    class1Copy.read(path1)
//
//    val path2 = "C:\\Users\\ingal\\Downloads\\owl\\car1.owl"
//    val class2 = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM)
//    class2.read(path2)
//
//    println(convertToAdjacencyList(class1).toString())
//    println(convertToAdjacencyList(class2).toString())
//
//    println("Started merging.")
//    mergeOntologies(class1Copy, class1, class2)
//    println("Merging Complete.")
//
//    println(convertToAdjacencyList(class1).toString())
//    val outputStream = FileOutputStream(File("C:\\Users\\ingal\\Downloads\\owl\\mergedAntCars.owl"), false)
//    class1.write(outputStream)
//}
//
//fun convertToAdjacencyList(model: OntModel): AdjacencyList<String> {
//    val adjacencyList = AdjacencyList<String>()
//    val resourceToVertexMap = mutableMapOf<Resource, Vertex<String>>()
//
//    println("=======Pre Report============")
//    println("Ontologies : " + model.listOntologies().toList().toString())
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
//
//    model.listSubjects().forEachRemaining { resource ->
//        if (resource.isAnon) return@forEachRemaining
//        val localName = resource.localName
//        val vertex = adjacencyList.createVertex(localName)
//        resourceToVertexMap[resource] = vertex
//    }
//
//    model.listStatements().forEachRemaining { statement ->
//        try {
//            val source = statement.subject
//            val destination = statement.`object`.asResource() ?: return@forEachRemaining
//
//            if (source.isAnon || destination.isAnon) return@forEachRemaining
//
//            val sourceVertex = resourceToVertexMap[source]
//            val destinationVertex = resourceToVertexMap[destination]
//
//            if (sourceVertex != null && destinationVertex != null) {
//                adjacencyList.addDirectedEdge(destinationVertex, sourceVertex)
//            }
//        } catch (_: Exception) {
//        }
//    }
//
//    return adjacencyList
//}
//
//@Throws(java.lang.Exception::class)
//fun mergeOntologies(model1Copy: OntModel, model1: OntModel, model2: OntModel) {
//    val pheromones = mutableMapOf<Pair<OntClass, OntClass>, Double>()
//    val classesToMerge = model2.listClasses().toList()
//
//    for (class2 in classesToMerge) {
//        mergeClassACO(model1Copy, model1, class2, pheromones)
//    }
//    mergeAllProperties(model1, model2)
//}
//
//@Throws(java.lang.Exception::class)
//fun mergeClassACO(model1Copy: OntModel, model1: OntModel, class2: OntClass, pheromones: MutableMap<Pair<OntClass, OntClass>, Double>) {
//    val ants = initializeAnts(model1Copy, class2)
//    for (iteration in 1..ITERATIONS) {
//        for (ant in ants) {
//            moveAnt(ant, model1Copy, class2, pheromones)
//        }
//        updatePheromones(ants, pheromones)
//    }
//    val bestAnt = ants.maxByOrNull { ant -> calculateHeuristic(ant.current, class2) }
//    bestAnt?.let { bestMatch ->
//        println("Best match found: ${bestMatch.current.uri}")
//        if (bestMatch.path.isNotEmpty()) {
//            bestMatch.path.last().addSubClass(class2)
//            println("Added ${class2.uri} as subclass of ${bestMatch.path.last().uri}")
//        } else {
//            model1.createClass(class2.uri).addSubClass(class2)
//            println("Created new class ${class2.uri} and added as subclass")
//        }
//    }
//}
//
//fun initializeAnts(model1Copy: OntModel, class2: OntClass): List<Ant> {
//    val ants = mutableListOf<Ant>()
//    model1Copy.listClasses().forEachRemaining { ontClass ->
//        val ant = Ant(mutableListOf(), ontClass)
//        ants.add(ant)
//    }
//    return ants
//}
//
//fun moveAnt(ant: Ant, model1Copy: OntModel, class2: OntClass, pheromones: MutableMap<Pair<OntClass, OntClass>, Double>) {
//    val possibleMoves = ant.current.listSubClasses().toList()
//    if (possibleMoves.isEmpty()) return
//
//    val moveProbabilities = possibleMoves.map { subclass ->
//        val pheromone = pheromones.getOrDefault(Pair(ant.current, subclass), PHEROMONE_INIT)
//        val heuristic = calculateHeuristic(subclass, class2)
//        Math.pow(pheromone, ALPHA) * Math.pow(heuristic, BETA)
//    }
//
//    val totalProbability = moveProbabilities.sum()
//    val probabilities = moveProbabilities.map { it / totalProbability }
//    val nextMove = selectNextMove(possibleMoves, probabilities)
//
//    ant.path.add(nextMove)
//    ant.current = nextMove
//}
//
//fun updatePheromones(ants: List<Ant>, pheromones: MutableMap<Pair<OntClass, OntClass>, Double>) {
//    pheromones.keys.forEach { key ->
//        pheromones[key] = pheromones[key]!! * (1 - PHEROMONE_DECAY)
//    }
//
//    ants.forEach { ant ->
//        for (i in 0 until ant.path.size - 1) {
//            val pair = Pair(ant.path[i], ant.path[i + 1])
//            pheromones[pair] = pheromones.getOrDefault(pair, PHEROMONE_INIT) + PHEROMONE_INCREMENT
//        }
//    }
//}
//
//fun calculateHeuristic(ontClass: OntClass, targetClass: OntClass): Double {
//    var similarity = 0.0
//    if (areSemanticallySimilar(ontClass, targetClass)) similarity += 1.0
//    return similarity
//}
//
//fun selectNextMove(possibleMoves: List<OntClass>, probabilities: List<Double>): OntClass {
//    val cumulativeProbabilities = probabilities.scan(0.0) { acc, prob -> acc + prob }
//    val randomValue = Random.nextDouble()
//
//    for (i in 1 until cumulativeProbabilities.size) {
//        if (randomValue < cumulativeProbabilities[i]) {
//            return possibleMoves[i - 1]
//        }
//    }
//    return possibleMoves.last()
//}
//
//private fun isMerged(it: OntClass) = it.versionInfo?.contains("mergedFromLLM") != true
//
//@Throws(java.lang.Exception::class)
//private fun findBestMatchRecursive(
//    classList1Copy: List<OntClass>,
//    classList1: List<OntClass>,
//    candidateClass: OntClass
//): OntClass? {
//    var bestMatch: OntClass? = null
//    val hashMap = HashMap<String, OntClass?>()
//
//    for (i in classList1Copy.indices) {
//        hashMap[classList1Copy[i].uri] = null
//    }
//
//    for (i in classList1.indices) {
//        hashMap[classList1[i].uri] = classList1[i]
//    }
//
//    for (i in classList1Copy.indices) {
//        if (areSemanticallySimilar(classList1Copy[i], candidateClass)) {
//            if (classList1Copy[i].localName != hashMap[classList1Copy[i].uri]?.localName) {
//                println("Sending Didn't Match,  Copy : ${classList1Copy[i].localName} , Actual : ${classList1[i].localName}")
//            }
//
//            bestMatch = hashMap[classList1Copy[i].uri]
//            val subClassesCopy = classList1Copy[i].listSubClasses().toList()
//            if (hashMap[classList1Copy[i].uri] == null) println("Null found in hashmap")
//            val subClasses = hashMap[classList1Copy[i].uri]!!.listSubClasses().toList().filter { isMerged(it) }
//            val deeperMatch = findBestMatchRecursive(subClassesCopy, subClasses, candidateClass)
//            if (deeperMatch != null) {
//                bestMatch = deeperMatch
//            }
//        }
//    }
//    return bestMatch
//}
//
//@Throws(java.lang.Exception::class)
//private fun addSubClassRecursive(model1: OntModel, parentClass: OntClass, subclass: OntClass) {
//    val newSubclass = model1.createClass(subclass.uri)
//
//    if (!parentClass.getComment(null).isNullOrBlank())
//        newSubclass.addComment(model1.createLiteral(parentClass.getComment(null)))
//
//    parentClass.listProperties().forEachRemaining { action ->
//        newSubclass.addProperty(action.predicate, action.`object`)
//    }
//
//    newSubclass.addVersionInfo("${newSubclass.versionInfo ?: ""}\n mergedFromLLM")
//
//    parentClass.addSubClass(newSubclass)
//
//    val subSubclasses = subclass.listSubClasses().toList()
//    for (subSubclass in subSubclasses) {
//        addSubClassRecursive(model1, newSubclass, subSubclass)
//    }
//}
//
//fun mergeAllProperties(model1: OntModel, model2: OntModel) {
//    val allProperties2 = model2.listAllOntProperties().toList()
//
//    for (prop2 in allProperties2) {
//        val prop1 = model1.getOntProperty(prop2.uri)
//
//        if (prop1 == null) {
//            val newProp1 = model1.createOntProperty(prop2.uri)
//            copyPropertyDetails(model1, newProp1, prop2)
//            println("Added property ${prop2.uri} from model2 to model1")
//        } else {
//            copyPropertyDetails(model1, prop1, prop2)
//        }
//    }
//}
//
//private fun copyPropertyDetails(model1: OntModel, prop1: OntProperty, prop2: OntProperty) {
//    val domains2 = prop2.listDomain().toList()
//    for (domain in domains2) {
//        if (!prop1.hasDomain(domain)) {
//            prop1.addDomain(domain)
//            println("Added domain ${domain.uri} to property ${prop1.uri}")
//        }
//    }
//
//    val ranges2 = prop2.listRange().toList()
//    for (range in ranges2) {
//        if (!prop1.hasRange(range)) {
//            prop1.addRange(range)
//            println("Added range ${range.uri} to property ${prop1.uri}")
//        }
//    }
//
//    val annotationProps = model1.listAnnotationProperties().toList()
//    for (annProp in annotationProps) {
//        val annotations = prop2.listPropertyValues(annProp).toList()
//        for (annotation in annotations) {
//            if (!prop1.hasProperty(annProp, annotation)) {
//                prop1.addProperty(annProp, annotation)
//                println("Added annotation ${annProp.uri} with value $annotation to property ${prop1.uri}")
//            }
//        }
//    }
//}

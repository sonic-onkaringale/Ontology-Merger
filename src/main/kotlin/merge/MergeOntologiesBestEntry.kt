@file:Suppress("DuplicatedCode")

package merge

import OntologyDetails
import graphs.OntGraphUtils
import kotlinx.coroutines.runBlocking
import natureInspired.LoadBalancing
import org.apache.jena.ontology.OntClass
import org.apache.jena.ontology.OntModel
import org.apache.jena.ontology.OntProperty
import org.onkaringale.graphs.AdjacencyList
import utils.*
import java.io.File
import java.io.FileOutputStream
import java.text.DateFormat
import java.time.Instant
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet

class MergeOntologiesBestEntry
{
    //    All Models
    private var model1: OntModel
    private var model1Copy: OntModel
    private var model2: OntModel

    //    All Graphs
    private var model1Graph: AdjacencyList<String>? = null
    private var model1CopyGraph: AdjacencyList<String>
    private var model2Graph: AdjacencyList<String>

    // Hash Map of Class 1 Copy Hash and A Pair of Class 1 Copy(first) and Class 1(second)
    private val model1Map = HashMap<String, Pair<OntClass, OntClass>>()

    //    Maps of Hash of Class and Count
    val model1SubclassesCountMap = HashMap<String, Int>()
    val model2SubclassesCountMap = HashMap<String, Int>()

    //    Used to remove already merged class while merging. For Add SubClassRecursive
    private val toRemove = HashSet<OntClass>()

    //    HashMap of class2 Hash and A Pair of Old Class from Model 2 and New Class from Model 1
    private val mergedClassesMap = HashMap<String, Pair<OntClass, OntClass>>()

    //    Timers
    private var startTime: Date = Date()
    private var endTime: Date = Date()

    //    Flags
    private var isOntologyMerged = false

    constructor(file1Path: String, file2Path: String)
    {
        model1 = Commons.readOntologyFromFile(file1Path) ?: throw RuntimeException("File 1 path isn't a valid owl file")
        model1Copy =
            Commons.readOntologyFromFile(file1Path) ?: throw RuntimeException("File 1 path isn't a valid owl file")
        model2 = Commons.readOntologyFromFile(file2Path) ?: throw RuntimeException("File 2 path isn't a valid owl file")
        model1CopyGraph = OntGraphUtils.convertToAdjacencyList(model1Copy)
        model2Graph = OntGraphUtils.convertToAdjacencyList(model2)

        model1Copy.listClasses().toList().forEach { it ->
            model1SubclassesCountMap[it.getHash(model1Copy)] = OntGraphUtils.countSubClasses(it)
        }
        model2.listClasses().toList().forEach { it ->
            model2SubclassesCountMap[it.getHash(model1Copy)] = OntGraphUtils.countSubClasses(it)
        }

    }

    fun getModel1Graph(): AdjacencyList<String>
    {
        return model1CopyGraph
    }

    fun getModel2Graph(): AdjacencyList<String>
    {
        return model2Graph
    }

    fun getMergedGraph(): AdjacencyList<String>?
    {
        if (!isOntologyMerged)
        {
            log("Model is not merged, call mergeOntologies() to merge Ontologies")
            return null
        }
        return model1Graph
    }


    fun getModel1(): OntModel
    {
        return model1Copy
    }

    fun getModel2(): OntModel
    {
        return model2
    }

    fun getMergedModel(): OntModel?
    {
        if (!isOntologyMerged)
        {
            log("Model is not merged, call mergeOntologies() to merge Ontologies")
            return null
        }
        return model1Copy
    }

    fun mergeOntologies()
    {
        mergeOntologies(model1Copy, model1, model2)
    }

    fun printTimeTookToMerge()
    {
        if (!isOntologyMerged)
        {
            log("Model is not merged, call mergeOntologies() to merge Ontologies")
            return
        }
        val timeTook = "Merging Completed in  " + Commons.getDateTimeDifference(startTime, endTime)
        log(timeTook)
    }

    fun printClassesForBottomUp()
    {
        var classesToMerge = model2.listClasses().toList().filter {
            it.getLabelElite() != null
        }.toMutableList()
        classesToMerge = classesToMerge.sortedBy {
            model2SubclassesCountMap[it.getHash(model2)]
        }.toMutableList()
        for (c in classesToMerge)
        {
            println(c.getLabelElite())
        }
    }


    @Throws(java.lang.Exception::class)
    private fun mergeOntologies(model1Copy: OntModel, model1: OntModel, model2: OntModel)
    {
        startTime = Date(Instant.now().toEpochMilli())

        if (isOntologyMerged)
        {
            log("Model is already Merged")
            return
        }
        log(
            "Merging with "
                    + if (OntologyDetails.mergingApproach == OntologyDetails.MergingApproach.TOP_BOTTOM)
                "Top to Bottom Approach"
            else if (OntologyDetails.mergingApproach == OntologyDetails.MergingApproach.BOTTOM_UP)
                "Bottom to Up Approach"
            else
                "Unknown Approach"
        )

        var classesToMerge = model2.listClasses().toList().filter {
            it.getLabelElite() != null
        }.toMutableList()
        log("Model 1 : ${OntologyDetails.ontology1} with ${model1.listClasses().toList().size} classes")
        log("Model 2 : ${OntologyDetails.ontology2} with ${classesToMerge.size} classes")

        val graph1 = model1CopyGraph


        val model1Classes = model1.listClasses().toList()
        val model1CopyClasses = model1Copy.listClasses().toList()
        for (i in model1CopyClasses.indices)
        {
            if (Commons.getLabel(model1CopyClasses[i]) != null)
            {
                model1Map[model1CopyClasses[i].getHash(model1Copy)] =
                    Pair(model1CopyClasses[i], model1Classes[i])
            }
        }


        val bestEntryHash =
            graph1.findBestEntryPoint()?.data?.let { Commons.getHashOfClass(it, model1Copy) }
        if (bestEntryHash == null)
        {
            throw RuntimeException(
                "Best entry not found"
            )
        }
        if (!model1Map.contains(bestEntryHash))
        {
            throw RuntimeException(
                "Best entry found but didnt found in HashMap"
            )
        }

        if (OntologyDetails.mergingApproach == OntologyDetails.MergingApproach.TOP_BOTTOM)
        {
            classesToMerge = classesToMerge.sortedByDescending {
                model2SubclassesCountMap[it.getHash(model2)]
            }.toMutableList()
        }
        else if (OntologyDetails.mergingApproach == OntologyDetails.MergingApproach.BOTTOM_UP)
        {
            classesToMerge = classesToMerge.sortedBy {
                model2SubclassesCountMap[it.getHash(model2)]
            }.toMutableList()
        }


        var isReachedEnd = false
//        While loop is to not contaminate indexes because we remove some elements while iterating in for loop
        while (true)
        {
            for (i in classesToMerge.indices)
            {
                val class2 = classesToMerge[i]
                log(" $i / ${classesToMerge.lastIndex}")
                val isMerged = mergeClassRecursive(model1Copy, model1, class2, bestEntryHash, model1Map)
                if (isMerged)
                {
                    if (OntologyDetails.mergingApproach == OntologyDetails.MergingApproach.TOP_BOTTOM)
                    {
                        classesToMerge.removeAll(toRemove)
                        toRemove.clear()
                        break
                    }
                }
                else
                {
                    if (OntologyDetails.mergingApproach == OntologyDetails.MergingApproach.TOP_BOTTOM)
                    {
                        toRemove.add(class2)
                    }
                }
                if (i == classesToMerge.lastIndex)
                    isReachedEnd = true
            }
            if (isReachedEnd || classesToMerge.isNullOrEmpty())
                break
        }
        mergeAllProperties(model1, model2)


        model1Graph = OntGraphUtils.convertToAdjacencyList(model1)
        isOntologyMerged = true

        val loc = Locale.ENGLISH
        val time = Commons.toSafeFileName(
            DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG, loc)
                .format(Date(Instant.now().toEpochMilli()))
        )
        save(
            OntologyDetails.saveFolder + Commons.toSafeFileName(
                OntologyDetails.ontology1.lowercase() + "_" + OntologyDetails.ontology2.lowercase() + "merged_" + time + "_" +
                        (if (OntologyDetails.mergingApproach == OntologyDetails.MergingApproach.TOP_BOTTOM)
                            "Top_to_Bottom_Approach"
                        else if (OntologyDetails.mergingApproach == OntologyDetails.MergingApproach.BOTTOM_UP)
                            "Bottom_to_Up_Approach"
                        else
                            "Unknown_Approach")
                        + ".owl"
            )
        )
        endTime = Date(Instant.now().toEpochMilli())
        printMergeReport()
    }

    private fun printMergeReport()
    {
        log("==============Merge Report Start==============")

        log("")
        printTimeTookToMerge()
        log("")

        val totalClassesInModel1 = model1Copy.listClasses().toList().size
        val totalClassesInModel2 = model2.listClasses().toList().size
        log("Number of Classes in ${model1Copy.getName()}  : $totalClassesInModel1")
        log("Number of Classes in ${model2.getName()}  : $totalClassesInModel2")

        val bestEntryHash = model1CopyGraph.findBestEntryPoint()!!.data.let { Commons.getHashOfClass(it, model1Copy) }
        val reachedNodes = OntGraphUtils.countUniqueSubClasses(
            model1Map[bestEntryHash]!!.first,
            model1Copy
        )

        log("Number of Classes Excluded from ${model1Copy.getName()} because they weren't subclass of Best Entry Point : ${totalClassesInModel1 - reachedNodes}")

        log("Total Classes Merged : ${mergedClassesMap.size}")
        log("Total Classes Didn't Semantically Match : ${totalClassesInModel2 - mergedClassesMap.size}")

        val redundantMergedCheck = HashMap<OntClass, Int>()
        model1.listClasses().toList().forEach {
            if (redundantMergedCheck.contains(it))
            {
                redundantMergedCheck[it] = redundantMergedCheck[it]!! + 1
            }
            else
            {
                redundantMergedCheck[it] = 1
            }
        }
        var redundantMergedCount = 0
        redundantMergedCheck.forEach { (key, value) ->
            if (value > 1)
                redundantMergedCount++
        }

        log("Total Number of Redundant Merging(Containing Same Hash Value) : $redundantMergedCount")

        log("==============Merge Report End==============")
    }

    @Throws(java.lang.Exception::class)
    private fun mergeClassRecursive(
        model1Copy: OntModel,
        model1: OntModel,
        class2: OntClass,
        bestEntryHash: String,
        model1Map: HashMap<String, Pair<OntClass, OntClass>>
    ): Boolean
    {
        val visitedNodes = HashSet<String>()
        val bestMatch = findBestMatchRecursive(
            model1Map[bestEntryHash]!!.first.listSubClasses().toList(),
            class2,
            model1Map,
            model1Copy,
            visitedNodes
        )
        if (bestMatch != null)
        {
            val newClass2 = model1.createClass(class2.uri)
            newClass2.addVersionInfo("${newClass2.versionInfo ?: ""}\n mergedFromLLM")
            copyPropertiesClassToClass(newClass2,class2,model1)
            bestMatch.addSubClass(newClass2)
            mergedClassesMap[class2.getHash(model2)] = Pair(class2, newClass2)
            if (OntologyDetails.mergingApproach == OntologyDetails.MergingApproach.TOP_BOTTOM)
            {
                toRemove.add(class2)
                // Recursively merge subclasses of class2 directly under newClass2
                val subclasses = class2.listSubClasses().toList().sortedByDescending {
                    model2SubclassesCountMap[it.getHash(model2)]
                }
                for (subclass in subclasses)
                {
                    addSubClassRecursive(model1, newClass2, subclass)
                }
            }
            if (OntologyDetails.mergingApproach == OntologyDetails.MergingApproach.BOTTOM_UP)
            {
//                Implement addSuperClassRecursive Function Here
                val superClasses = class2.listSuperClasses().toList().sortedBy {
                    model2SubclassesCountMap[it.getHash(model2)]
                }

                for (superclass in superClasses)
                {
                    addSuperClassRecursive(newClass2, superclass)
                }
            }

            log("Merged ${class2.getLabelElite()} with ${bestMatch.getLabelElite()}")
            return true
        }
        else
        {
//        model1.add(class2)
            return false
        }
    }

    @Throws(java.lang.Exception::class)
    private fun findBestMatchRecursive(
        classList1Copy: List<OntClass>,
        candidateClass: OntClass,
        model1Map: HashMap<String, Pair<OntClass, OntClass>>,
        model1Copy: OntModel, visitedNodes: HashSet<String>
    ): OntClass?
    {
        var bestMatch: OntClass? = null

        log("Comparing ${candidateClass.getLabelElite()} to  ${classList1Copy.size} classes")

        val classList1CopyFiltered = classList1Copy.filter {
            if (visitedNodes.contains(it.getHash(model1Copy)))
                false
            else if (it.getLabelElite() == null)
                false
            else
            {
                visitedNodes.add(it.getHash(model1Copy))
                true
            }
        }
        val semanticResults = runBlocking {
            LoadBalancing.areSemanticallySimilarLoadBalanced(classList1CopyFiltered, candidateClass)
        }


        for (i in classList1CopyFiltered.indices)
        {

            if (
//            areSyntacticallySimilar(classList1CopyFiltered[i], candidateClass) ||
//            areStructurallySimilar(classList1CopyFiltered[i], candidateClass) ||
//                SemanticSimilarity.areSemanticallySimilar(classList1CopyFiltered[i], candidateClass))
                semanticResults[i]
            )
            {

                if (model1Map[classList1CopyFiltered[i].getHash(model1Copy)]?.second == null) log("Null found in hashmap")
                bestMatch = model1Map[classList1CopyFiltered[i].getHash(model1Copy)]!!.second

                val subClassesCopy = classList1CopyFiltered[i].listSubClasses().toList().filter {
                    it.getLabelElite() != null
                }

                val deeperMatch = findBestMatchRecursive(
                    subClassesCopy,
                    candidateClass,
                    model1Map,
                    model1Copy,
                    visitedNodes
                )
                if (deeperMatch != null)
                {
                    bestMatch = deeperMatch
                }
            }
        }

        return bestMatch
    }

    private fun isNotMerged(it: OntClass) = if (it.versionInfo?.contains("mergedFromLLM") != true)
    {
        true
    }
    else
    {
        false
    }

    @Throws(java.lang.Exception::class)
    private fun addSubClassRecursive(
        model1: OntModel,
        newSubClass: OntClass,//New Class
        oldSubclass: OntClass //Old Class
    )
    {
        toRemove.add(oldSubclass)
        val newSubclass = model1.createClass(oldSubclass.uri)

        copyPropertiesClassToClass(newSubclass, oldSubclass, model1)

        newSubClass.addSubClass(oldSubclass)


        // Recursively add subclasses of subclass under newSubclass
        val subSubclasses = oldSubclass.listSubClasses().toList().sortedByDescending {
            model2SubclassesCountMap[it.getHash(model2)]
        }
        for (subSubclass in subSubclasses)
        {
            addSubClassRecursive(model1, newSubclass, subSubclass)
        }
    }

    private fun copyPropertiesClassToClass(
        destinationClass: OntClass,
        sourceClass: OntClass,
        mergingModel: OntModel
    )
    {
        destinationClass.addLabel(sourceClass.getLabelElite(), null)
        if (!sourceClass.getComment(null)
                .isNullOrBlank()) destinationClass.addComment(mergingModel.createLiteral(sourceClass.getComment(null)))
        sourceClass.listProperties().forEachRemaining { action ->

            destinationClass.addProperty(action.predicate, action.`object`)
        }

        destinationClass.addVersionInfo("${sourceClass.versionInfo ?: ""}\n mergedFromLLM")
    }

    private fun addSuperClassRecursive(
        childClass: OntClass//New Class
        , superclass: OntClass //Old Class
    )
    {
        if (superclass.getLabelElite() == null)
            return
        if (superclass.getLabelElite() == "Thing" || childClass.getLabelElite() == "Thing")
            return
        val superclassHash = superclass.getHash(model1)
        val existingMergedClass = mergedClassesMap[superclassHash]

//        Linking existing classes

//        for ((_, pairOfOldM2AndNewM1Class) in mergedClassesMap)
//        {
//            if (isSuperClassOf(superclass, pairOfOldM2AndNewM1Class.first))
//            {
//                if (pairOfOldM2AndNewM1Class.first.getLabelElite() == "Thing")
//                    continue
//                childClass.addSuperClass(pairOfOldM2AndNewM1Class.second)
//                log("Linked ${pairOfOldM2AndNewM1Class.second.getLabelElite()} as super class of <- ${childClass.getLabelElite()}")
//            }
//        }

        if (existingMergedClass != null)
        {
            // Add the existing merged class as a superclass
            childClass.addSuperClass(existingMergedClass.second)

            log("Found existing merged superclass ${existingMergedClass.second.getLabelElite()} to ${childClass.getLabelElite()}")
        }
        else
        {
            // Add the superclass to the removal list to prevent re-merging
            toRemove.add(superclass)

            // Create a new class in model1 with the URI of the superclass
            val newSuperclass = model1.createClass(superclass.uri)

            copyPropertiesClassToClass(newSuperclass,superclass,model1)

            // Add the new superclass to the child class
            childClass.addSuperClass(newSuperclass)

            // Add the new superclass to the mergedClassesMap
            mergedClassesMap[superclassHash] = Pair(superclass, newSuperclass)

            // Recursively add superclasses of the superclass under the newSuperclass
            val superSuperclasses = superclass.listSuperClasses().toList().sortedBy {
                model2SubclassesCountMap[it.getHash(model2)]
            }
            for (superSuperclass in superSuperclasses)
            {
                addSuperClassRecursive(newSuperclass, superSuperclass)
            }
        }
    }


    // Helper function to check if a class is a superclass of another class
    private fun isSuperClassOf(superclass: OntClass, subclass: OntClass): Boolean
    {
        val superClasses = subclass.listSuperClasses().toList()
        if (superClasses.contains(superclass))
        {
            return true
        }
        for (superClass in superClasses)
        {
            if (isSuperClassOf(superclass, superClass))
            {
                return true
            }
        }
        return false
    }

    private fun mergeAllProperties(model1: OntModel, model2: OntModel)
    {
        val allProperties2 = model2.listAllOntProperties().toList()

        for (prop2 in allProperties2)
        {
            val prop1 = model1.getOntProperty(prop2.uri)

            if (prop1 == null)
            {
                // Property doesn't exist in model1, so create it
                val newProp1 = model1.createOntProperty(prop2.uri)
                copyPropertyDetails(model1, newProp1, prop2)
                log("Added property ${prop2.uri} from model2 to model1")
            }
            else
            {
                // Property exists, merge additional information
                copyPropertyDetails(model1, prop1, prop2)
            }
        }
    }

    private fun copyPropertyDetails(model1: OntModel, prop1: OntProperty, prop2: OntProperty)
    {
        // Copy domains
        val domains2 = prop2.listDomain().toList()
        for (domain in domains2)
        {
            if (!prop1.hasDomain(domain))
            {
                prop1.addDomain(domain)
                log("Added domain ${domain.uri} to property ${prop1.uri}")
            }
        }

        // Copy ranges
        val ranges2 = prop2.listRange().toList()
        for (range in ranges2)
        {
            if (!prop1.hasRange(range))
            {
                prop1.addRange(range)
                log("Added range ${range.uri} to property ${prop1.uri}")
            }
        }

        // Copy other annotation properties
        val annotationProps = model1.listAnnotationProperties().toList()
        for (annProp in annotationProps)
        {
            val annotations = prop2.listPropertyValues(annProp).toList()
            for (annotation in annotations)
            {
                if (!prop1.hasProperty(annProp, annotation))
                {
                    prop1.addProperty(annProp, annotation)
                    log("Added annotation ${annProp.uri} with value ${annotation} to property ${prop1.uri}")
                }
            }
        }
    }


    fun save(filePath: String)
    {
        val outputStream = FileOutputStream(File(filePath), false)
        model1.write(outputStream)
        log("Merged Model Saved at $filePath")
    }
}
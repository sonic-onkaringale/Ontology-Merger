@file:Suppress("DuplicatedCode")

package merge

import OntologyDetails
import org.apache.jena.ontology.OntClass
import org.apache.jena.ontology.OntModel
import org.apache.jena.ontology.OntProperty
import org.onkaringale.matching.SemanticSimilarity
import utils.Commons

object MergeOntologiesEmbedding
{
    @Throws(java.lang.Exception::class)
    fun mergeOntologies(model1Copy: OntModel, model1: OntModel, model2: OntModel)
    {

        val classesToMerge = model2.listClasses().toList()
        println("Model 1 : ${OntologyDetails.ontology1} with ${model1.listClasses().toList().size} classes")
        println("Model 2 : ${OntologyDetails.ontology2} with ${classesToMerge.size} classes")
//        TODO : Create HashMap of model 1 Copy and Copy
//        val model1Map = HashMap<String,Pair<OntClass,OntClass>>()


        for (class2 in classesToMerge)
        {
            mergeClassRecursive(model1Copy, model1, class2)
        }
        mergeAllProperties(model1, model2)
    }

    @Throws(java.lang.Exception::class)
    private fun mergeClassRecursive(model1Copy: OntModel, model1: OntModel, class2: OntClass)
    {
//        TODO : Create isNodeVisited HashMap/Set
        val bestMatch = findBestMatchRecursive(
            model1Copy.listClasses().toList(), model1.listClasses().toList().filter { it ->
                isNotMerged(it)
            }, class2
        )
        if (bestMatch != null)
        {
            val newClass2 = model1.createClass(class2.uri)
            newClass2.addVersionInfo("${newClass2.versionInfo ?: ""}\n mergedFromLLM")
            bestMatch.addSubClass(newClass2)
            println("Merged ${class2.localName} with ${bestMatch.localName}")
            // Recursively merge subclasses of class2 directly under newClass2
            val subclasses = class2.listSubClasses().toList()
            for (subclass in subclasses)
            {
                addSubClassRecursive(model1, newClass2, subclass)
            }
        }
        else
        {
//        model1.add(class2)
        }
    }

    @Throws(java.lang.Exception::class)
    private fun findBestMatchRecursive(
        classList1Copy: List<OntClass>, classList1: List<OntClass>, candidateClass: OntClass
    ): OntClass?
    {
        var bestMatch: OntClass? = null
//    HashMap of Uri
        val hashMap = HashMap<String, OntClass?>()

//    Map keys
        for (i in classList1Copy.indices)
        {
            if ((classList1Copy[i].uri ?: classList1Copy[i].getLabel(null)) != null)
                hashMap[classList1Copy[i].uri
                ?: classList1Copy[i].getLabel(null)] = null
        }
//    Put references
        for (i in classList1.indices)
        {
//        TODO add doesContainKey to find out the additional classes left
            if ((classList1[i].uri ?: classList1[i].getLabel(null)) != null) hashMap[classList1[i].uri
                ?: classList1[i].getLabel(null)] = classList1[i]
        }


        for (i in classList1Copy.indices)
        {
            if (
//            areSyntacticallySimilar(classList1Copy[i], candidateClass) ||
//            areStructurallySimilar(classList1Copy[i], candidateClass) ||
                SemanticSimilarity.areSemanticallySimilar(classList1Copy[i], candidateClass))
            {
//            if (classList1Copy[i].localName != hashMap[classList1Copy[i].uri]?.localName)
//            {
//                println("Sending Didn't Match,  Copy : ${classList1Copy[i].localName} , Actual : ${classList1[i].localName}")
//            }

                bestMatch = hashMap[classList1Copy[i].uri ?: classList1Copy[i].getLabel(null)]

                val subClassesCopy = classList1Copy[i].listSubClasses().toList()
                if (hashMap[classList1Copy[i].uri
                        ?: classList1Copy[i].getLabel(null)] == null) println("Null found in hashmap")
                val subClasses =
                    hashMap[classList1Copy[i].uri ?: classList1Copy[i].getLabel(null)]!!.listSubClasses().toList()
                        .filter { it ->
                            isNotMerged(it)
                        }
                val deeperMatch = findBestMatchRecursive(subClassesCopy, subClasses, candidateClass)
                if (deeperMatch != null)
                {
                    bestMatch = deeperMatch
                }
            }
        }
//    for (class1 in classList1)
//    {
//        if (areSyntacticallySimilar(class1, candidateClass) ||
//            areStructurallySimilar(class1, candidateClass) ||
//            areSemanticallySimilar(class1, candidateClass))
//        {
//            bestMatch = class1
//            val subClasses = class1.listSubClasses().toList()
//            val deeperMatch = findBestMatchRecursive(subClasses, candidateClass)
//            if (deeperMatch != null)
//            {
//                bestMatch = deeperMatch
//            }
//        }
//    }
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
    private fun addSubClassRecursive(model1: OntModel, parentClass: OntClass, subclass: OntClass)
    {
        val newSubclass = model1.createClass(subclass.uri)

        if (!parentClass.getComment(null)
                .isNullOrBlank()) newSubclass.addComment(model1.createLiteral(parentClass.getComment(null)))

        parentClass.listProperties().forEachRemaining { action ->

            newSubclass.addProperty(action.predicate, action.`object`)
        }

        newSubclass.addVersionInfo("${newSubclass.versionInfo ?: ""}\n mergedFromLLM")


        parentClass.addSubClass(newSubclass)


        // Recursively add subclasses of subclass under newSubclass
        val subSubclasses = subclass.listSubClasses().toList()
        for (subSubclass in subSubclasses)
        {
            addSubClassRecursive(model1, newSubclass, subSubclass)
        }
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
                println("Added property ${prop2.uri} from model2 to model1")
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
                println("Added domain ${domain.uri} to property ${prop1.uri}")
            }
        }

        // Copy ranges
        val ranges2 = prop2.listRange().toList()
        for (range in ranges2)
        {
            if (!prop1.hasRange(range))
            {
                prop1.addRange(range)
                println("Added range ${range.uri} to property ${prop1.uri}")
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
                    println("Added annotation ${annProp.uri} with value ${annotation} to property ${prop1.uri}")
                }
            }
        }
    }



}
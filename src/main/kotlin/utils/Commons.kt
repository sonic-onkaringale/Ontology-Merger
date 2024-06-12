package utils

import org.apache.jena.ontology.OntClass
import org.apache.jena.ontology.OntModel
import org.apache.jena.ontology.OntModelSpec
import org.apache.jena.rdf.model.ModelFactory
import java.util.*

object Commons
{
    fun <T> splitIntoBatches(list: List<T>, batchSize: Int): MutableList<List<T>>
    {
        val batches = mutableListOf<List<T>>()
        var currentBatch = mutableListOf<T>()

        for (item in list)
        {
            currentBatch.add(item)
            if (currentBatch.size == batchSize)
            {
                batches.add(currentBatch)
                currentBatch = mutableListOf()
            }
        }
        if (currentBatch.isNotEmpty())
        {
            batches.add(currentBatch)
        }

        return batches
    }

    fun getLabel(ontClass: OntClass): String?
    {
        return ontClass.getLabel(null) ?: ontClass.localName
    }

    fun getHashOfClass(clazz: OntClass, model: OntModel): String
    {
        return (getOntologyName(model) + getLabel(clazz))
    }

    fun getHashOfClass(label: String, model: OntModel): String
    {
        return (getOntologyName(model) + label)
    }

    //    While merging ensure to pass copy of class 1 otherwise hashcode could change while merging process
    fun getOntologyName(ontModel: OntModel): String
    {
        // Try to get the ontology URI (rdf:ID) if it is defined
        val ontologyResource = ontModel.getOntology(ontModel.getNsPrefixURI(""))
        if (ontologyResource != null)
        {
            return ontologyResource.uri
        }

        // Fallback: Try to get the base URI of the model

        val uri = ontModel.getNsPrefixURI("") ?: throw RuntimeException("Ont Model Uri is Null")
        return uri
    }


    fun readOntologyFromFile(path:String): OntModel?
    {
        val model =ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM)
        model.read(path)
        return model
    }

    fun getDateTimeDifference(startDate: Date, endDate: Date): String
    {
        //milliseconds

        var different: Long = endDate.getTime() - startDate.getTime()

//        println("startDate : $startDate")
//        println("endDate : $endDate")
//        println("different : $different")

        val secondsInMilli: Long = 1000
        val minutesInMilli = secondsInMilli * 60
        val hoursInMilli = minutesInMilli * 60
        val daysInMilli = hoursInMilli * 24

        val elapsedDays = different / daysInMilli
        different = different % daysInMilli

        val elapsedHours = different / hoursInMilli
        different = different % hoursInMilli

        val elapsedMinutes = different / minutesInMilli
        different = different % minutesInMilli

        val elapsedSeconds = different / secondsInMilli

//        System.out.printf(
//            "%d days, %d hours, %d minutes, %d seconds%n",
//            elapsedDays,
//            elapsedHours, elapsedMinutes, elapsedSeconds
//        )
        return "${elapsedDays.toInt()} days, ${elapsedHours.toInt()} hours, ${elapsedMinutes.toInt()} minutes, ${elapsedSeconds.toInt()} seconds"
    }
}
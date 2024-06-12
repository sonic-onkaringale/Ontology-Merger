package utils

import OntologyDetails
import org.apache.jena.ontology.OntModel
import java.io.File
import java.text.DateFormat

import java.time.Instant
import java.util.*


object Log
{
    val logList = ArrayList<String>()


    fun reset()
    {
        logList.clear()
    }

    fun save(model1: OntModel, model2: OntModel)
    {
        val loc = Locale.ENGLISH
        val time = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG, loc)
            .format(Date(Instant.now().toEpochMilli()))
        val file = File(
            OntologyDetails.cacheFolder + "${time}_${Commons.getOntologyName(model1)}_${
                Commons.getOntologyName(model2)
            }_Log.json"
        )
        file.bufferedWriter().use { out ->
            logList.forEach { line ->
                out.write(line)
                out.newLine()
            }
        }
    }
}



fun log(msg: String?)
{
    val loc = Locale.ENGLISH
    val time = DateFormat.getTimeInstance(DateFormat.LONG, loc).format(Date(Instant.now().toEpochMilli()))
    if (msg == null)
    {
        println("null")
        Log.logList.add("$time : null")
        return
    }
    println("$time : $msg")
    Log.logList.add(msg)
}

fun log()
{
    println()
}
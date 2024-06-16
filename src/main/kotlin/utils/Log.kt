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
        val time = Commons.toSafeFileName(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG, loc)
            .format(Date(Instant.now().toEpochMilli())))
        val path = OntologyDetails.logFolder + "${time}_${Commons.getOntologyName(model1)}_${
            Commons.getOntologyName(model2)
        }_" +
                (if (OntologyDetails.mergingApproach==OntologyDetails.MergingApproach.TOP_BOTTOM)
                    "Top_to_Bottom_Approach"
                else if (OntologyDetails.mergingApproach==OntologyDetails.MergingApproach.BOTTOM_UP)
                    "Bottom_to_Up_Approach"
                else
                    "Unknown_Approach") + "_Log.txt"


        val file = File(
            path
        )
        file.bufferedWriter().use { out ->
            logList.forEach { line ->
                out.write(line)
                out.newLine()
            }
        }
        println("Logs saved at $path")
    }
}



fun log(msg: String?)
{
    val loc = Locale.ENGLISH
    val time = Commons.toSafeFileName(DateFormat.getTimeInstance(DateFormat.LONG, loc).format(Date(Instant.now().toEpochMilli())))
    if (msg == null)
    {
        println("null")
        Log.logList.add("$time : null")
        return
    }
    println("$time : $msg")
    Log.logList.add("$time : $msg")
}

fun logSilent(msg: String?)
{
    val loc = Locale.ENGLISH
    val time = Commons.toSafeFileName(DateFormat.getTimeInstance(DateFormat.LONG, loc).format(Date(Instant.now().toEpochMilli())))
    if (msg == null)
    {
        Log.logList.add("$time : null")
        return
    }
    Log.logList.add("$time : $msg")
}

fun logNoTime(msg: String?)
{
    if (msg == null)
    {
        println("null")
        Log.logList.add("null")
        return
    }
    println("$msg")
    Log.logList.add("$msg")
}

fun logerr(msg: String?)
{
    val loc = Locale.ENGLISH
    val time = Commons.toSafeFileName(DateFormat.getTimeInstance(DateFormat.LONG, loc).format(Date(Instant.now().toEpochMilli())))
    if (msg == null)
    {
        System.err.println("null")
        Log.logList.add("Error $time : null")
        return
    }
    System.err.println("Error $time : $msg")
    Log.logList.add("Error $time : $msg")
}

fun log()
{
    println()
}
package org.onkaringale

import Cache.ChatCache
import OntologyDetails
import graphs.OntGraphUtils.graphReports
import kotlinx.coroutines.*
import merge.MergeOntologiesBestEntry
import natureInspired.LoadBalanceTest
import natureInspired.LoadBalancing
import utils.*
import java.io.File
import java.time.Instant
import java.util.*
import kotlin.system.exitProcess


@OptIn(DelicateCoroutinesApi::class)
fun main()
{
    /* println()
     logNoTime("Welcome to Ontology Merger")
     println()


     OntologyDetails.init()
     askToChooseOwlFiles()
     askApproach()
     val startTime = Date(Instant.now().toEpochMilli())

     val mergeOntologiesInstance = MergeOntologiesBestEntry(OntologyDetails.modelPath1,OntologyDetails.modelPath2)
     OntologyDetails.setModels(mergeOntologiesInstance.getModel1(),mergeOntologiesInstance.getModel2())
     logNoTime("Model 1 : ${OntologyDetails.ontology1}")
     logNoTime("Model 2 : ${OntologyDetails.ontology2}")
     ChatCache.init()
     graphReports(
         mergeOntologiesInstance.getModel1(), mergeOntologiesInstance.getModel2(),
         mergeOntologiesInstance.getModel1Graph(), mergeOntologiesInstance.getModel2Graph()
     )

     log("Started merging.")
     mergeOntologiesInstance.mergeOntologies()
     log("Merging Complete.")


     ChatCache.saveCache()
     val endTime = Date(Instant.now().toEpochMilli())
     log("Total Compute Time :  " + Commons.getDateTimeDifference(startTime, endTime))
     Log.save(mergeOntologiesInstance.getModel1(), mergeOntologiesInstance.getModel2())
     println()
     println()*/

    LoadBalanceTest.simulate()
}

fun askToChooseOwlFiles()
{
    logNoTime("Merge Format : Owl File 1 <-- Owl File 2 ")
    logNoTime("Choose Owl File 1 ")
    val file1 = chooseFile()
    if (file1 == null)
    {
        logerr("Exiting program due to selection of invalid owl file")
        exitProcess(-1)
    }
    logNoTime("Choose Owl File 2 ")
    val file2 = chooseFile()
    if (file2 == null)
    {
        logerr("Exiting program due to selection of invalid owl file")
        exitProcess(-1)
    }

    OntologyDetails.modelPath1 = file1.path
    OntologyDetails.modelPath2 = file2.path
}

fun askApproach()
{
    logNoTime("Select Merging Approach : ")
    logNoTime("1.Top to Bottom Approach")
    logNoTime("2.Bottom to Top Approach")
    val scanner = Scanner(System.`in`)
    logNoTime("Enter the number of the Approach you want to choose: ")
    val chosenIndex = scanner.nextInt()
    if (chosenIndex == 1)
    {
        OntologyDetails.mergingApproach = OntologyDetails.MergingApproach.TOP_BOTTOM
    }
    else if (chosenIndex == 2)
    {
        OntologyDetails.mergingApproach = OntologyDetails.MergingApproach.BOTTOM_UP
    }
    else
    {
        logerr("Exiting program due to selection of invalid merging approach")
        exitProcess(-1)
    }
}

fun chooseFile(): File?
{
    // Specify the folder path
    val folderPath = OntologyDetails.owlFilesFolder

    // Create a File object for the folder
    val folder = File(folderPath)

    // Check if the folder exists and is a directory
    if (folder.exists() && folder.isDirectory)
    {
        // List the files in the folder
        val files = folder.listFiles()

        // Check if there are any files in the folder
        if (files != null && files.isNotEmpty())
        {
            // Display the files to the user
            logNoTime("Files in the folder:")
            for ((index, file) in files.withIndex())
            {
                logNoTime(" ${index + 1}. ${file.name}")
            }

            // Ask the user to choose a file
            val scanner = Scanner(System.`in`)
            logNoTime("Enter the number of the file you want to choose: ")
            val chosenIndex = scanner.nextInt() - 1

            // Validate the user's choice
            if (chosenIndex in files.indices)
            {
                // Get the chosen file
                val chosenFile = files[chosenIndex]

                // Print the path of the chosen file
//                println("You chose: ${chosenFile.path}")
                return chosenFile
            }
            else
            {
                logerr("Invalid choice")
            }
        }
        else
        {
            logerr("The folder $folderPath is empty or does not contain any files")
        }
    }
    else
    {
        logerr("The specified path $folderPath is not a directory or does not exist")
    }
    return null
}







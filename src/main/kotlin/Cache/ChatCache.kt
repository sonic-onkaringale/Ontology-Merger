package Cache

import OntologyDetails
import kotlinx.serialization.ExperimentalSerializationApi

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import utils.Commons
import java.io.File
import utils.log
import utils.logerr
import kotlin.collections.HashMap
import kotlin.system.exitProcess

typealias ChatCacheType = HashMap<String, Boolean>

@OptIn(ExperimentalSerializationApi::class)
object ChatCache
{

    var cache = HashMap<String, Boolean>()
        private set



    private var isInitialized = false
    private var path = ""
    fun init(isLoadCacheFromFile: Boolean = true)
    {
        if (!OntologyDetails.isModelSet)
        {
            logerr("Call Cache Init before setting Models in Ontology Details: Call OntologyDetails.setModels(model1:OntModel,model2:OntModel)")
            exitProcess(-1)
        }

        path =
            OntologyDetails.cacheFolder + Commons.toSafeFileName(
                        "${OntologyDetails.ontology1}_${OntologyDetails.ontology2}_"
                    ).lowercase() + "ChatCache.json"
        if (isLoadCacheFromFile)
        {
            try
            {
                cache = loadDumped()
                log("Cache Found")


            }
            catch (e: Exception)
            {
                logerr("Cache Not Found $path")
                println("Do you want to continue ? ")
                println("1.Yes")
                println("2.No")
                var choice = readln()
                choice = choice.trim()
                when(choice)
                {
                    "1"->{
                        println("Continuing without Cache")
                    }
                    "2"->{
                        logerr("Discontinuation triggered")
                        exitProcess(-1)
                    }
                    else->{
                        logerr("Invalid Choice")
                        logerr("Discontinuation triggered")
                        exitProcess(-1)
                    }
                }
            }
        }
        isInitialized = true

    }


    private fun loadDumped(): ChatCacheType
    {
        return File(path).inputStream().use { inputStream ->
            Json.decodeFromStream(inputStream)
        }
    }


    fun saveCache()
    {
        if (!isInitialized)
        {
            log("Cache Didn't Initialized")
            return
        }

        try
        {
            File(path).outputStream().use { outputStream ->
                Json.encodeToStream(cache, outputStream)
            }
            log("Cache Saved at $path")
        }
        catch (e: Exception)
        {
            log("Cache Save Error  : ${e.message}")
        }
    }
}


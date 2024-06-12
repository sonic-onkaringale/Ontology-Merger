package Cache

import OntologyDetails
import kotlinx.serialization.ExperimentalSerializationApi

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import org.apache.jena.ontology.OntModel
import utils.Commons
import java.io.File
import utils.log

typealias ChatCacheType = HashMap<String, Boolean>

@OptIn(ExperimentalSerializationApi::class)
object ChatCache
{

    var cache = HashMap<String, Boolean>()
        private set

    private var isInitialized = false
    private var path = ""
    fun init(model1: OntModel, model2: OntModel, isLoadCacheFromFile: Boolean = true)
    {
        path =
            OntologyDetails.cacheFolder + "${Commons.getOntologyName(model1)}_${Commons.getOntologyName(model2)}_ChatCache.json"
        if (isLoadCacheFromFile)
        {
            try
            {
                cache = loadDumped()
            }
            catch (e: Exception)
            {
                log("Cache Not Found")
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
        }
        catch (e: Exception)
        {
            log("Cache Save Error  : ${e.message}")
        }
    }
}


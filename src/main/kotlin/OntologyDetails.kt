import org.apache.jena.ontology.OntModel
import utils.OsUtil
import utils.capitalizeWords
import utils.getName

object OntologyDetails
{
    var ontology1 = "Human"
    var ontology2 = "Mouse"

    //  1.subclass of   2.similar to
    var strategy = "subclass of"

    const val tokenLimit = 4000

    enum class MergingApproach
    {
        BOTTOM_UP, TOP_BOTTOM
    }

    var mergingApproach = MergingApproach.TOP_BOTTOM


    var isModelSet = false
        private set

//    Windows
//     var modelPath1 = "C:\\Users\\ingal\\Downloads\\owl\\human.owl"
//     var modelPath2 = "C:\\Users\\ingal\\Downloads\\owl\\mouse.owl"
//     var cacheFolder = "D:\\llm\\cache\\"
//     var saveFolder = cacheFolder
//     var logFolder = cacheFolder


//    Linux

    var modelPath1 = "/home/paperspace/owl/human.owl"
    var modelPath2 = "/home/paperspace/owl/mouse.owl"
    var cacheFolder = "/home/paperspace/llmm/cache/"
    var saveFolder = "/home/paperspace/llmm/saves/"
    var logFolder = "/home/paperspace/llmm/logs/"
    var owlFilesFolder = "/home/paperspace/llmm/owl"

    fun init()
    {
        when (OsUtil.getOs())
        {
            OsUtil.OS.WINDOWS ->
            {
                modelPath1 = "C:\\Users\\ingal\\Downloads\\owl\\human.owl"
                modelPath2 = "C:\\Users\\ingal\\Downloads\\owl\\mouse.owl"
                cacheFolder = "D:\\llm\\local\\cache\\"
                saveFolder = "D:\\llm\\local\\saves\\"
                logFolder = "D:\\llm\\local\\logs\\"
                owlFilesFolder = "D:\\llm\\local\\owl"
            }

            OsUtil.OS.LINUX ->
            {
                modelPath1 = "/home/paperspace/owl/human.owl"
                modelPath2 = "/home/paperspace/owl/mouse.owl"
                cacheFolder = "/home/paperspace/llmm/cache/"
                saveFolder = "/home/paperspace/llmm/saves/"
                logFolder = "/home/paperspace/llmm/logs/"
                owlFilesFolder = "/home/paperspace/llmm/owl"
            }

            OsUtil.OS.MAC ->
            {

            }

            OsUtil.OS.SOLARIS ->
            {

            }
        }

    }

    fun setModels(model1:OntModel,model2:OntModel)
    {
        ontology1 = model1.getName().capitalizeWords()
        ontology2 = model2.getName().capitalizeWords()
        isModelSet=true
    }

}

object LLMDetails
{
    //    const val MODEL_NAME = "aaditya/OpenBioLLM-Llama3-8B-GGUF"
    const val MODEL_NAME = "lmstudio-community/Meta-Llama-3-8B-Instruct-GGUF"

}


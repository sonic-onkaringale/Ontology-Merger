object OntologyDetails
{
    var ontology1="Human"
    var ontology2="Mouse Ontology"

//  1.subclass of   2.similar to
    var strategy = "subclass of"

    const val tokenLimit = 4000

    enum class MergingApproach
    {
        BOTTOM_UP,TOP_BOTTOM
    }

    var mergingApproach = MergingApproach.BOTTOM_UP

    const val cacheFolder = "D:\\llm\\cache"

}
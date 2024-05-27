package org.onkaringale.matching

import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.RDFS


object SyntacticSimilarity
{
    fun areSyntacticallySimilar(res1: Resource, res2: Resource): Boolean
    {
        if (res1.uri == res2.uri)
        {
            return true
        }

        val label1 = getLabel(res1)
        val label2 = getLabel(res2)

        return label1 != null && label1.equals(label2, ignoreCase = true)
    }

    private fun getLabel(res: Resource): String?
    {
        val iter = res.listProperties(RDFS.label)
        if (iter.hasNext())
        {
            return iter.nextStatement().literal.string
        }
        return null
    }
}

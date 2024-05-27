package org.onkaringale.matching

import org.apache.jena.ontology.OntClass


object StructuralSimilarity
{
    fun areStructurallySimilar(class1: OntClass, class2: OntClass): Boolean
    {
        if (class1.listSuperClasses().toSet() != class2.listSuperClasses().toSet())
        {
            return false
        }

        if (class1.listSubClasses().toSet() != class2.listSubClasses().toSet())
        {
            return false
        }

        return true
    }
}

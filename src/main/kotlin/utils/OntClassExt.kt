package utils

import org.apache.jena.ontology.OntClass
import org.apache.jena.ontology.OntModel

fun OntClass.getHash(model: OntModel): String
{
    return Commons.getHashOfClass(this,model)
}

fun OntClass.getLabelElite(): String?
{
    return this.getLabel(null) ?: this.localName
}

fun OntModel.getName(): String
{
    return Commons.getOntologyName(this)
}
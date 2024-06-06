package utils

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import tech.amikos.chromadb.Collection
import tech.amikos.chromadb.Collection.QueryResponse
import tech.amikos.chromadb.EmbeddingFunction
import tech.amikos.chromadb.handler.ApiClient
import tech.amikos.chromadb.handler.DefaultApi
import tech.amikos.chromadb.model.QueryEmbedding
import tech.amikos.chromadb.model.QueryEmbedding.IncludeEnum
import java.util.concurrent.TimeUnit



class WhereBuilder private constructor()
{
    private val filter = JsonObject()

    fun eq(field: String, value: Any): WhereBuilder
    {
        return operation("\$eq", field, value)
    }

    fun gt(field: String, value: Any): WhereBuilder
    {
        return operation("\$gt", field, value)
    }

    fun gte(field: String, value: Any): WhereBuilder
    {
        return operation("\$gte", field, value)
    }

    fun lt(field: String, value: Any): WhereBuilder
    {
        return operation("\$lt", field, value)
    }

    fun lte(field: String, value: Any): WhereBuilder
    {
        return operation("\$lte", field, value)
    }

    fun ne(field: String, value: Any): WhereBuilder
    {
        return operation("\$ne", field, value)
    }

    fun `in`(field: String, value: List<Any?>): WhereBuilder
    {
        return operation("\$in", field, value)
    }

    fun nin(field: String, value: List<Any?>): WhereBuilder
    {
        return operation("\$nin", field, value)
    }

    fun and(vararg builders: WhereBuilder): WhereBuilder
    {
        val jsonArray = JsonArray()
        for (builder in builders)
        {
            jsonArray.add(builder.filter)
        }
        filter.add("\$and", jsonArray)
        return this
    }

    fun or(vararg builders: WhereBuilder): WhereBuilder
    {
        val jsonArray = JsonArray()
        for (builder in builders)
        {
            jsonArray.add(builder.filter)
        }
        filter.add("\$or", jsonArray)
        return this
    }

    private fun operation(operation: String, field: String, value: Any): WhereBuilder
    {
        val innerFilter = JsonObject()
        if (value is List<*>)
        {
            val jsonArray = JsonArray()
            for (o in value)
            {
                if (o is String) jsonArray.add(o.toString())
                else if (o is Int) jsonArray.add(o)
                else if (o is Float) jsonArray.add(o)
                else if (o is Boolean) jsonArray.add(o)
                else
                {
                    throw IllegalArgumentException("Unsupported type: " + if (o == null) "null" else o::class.java)
                }
            }
            innerFilter.add(operation, jsonArray)
        }
        else
        {
            innerFilter.addProperty(operation, value.toString())
        }
        filter.add(field, innerFilter) // Gson handles various value types
        return this
    }

    fun build(): JsonObject
    {
        return filter
    }

    companion object
    {
        fun create(): WhereBuilder
        {
            return WhereBuilder()
        }
    }
}

class WhereDocumentBuilder private constructor()
{
    private val filter = JsonObject()

    fun contains(value: String): WhereDocumentBuilder
    {
        return operation("\$contains", value)
    }

    fun notContains(value: String): WhereDocumentBuilder
    {
        return operation("\$not_contains", value)
    }

    fun and(vararg builders: WhereDocumentBuilder): WhereDocumentBuilder
    {
        val jsonArray = JsonArray()
        for (builder in builders)
        {
            jsonArray.add(builder.filter)
        }
        filter.add("\$and", jsonArray)
        return this
    }

    fun or(vararg builders: WhereDocumentBuilder): WhereDocumentBuilder
    {
        val jsonArray = JsonArray()
        for (builder in builders)
        {
            jsonArray.add(builder.filter)
        }
        filter.add("\$or", jsonArray)
        return this
    }

    private fun operation(operation: String, value: Any): WhereDocumentBuilder
    {
        filter.addProperty(operation, value.toString()) // Gson handles various value types
        return this
    }

    fun build(): JsonObject
    {
        return filter
    }

    companion object
    {
        fun create(): WhereDocumentBuilder
        {
            return WhereDocumentBuilder()
        }
    }
}


fun Collection.queryElite(
    emF: EmbeddingFunction,
    queryTexts: List<String?>?,
    nResults: Int?,
    where: JsonObject?,
    whereDocument: JsonObject?,
    include: List<IncludeEnum?>?
): QueryResponse?
{
    val apiClient = ApiClient()
    var api: DefaultApi? = null
    val body = QueryEmbedding()
    apiClient.setBasePath("http://localhost:8000")
    api = DefaultApi(apiClient)
    apiClient.setHttpClient(
        apiClient.httpClient.newBuilder().readTimeout(60, TimeUnit.SECONDS).writeTimeout(60, TimeUnit.SECONDS).build()
    )
    api.apiClient.setUserAgent("Chroma-JavaClient/0.1.x")

    body.queryEmbeddings(emF.createEmbedding(queryTexts) as List<Any?>)
    body.nResults(nResults)
    body.include(include)
    if (where != null)
    {
        body.where(
            where.asMap() as Map<String, Any>?
        )
    }
    if (whereDocument != null)
    {

        body.whereDocument(whereDocument.asMap() as Map<String, Any>?)
    }
    val gson = Gson()
    val json = gson.toJson(api.getNearestNeighbors(body, this.id))
    return Gson().fromJson(json, QueryResponse::class.java)

}


package natureInspired

import extensions.negate
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.apache.jena.ontology.OntClass
import org.onkaringale.api.Apis
import org.onkaringale.matching.SemanticSimilarity
import utils.log
import utils.logerr
import java.time.Instant
import java.util.*
import kotlin.collections.HashMap
import kotlin.random.Random


object LoadBalancing
{

    data class Node(val id: Int, val llmApi: Apis.LlmApi, var load: Int = 0)
    data class Task(val id: Int, val class1: OntClass, val class2: OntClass)

    val multipleApis = Apis.getMultipleLLMApis()
    val timeTookArray = MutableList(multipleApis.size) { 0L }
    val pheromones = Array(multipleApis.size) { 1.0 }

    class AntColonyOptimization(
        val nodes: List<Node>,
        val tasks: List<Task>,
        val evaporationRate: Double,
        val pheromoneIncrease: Double
    )
    {
        val pheromoneDecreaseFactor: Double = 0.1
        private val unassignedTasks = tasks.toMutableList()


        @OptIn(DelicateCoroutinesApi::class)
        suspend fun runOptimization(): List<Boolean>
        {
            val results = MutableList<Boolean>(unassignedTasks.size) { false }
            val allDeferredResults = HashMap<Pair<Task, Node>, Deferred<Pair<Boolean?, Long>>>()

            for (task in unassignedTasks)
            {
                val assignment = assignTask(task)
                val resultDeferred = GlobalScope.async {
                    val (result, timeTook) = executeTask(assignment)
                    results[assignment.first.id] = result ?: false
                    Pair(result, timeTook)
                }
                allDeferredResults[assignment] = resultDeferred
            }
            for ((key, value) in allDeferredResults.toSortedMap { o1, o2 ->
                if (o1.first.id > o2.first.id)
                    o1.first.id
                else
                    o2.first.id
            })
            {
                value.await()
                updatePheromones(key, value.await().first, value.await().second)
                evaporatePheromones()
            }

            return results
        }

        private fun assignTask(task: Task): Pair<Task, Node>
        {
            val probabilities = calculateProbabilities()
            val selectedNode = selectNode(probabilities)
            nodes[selectedNode].load += 1
            return task to nodes[selectedNode]
        }

        private fun calculateProbabilities(): List<Double>
        {
            val maxLoad = nodes.maxOf { it.load }.toDouble()
            val loadFactor = nodes.map { 1 - (it.load / (maxLoad + 1)) } // +1 to avoid division by zero
            val combined = pheromones.zip(loadFactor) { pheromone, ldFactor ->
                pheromone * ldFactor
            }
            val sumCombined = combined.sum()
            return if (sumCombined == 0.0)
            {
                List(combined.size) { 1.0 / combined.size }
            }
            else
            {
//                combined.map { it / sumCombined }
                combined.map { it }
            }
        }


        private suspend fun executeTask(assignment: Pair<Task, Node>): Pair<Boolean?, Long>
        {
            val (task, node) = assignment
            val startTime = Instant.now().toEpochMilli()
            val result = makeHttpCall(node, task)
            val endTime = Instant.now().toEpochMilli()
            val timeTook = endTime - startTime
            timeTookArray[node.id] += timeTook
            return Pair(result, timeTook)
        }

        private suspend fun makeHttpCall(node: Node, task: Task): Boolean
        {
            try
            {
                return SemanticSimilarity.areSemanticallySimilar(task.class1, task.class2, node.llmApi, node.id + 1)
            }
            catch (e: Exception)
            {
                logerr("Error making request to Node ${node.id}: ${e.message}")
            }
            return false
        }


        private fun updatePheromones(assignment: Pair<Task, Node>, success: Boolean?, timeTook: Long)
        {
            val (task, node) = assignment
            if (success != null)
            {

                pheromones[node.id] = (pheromones[node.id] + pheromoneIncrease) + timeValueFunction(timeTook)
            }
            else
            {
                pheromones[node.id] =
                    negate((pheromones[node.id] * pheromoneDecreaseFactor) + timeValueFunction(timeTook))
                if (!compromisedNodes.containsKey(node.id))
                {
                    compromisedNodes[node.id]=true
                    stackSize--
                }

            }
        }


        fun timeValueFunction(value: Long): Double
        {
            return (1.0 / (value + 1)) * 100
        }

        private fun evaporatePheromones()
        {
            for (i in pheromones.indices)
            {


                pheromones[i] *= (1 - evaporationRate)
//                pheromones[i] = maxOf(pheromones[i], minPheromoneLevel)
            }
        }

        var lastSelectedIndexes = Stack<Int>()
        var stackSize = nodes.size
        var compromisedNodes =HashMap<Int,Boolean>()

        private fun selectNode(probabilities: List<Double>): Int
        {
            val totalProbability = probabilities.max()
            val randomValue = Random.nextDouble(totalProbability)
            var cumulativeProbability = 0.0
//            Index and Probability
            var mapProbability = HashMap<Int, Double>()

            for ((index, probability) in probabilities.withIndex())
            {
                mapProbability[index] = probability
            }
            val sortedMapProbability = mapProbability.entries.sortedByDescending {
                it.value
            }

            for ((index, probability) in sortedMapProbability)
            {

                cumulativeProbability += probability
                if (lastSelectedIndexes.size == stackSize)
                    lastSelectedIndexes.clear()

                if (randomValue <= cumulativeProbability && !lastSelectedIndexes.contains(index))
                {
                    lastSelectedIndexes.add(index)
                    return index
                }
            }
            return probabilities.indices.random()

        }
    }


    suspend fun areSemanticallySimilarLoadBalanced(classes1: List<OntClass>, class2: OntClass): List<Boolean>
    {

        val allLLmApiNodes = ArrayList<Node>()
        for (i in multipleApis.indices)
        {
            allLLmApiNodes.add(Node(i, multipleApis[i]))
        }
        val tasks = ArrayList<Task>()
        for (i in classes1.indices)
        {
            tasks.add(Task(i, classes1[i], class2))
        }

        val aco = AntColonyOptimization(allLLmApiNodes, tasks, evaporationRate = 0.1, pheromoneIncrease = 0.4)
        val results = aco.runOptimization()
        allLLmApiNodes.forEach { log("Node ${it.id} had load ${it.load}") }
        return results
    }

}
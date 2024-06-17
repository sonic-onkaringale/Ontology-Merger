@file:Suppress("DuplicatedCode")

package natureInspired

import extensions.negate
import kotlinx.coroutines.*
import org.onkaringale.api.Apis
import org.onkaringale.api.Apis.constructRetrofit
import utils.log
import utils.logerr
import java.time.Instant
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.random.Random

typealias Assignment = Pair<LoadBalanceTest.Task, LoadBalanceTest.Node>

object LoadBalanceTest
{
    data class Node(val id: Int, val llmApi: Apis.LlmApi, var load: Int = 0)
    data class Task(val id: Int, val class1: String, val class2: String)

    val multipleApis = listOf(
        constructRetrofit("http://localhost:1234"),
        constructRetrofit("http://localhost:1234"),
        constructRetrofit("http://localhost:1234"),
        constructRetrofit("http://localhost:1234"),
        constructRetrofit("http://localhost:1234"),
        constructRetrofit("http://localhost:1234"),
        constructRetrofit("http://localhost:1234"),
        constructRetrofit("http://localhost:1234"),


        )
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


            val allDeferredResults = HashMap<Assignment, Deferred<Pair<Boolean?, Long>>>()

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
//            println(combined)
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
//            var timeTook = 0L
            var timeTook = endTime - startTime
            if (node.id == 4)
                timeTook = Random.nextLong(0, 100000)
            else if (node.id == 1)
                timeTook = Random.nextLong(0, 1000)
            else
                timeTook = Random.nextLong(0, 4)

            timeTookArray[node.id] += timeTook
            return Pair(result, timeTook)
        }

        private fun makeHttpCall(node: Node, task: Task): Boolean?
        {
            return try
            {

                if (node.id == 6)
                    return null
                task.class1.contains("true")
//                SemanticSimilarity.areSemanticallySimilar(task.class1, task.class2, node.llmApi, node.id + 1)
            }
            catch (e: Exception)
            {
                logerr("Error making request to Node ${node.id}: ${e.message}")
                null
            }
        }

        private fun updatePheromones(assignment: Pair<Task, Node>, success: Boolean?, timeTook: Long)
        {
            val (task, node) = assignment
            if (success != null)
            {
//                if (assignment.second.id==0)
//                    pheromones[node.id] = (pheromones[node.id] + (pheromoneIncrease/2)) + timeValueFunction(timeTook)
                pheromones[node.id] = (pheromones[node.id] + pheromoneIncrease) + timeValueFunction(timeTook)
            }
            else
            {
                pheromones[node.id] =
                    negate((pheromones[node.id] * pheromoneDecreaseFactor) + timeValueFunction(timeTook))
//                pheromones[node.id] = maxOf(pheromones[node.id] * pheromoneDecreaseFactor, minPheromoneLevel)
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
                println("$index $probability")
                cumulativeProbability += probability
                if (lastSelectedIndexes.size == nodes.size - 2)
                    lastSelectedIndexes.clear()

                if (randomValue <= cumulativeProbability && !lastSelectedIndexes.contains(index))
                {

                    println(" Index $index :")
                    pheromones.forEach { print("$it,") }
                    println()
                    lastSelectedIndexes.add(index)
                    return index
                }
            }
            return probabilities.indices.random()

        }
    }

    suspend fun areSemanticallySimilarLoadBalanced(classes1: List<String>, class2: String): List<Boolean>
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

        val aco = AntColonyOptimization(
            allLLmApiNodes,
            tasks,
            evaporationRate = 0.1,
//            pheromoneIncrease = (multipleApis.size / 2).toDouble()
            pheromoneIncrease = 0.4
        )
        val results = aco.runOptimization()
        allLLmApiNodes.forEach { log("Node ${it.id} had load ${it.load} and took ${timeTookArray[it.id]}") }
        return results
    }


    fun simulate()
    {
        val size = 60
        val booleanList = List(size) {
            java.util.Random().nextBoolean()
        }
        val classList = List(size) {
            "class expects ${booleanList[it]}"
        }
        runBlocking {
            repeat(100) {
                val result = LoadBalanceTest.areSemanticallySimilarLoadBalanced(
                    classList,
                    "class"
                )
                for (i in result.indices)
                {
                    if (!classList[i].contains(result[i].toString()))
                    {
                        println("Result didn't match")
                        break
                    }
                }
            }
            println(-(-4))
        }
    }

}
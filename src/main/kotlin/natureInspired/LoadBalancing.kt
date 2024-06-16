package natureInspired

import kotlin.random.Random

import kotlinx.coroutines.*
import org.apache.jena.ontology.OntClass
import org.onkaringale.api.Apis
import org.onkaringale.matching.SemanticSimilarity
import utils.log
import utils.logSilent
import utils.logerr


object LoadBalancing
{

    data class Node(val id: Int, val llmApi: Apis.LlmApi, var load: Int = 0)
    data class Task(val id: Int, val class1: OntClass, val class2: OntClass)

    class AntColonyOptimization(
        val nodes: List<Node>,
        val tasks: List<Task>,
        val evaporationRate: Double,
        val pheromoneIncrease: Double
    )
    {
        private val pheromones: Array<DoubleArray> = Array(tasks.size) { DoubleArray(nodes.size) { 1.0 } }
        private val unassignedTasks = tasks.toMutableList()

        suspend fun runOptimization(): List<Boolean>
        {
            val assignments = assignTasks()
            updatePheromones(assignments)
            evaporatePheromones()
            return executeTasks(assignments)
        }

        private fun assignTasks(): List<Pair<Task, Node>>
        {
            val assignments = mutableListOf<Pair<Task, Node>>()
            for (task in unassignedTasks)
            {
                val probabilities = calculateProbabilities(task.id)
                val selectedNode = selectNode(probabilities)
                nodes[selectedNode].load += 1
//                logSilent("Assigned Task ${task.id} to Node ${nodes[selectedNode].id}")
                assignments.add(task to nodes[selectedNode])
            }
            unassignedTasks.clear()
            return assignments
        }

        private fun calculateProbabilities(taskId: Int): List<Double>
        {
            val pheromoneLevels = pheromones[taskId]
            val maxLoad = nodes.maxOf { it.load }.toDouble()
            val loadFactor = nodes.map { 1 - (it.load / (maxLoad + 1)) } // +1 to avoid division by zero
            val combined = pheromoneLevels.zip(loadFactor) { pheromone, loadFactor ->
                pheromone * loadFactor
            }
            val sumCombined = combined.sum()
            return combined.map { it / sumCombined }
        }

        private suspend fun executeTasks(assignments: List<Pair<Task, Node>>): List<Boolean> = coroutineScope {
//            Hashmap of index and result
            val allDeferredResults = HashMap<Int, Deferred<Boolean>>()
            assignments.forEach { (task, node) ->
                val result = async {
                    return@async makeHttpCall(node, task)
                }
                allDeferredResults[task.id] = result
            }
            for ((key, value) in allDeferredResults)
            {
                value.await()
            }
            return@coroutineScope List<Boolean>(allDeferredResults.size) { index: Int ->
                allDeferredResults[index]?.await() ?: throw RuntimeException("Indexing in Load Balancing is messed up")
            }


        }

        private suspend fun makeHttpCall(node: Node, task: Task): Boolean
        {
            try
            {
                return SemanticSimilarity.areSemanticallySimilar(task.class1, task.class2, node.llmApi,node.id+1)
            }
            catch (e: Exception)
            {
                logerr("Error making request to Node ${node.id}: ${e.message}")
            }
            return false
        }

        private fun updatePheromones(assignments: List<Pair<Task, Node>>)
        {
            assignments.forEach { (task, node) ->
                pheromones[task.id][node.id] += pheromoneIncrease
            }
        }

        private fun evaporatePheromones()
        {
            for (i in pheromones.indices)
            {
                for (j in pheromones[i].indices)
                {
                    pheromones[i][j] *= (1 - evaporationRate)
                }
            }
        }

        private fun selectNode(probabilities: List<Double>): Int
        {
            val randomValue = Random.nextDouble()
            var cumulativeProbability = 0.0
            for ((index, probability) in probabilities.withIndex())
            {
                cumulativeProbability += probability
                if (randomValue <= cumulativeProbability)
                {
                    return index
                }
            }
            return probabilities.size - 1
        }
    }


    suspend fun areSemanticallySimilarLoadBalanced(classes1: List<OntClass>, class2: OntClass): List<Boolean>
    {

        val allLLmApiNodes = ArrayList<Node>()
        val multipleApis = Apis.getMultipleLLMApis()
        for (i in multipleApis.indices)
        {
            allLLmApiNodes.add(Node(i, multipleApis[i]))
        }
        val tasks = ArrayList<Task>()
        for (i in classes1.indices)
        {
            tasks.add(Task(i, classes1[i], class2))
        }

        val aco = AntColonyOptimization(allLLmApiNodes, tasks, evaporationRate = 0.1, pheromoneIncrease = 1.0)
        val results = aco.runOptimization()
        allLLmApiNodes.forEach { log("Node ${it.id} had load ${it.load}") }
        return results
    }

}
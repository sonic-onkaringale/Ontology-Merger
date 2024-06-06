package natureInspired

import kotlin.random.Random

import kotlinx.coroutines.*


object LoadBalancing
{

    data class Node(val id: Int, val url: String, var load: Int = 0)
    data class Task(val id: Int, val payload: String)

    class AntColonyOptimization(
        val nodes: List<Node>,
        val tasks: List<Task>,
        val evaporationRate: Double,
        val pheromoneIncrease: Double
    ) {
        private val pheromones: Array<DoubleArray> = Array(tasks.size) { DoubleArray(nodes.size) { 1.0 } }
        private val unassignedTasks = tasks.toMutableList()

        suspend fun runOptimization() {
            val assignments = assignTasks()
            updatePheromones(assignments)
            evaporatePheromones()
            executeTasks(assignments)
        }

        private fun assignTasks(): List<Pair<Task, Node>> {
            val assignments = mutableListOf<Pair<Task, Node>>()
            for (task in unassignedTasks) {
                val probabilities = calculateProbabilities(task.id)
                val selectedNode = selectNode(probabilities)
                nodes[selectedNode].load += 1
                println("Assigned Task ${task.id} to Node ${nodes[selectedNode].url}")
                assignments.add(task to nodes[selectedNode])
            }
            unassignedTasks.clear()
            return assignments
        }

        private fun calculateProbabilities(taskId: Int): List<Double> {
            val pheromoneLevels = pheromones[taskId]
            val maxLoad = nodes.maxOf { it.load }.toDouble()
            val loadFactor = nodes.map { 1 - (it.load / (maxLoad + 1)) } // +1 to avoid division by zero
            val combined = pheromoneLevels.zip(loadFactor) { pheromone, loadFactor ->
                pheromone * loadFactor
            }
            val sumCombined = combined.sum()
            return combined.map { it / sumCombined }
        }

        private suspend fun executeTasks(assignments: List<Pair<Task, Node>>) = coroutineScope {
            assignments.forEach { (task, node) ->
                launch {
                    makeHttpCall(node, task)
                }
            }
        }

        private suspend fun makeHttpCall(node: Node, task: Task) {
            try {
                // Simulate a synchronous request
                println("Request Made to ${node.url} Node ${node.id}")
//                delay(Random.nextLong(100, 300))
//                println("Response from ${node.url} Node ${node.id}: Response")
            } catch (e: Exception) {
                println("Error making request to Node ${node.id}: ${e.message}")
            } finally {
//                node.load -= 1
            }
        }

        private fun updatePheromones(assignments: List<Pair<Task, Node>>) {
            assignments.forEach { (task, node) ->
                pheromones[task.id][node.id] += pheromoneIncrease
            }
        }

        private fun evaporatePheromones() {
            for (i in pheromones.indices) {
                for (j in pheromones[i].indices) {
                    pheromones[i][j] *= (1 - evaporationRate)
                }
            }
        }

        private fun selectNode(probabilities: List<Double>): Int {
            val randomValue = Random.nextDouble()
            var cumulativeProbability = 0.0
            for ((index, probability) in probabilities.withIndex()) {
                cumulativeProbability += probability
                if (randomValue <= cumulativeProbability) {
                    return index
                }
            }
            return probabilities.size - 1
        }
    }

    suspend fun simulate() {
        val nodes = listOf(
            Node(0, "http://server1.example.com"),
            Node(1, "http://server2.example.com"),
            Node(2, "http://server3.example.com"),
            Node(3, "http://server4.example.com"),
            Node(4, "http://server5.example.com")
        )
        val tasks = List(1000) { Task(it, "payload for task $it") }
        val aco = AntColonyOptimization(nodes, tasks, evaporationRate = 0.1, pheromoneIncrease = 1.0)

        aco.runOptimization()

        println("Final loads on nodes:")
        nodes.forEach { println("Node ${it.id} has load ${it.load}") }
    }

}
package org.onkaringale.graphs

class AdjacencyList<T> {

    private val adjacencyMap = mutableMapOf<Vertex<T>, ArrayList<Edge<T>>>()

    fun createVertex(data: T): Vertex<T> {
        val vertex = Vertex(adjacencyMap.count(), data)
        adjacencyMap[vertex] = arrayListOf()
        return vertex
    }

    fun addDirectedEdge(source: Vertex<T>, destination: Vertex<T>, weight: Double? = 0.0) {
        val edge = Edge(source, destination, weight)
        adjacencyMap[source]?.add(edge)
    }

    override fun toString(): String {
        return buildString {
            adjacencyMap.forEach { (vertex, edges) ->
                val edgeString = edges.joinToString { it.destination.data.toString() }
                append("${vertex.data} -> [$edgeString]\n")
            }
        }
    }


    fun findEntryPoints(): List<Vertex<T>> {
        val inDegree = mutableMapOf<Vertex<T>, Int>().withDefault { 0 }

        // Calculate in-degrees of all vertices
        adjacencyMap.forEach { (vertex, edges) ->
            edges.forEach { edge ->
                inDegree[edge.destination] = inDegree.getValue(edge.destination) + 1
            }
        }

        // Entry points have in-degree of 0
        return adjacencyMap.keys.filter { inDegree.getValue(it) == 0 }
    }

    fun findDisconnectedSubgraphs(): List<List<Vertex<T>>> {
        val visited = mutableSetOf<Vertex<T>>()
        val subgraphs = mutableListOf<List<Vertex<T>>>()

        // Perform DFS from each vertex
        adjacencyMap.keys.forEach { vertex ->
            if (!visited.contains(vertex)) {
                val subgraph = mutableListOf<Vertex<T>>()
                dfs(vertex, visited, subgraph)
                subgraphs.add(subgraph)
            }
        }
        return subgraphs
    }

    private fun dfs(vertex: Vertex<T>, visited: MutableSet<Vertex<T>>, subgraph: MutableList<Vertex<T>>) {
        visited.add(vertex)
        subgraph.add(vertex)
        adjacencyMap[vertex]?.forEach { edge ->
            if (!visited.contains(edge.destination)) {
                dfs(edge.destination, visited, subgraph)
            }
        }
    }

    fun findBestEntryPoint(): Vertex<T>? {
        val entryPoints = findEntryPoints()
        var maxReach = 0
        var bestEntryPoint: Vertex<T>? = null

        entryPoints.forEach { entryPoint ->
            val reach = calculateReach(entryPoint)
            if (reach > maxReach) {
                maxReach = reach
                bestEntryPoint = entryPoint
            }
        }

        return bestEntryPoint
    }
    fun findTopNBestEntryPoints(n: Int): List<Vertex<T>> {
        val entryPoints = findEntryPoints()
        val entryPointsWithReach = entryPoints.map { it to calculateReach(it) }

        // Sort by reach in descending order and take the top n
        return entryPointsWithReach.sortedByDescending { it.second }
            .take(n)
            .map { it.first }
    }

    private fun calculateReach(vertex: Vertex<T>): Int {
        val visited = mutableSetOf<Vertex<T>>()
        val stack = mutableListOf<Vertex<T>>()
        stack.add(vertex)

        while (stack.isNotEmpty()) {
            val current = stack.removeAt(stack.size - 1)
            if (!visited.contains(current)) {
                visited.add(current)
                adjacencyMap[current]?.forEach { edge ->
                    stack.add(edge.destination)
                }
            }
        }

        return visited.size
    }
}
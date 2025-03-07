class AuditorioMap {
    companion object {
        // Constantes para los tipos de celdas
        const val PARED = 0        // Paredes y obstáculos
        const val CAMINO = 1       // Espacio transitable
        const val ENTRADA = 2      // Entradas/salidas
        const val ASIENTOS = 3     // Área de asientos
        const val ESCALERAS = 4    // Escaleras

        // Asegurarnos de que las dimensiones coincidan con MapMatrixProvider
        val MAP_WIDTH = 40  // Aumentamos el tamaño para coincidir con otros mapas
        val MAP_HEIGHT = 40 // Aumentamos el tamaño para coincidir con otros mapas

        // Modificar la matriz del auditorio para que tenga las dimensiones correctas
        val mapaAuditorio = Array(MAP_HEIGHT) { y ->
            Array(MAP_WIDTH) { x ->
                // Si estamos dentro de los límites del mapa original (20x15)
                if (y < 15 && x < 20) {
                    when (y) {
                        // Aquí copiamos el diseño original del auditorio
                        0 -> arrayOf(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0)[x]
                        1 -> arrayOf(0,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0)[x]
                        2 -> arrayOf(0,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0)[x]
                        3 -> arrayOf(0,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0)[x]
                        4 -> arrayOf(0,1,1,1,0,4,1,1,1,1,1,1,1,1,1,1,4,1,1,0)[x]
                        5 -> arrayOf(0,2,2,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,2,0)[x]
                        6 -> arrayOf(0,2,2,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,2,0)[x]
                        7 -> arrayOf(0,1,1,1,3,3,3,3,3,3,3,3,3,3,3,3,1,1,1,0)[x]
                        8 -> arrayOf(0,1,1,1,3,3,3,3,3,3,3,3,3,3,3,3,1,1,1,0)[x]
                        9 -> arrayOf(0,1,1,1,3,3,3,3,3,3,3,3,3,3,3,3,1,1,1,0)[x]
                        10 -> arrayOf(0,1,1,1,3,3,3,3,3,3,3,3,3,3,3,3,1,1,1,0)[x]
                        11 -> arrayOf(0,1,1,1,3,3,3,3,3,3,3,3,3,3,3,3,1,1,1,0)[x]
                        12 -> arrayOf(0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0)[x]
                        13 -> arrayOf(0,2,2,1,1,1,1,1,1,1,1,1,1,1,1,1,1,2,2,0)[x]
                        14 -> arrayOf(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0)[x]
                        else -> CAMINO // Espacios adicionales como camino
                    }
                } else {
                    CAMINO // El resto del espacio como camino
                }
            }
        }

        // Verifica si una posición es válida y transitable
        fun isValidPosition(x: Int, y: Int): Boolean {
            return x >= 0 && x < MAP_WIDTH && 
                   y >= 0 && y < MAP_HEIGHT && 
                   mapaAuditorio[y][x] != PARED
        }

        // Obtiene el valor en una posición específica
        fun getMapValue(x: Int, y: Int): Int {
            return if (x >= 0 && x < MAP_WIDTH && y >= 0 && y < MAP_HEIGHT) {
                mapaAuditorio[y][x]
            } else {
                PARED // Valor por defecto para posiciones fuera del mapa
            }
        }

        // Verifica si una posición es una entrada/salida
        fun isExit(x: Int, y: Int): Boolean {
            return isValidPosition(x, y) && mapaAuditorio[y][x] == ENTRADA
        }

        // Verifica si una posición es una escalera
        fun isStairs(x: Int, y: Int): Boolean {
            return isValidPosition(x, y) && mapaAuditorio[y][x] == ESCALERAS
        }

        // Verifica si una posición es un asiento
        fun isAsiento(x: Int, y: Int): Boolean {
            return isValidPosition(x, y) && mapaAuditorio[y][x] == ASIENTOS
        }

        // Verifica si una posición es un camino transitable
        fun isCamino(x: Int, y: Int): Boolean {
            return isValidPosition(x, y) && mapaAuditorio[y][x] == CAMINO
        }

        // Obtiene las posiciones iniciales válidas para el spawn de jugadores
        fun getSpawnPoints(): List<Pair<Int, Int>> {
            val spawnPoints = mutableListOf<Pair<Int, Int>>()
            
            // Buscar entradas como puntos de spawn
            for (y in 0 until MAP_HEIGHT) {
                for (x in 0 until MAP_WIDTH) {
                    if (mapaAuditorio[y][x] == ENTRADA) {
                        spawnPoints.add(Pair(x, y))
                    }
                }
            }
            
            return spawnPoints
        }

        // Obtiene las posiciones cercanas transitables a una posición dada
        fun getAdjacentWalkablePositions(x: Int, y: Int): List<Pair<Int, Int>> {
            val adjacent = mutableListOf<Pair<Int, Int>>()
            val directions = listOf(
                Pair(-1, 0), // Izquierda
                Pair(1, 0),  // Derecha
                Pair(0, -1), // Arriba
                Pair(0, 1)   // Abajo
            )

            for (dir in directions) {
                val newX = x + dir.first
                val newY = y + dir.second
                if (isValidPosition(newX, newY) && 
                    (mapaAuditorio[newY][newX] == CAMINO || 
                     mapaAuditorio[newY][newX] == ENTRADA || 
                     mapaAuditorio[newY][newX] == ESCALERAS)) {
                    adjacent.add(Pair(newX, newY))
                }
            }

            return adjacent
        }
    }
}

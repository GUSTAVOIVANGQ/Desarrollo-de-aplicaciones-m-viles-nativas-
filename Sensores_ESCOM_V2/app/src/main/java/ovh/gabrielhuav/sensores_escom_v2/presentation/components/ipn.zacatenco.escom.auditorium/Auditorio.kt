class AuditorioMap {
    companion object {
        // Constantes para los tipos de celdas
        const val PARED = 0        // Paredes y obstáculos
        const val CAMINO = 1       // Espacio transitable
        const val ENTRADA = 2      // Entradas/salidas
        const val ASIENTOS = 3     // Área de asientos
        const val ESCALERAS = 4    // Escaleras
        const val ESCENARIO = 5    // Escenario

        // Dimensiones del mapa basadas exactamente en el ASCII art
        val MAP_WIDTH = 81  // Ancho exacto del ASCII art
        val MAP_HEIGHT = 84 // Alto exacto del ASCII art

        // Matriz del auditorio con representación exacta del ASCII art
        val mapaAuditorio = Array(MAP_HEIGHT) { y ->
            Array(MAP_WIDTH) { x ->
                when (y) {
                    0 -> when (x) {
                        in 0..0 -> PARED     // ╔
                        in 1..10 -> PARED    // ══════════
                        in 11..11 -> PARED   // ╦
                        in 12..66 -> PARED   // ═════════════════════════════════════════════════════════════════
                        in 67..67 -> PARED   // ╦
                        in 68..75 -> PARED   // ════════
                        in 76..76 -> PARED   // ╗
                        else -> CAMINO
                    }
                    1 -> when (x) {
                        in 0..0 -> PARED     // ║
                        in 1..10 -> CAMINO   // "          "
                        in 11..11 -> PARED   // ║
                        in 12..25 -> CAMINO  // "              "
                        in 26..49 -> ASIENTOS // ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓
                        in 50..66 -> CAMINO  // "                   "
                        in 67..67 -> PARED   // ║
                        in 68..75 -> CAMINO  // "       "
                        in 76..76 -> PARED   // ║
                        else -> CAMINO
                    }
                    2 -> when (x) {
                        in 0..0 -> PARED     // ║
                        in 1..10 -> CAMINO   // "          "
                        in 11..11 -> PARED   // ║
                        in 12..66 -> CAMINO  // "                                                                  "
                        in 67..67 -> PARED   // ║
                        in 68..75 -> CAMINO  // "       "
                        in 76..76 -> PARED   // ║
                        else -> CAMINO
                    }
                    3 -> when (x) {
                        in 0..0 -> PARED     // ║
                        in 1..10 -> CAMINO   // "          "
                        in 11..11 -> PARED   // ║
                        in 12..66 -> CAMINO  // "                                                                  "
                        in 67..67 -> PARED   // ║
                        in 68..75 -> CAMINO  // "       "
                        in 76..76 -> PARED   // ║
                        else -> CAMINO
                    }
                    4 -> when (x) {
                        in 0..0 -> PARED     // ║
                        in 1..10 -> CAMINO   // "          "
                        in 11..11 -> PARED   // ║
                        in 12..30 -> CAMINO  // "                   "
                        in 31..38 -> ESCENARIO // "Scenario"
                        in 39..66 -> CAMINO  // "                                       "
                        in 67..67 -> PARED   // ║
                        in 68..75 -> CAMINO  // "       "
                        in 76..76 -> PARED   // ║
                        else -> CAMINO
                    }
                    5 -> when (x) {
                        in 0..0 -> PARED     // ║
                        in 1..10 -> CAMINO   // "          "
                        in 11..11 -> PARED   // ║
                        in 12..66 -> CAMINO  // "                                                                  "
                        in 67..67 -> PARED   // ║
                        in 68..75 -> CAMINO  // "       "
                        in 76..76 -> PARED   // ║
                        else -> CAMINO
                    }
                    6 -> when (x) {
                        in 0..0 -> PARED     // ║
                        in 1..10 -> CAMINO   // "          "
                        in 11..11 -> PARED   // ║
                        in 12..19 -> CAMINO  // " Ladders"
                        in 20..20 -> PARED   // ║
                        in 21..65 -> CAMINO  // "                                                 "
                        in 66..66 -> PARED   // ║
                        in 67..67 -> PARED   // ║
                        in 68..75 -> CAMINO  // "       "
                        in 76..76 -> PARED   // ║
                        else -> CAMINO
                    }
                    7 -> when (x) {
                        in 0..0 -> PARED     // ║
                        in 1..10 -> CAMINO   // "          "
                        in 11..11 -> PARED   // ║
                        in 12..18 -> CAMINO  // " ↗️↘️  "
                        in 19..19 -> PARED   // ║
                        in 20..64 -> CAMINO  // "                                                  "
                        in 65..65 -> PARED   // ║
                        in 66..67 -> CAMINO  // " "
                        in 68..73 -> CAMINO  // "↗️↘️ "
                        in 74..75 -> CAMINO  // " "
                        in 76..76 -> PARED   // ║
                        else -> CAMINO
                    }
                    8 -> when (x) {
                        in 0..0 -> PARED     // ╠
                        in 1..10 -> PARED    // ══════════
                        in 11..11 -> PARED   // ╩
                        in 12..19 -> CAMINO  // "       "
                        in 20..20 -> PARED   // ╩
                        in 21..64 -> PARED   // ══════════════════════════════════════════════════
                        in 65..65 -> PARED   // ╩
                        in 66..73 -> CAMINO  // "       "
                        in 74..75 -> PARED   // ╩═
                        in 76..76 -> PARED   // ╣
                        else -> CAMINO
                    }
                    9 -> when (x) {
                        in 0..1 -> ENTRADA   // 🚪E
                        in 2..67 -> CAMINO   // "xit                                                                            "
                        in 68..73 -> CAMINO  // "Exit"
                        in 74..74 -> ENTRADA // 🚪
                        else -> CAMINO
                    }
                    10 -> when (x) {
                        in 0..1 -> ENTRADA   // 🚪E
                        in 2..67 -> CAMINO   // "xit                                                                            "
                        in 68..73 -> CAMINO  // "Exit"
                        in 74..74 -> ENTRADA // 🚪
                        else -> CAMINO
                    }
                    11 -> when (x) {
                        in 0..0 -> PARED     // ║
                        in 1..79 -> CAMINO   // "                                                                                     "
                        in 80..80 -> PARED   // ║
                        else -> CAMINO
                    }
                    12 -> when (x) {
                        in 0..0 -> PARED     // ║
                        in 1..1 -> CAMINO    // " "
                        in 2..2 -> PARED     // ┌
                        in 3..26 -> PARED    // ────────────────────────
                        in 27..27 -> PARED   // ┐
                        in 28..34 -> CAMINO  // "       "
                        in 35..35 -> PARED   // ┌
                        in 36..61 -> PARED   // ──────────────────────────────
                        in 62..62 -> PARED   // ┐
                        in 63..69 -> CAMINO  // "       "
                        in 70..70 -> PARED   // ┌
                        in 71..82 -> PARED   // ────────────────
                        in 83..83 -> PARED   // ┐
                        in 84..84 -> CAMINO  // "  "
                        in 85..85 -> PARED   // ║
                        else -> CAMINO
                    }
                    in 13..24 -> when (x) {
                        in 0..0 -> PARED     // ║
                        in 1..1 -> CAMINO    // " "
                        in 2..2 -> PARED     // │
                        in 3..26 -> ASIENTOS // "   [_]   [_]   [_]  " (área de asientos)
                        in 27..27 -> PARED   // │
                        in 28..34 -> CAMINO  // "       "
                        in 35..35 -> PARED   // │
                        in 36..61 -> ASIENTOS // "   [_]   [_]   [_]   [_]  " (área de asientos)
                        in 62..62 -> PARED   // │
                        in 63..69 -> CAMINO  // "       "
                        in 70..70 -> PARED   // │
                        in 71..82 -> ASIENTOS // " [_]   [_]   [_]" (área de asientos)
                        in 83..83 -> PARED   // │
                        in 84..84 -> CAMINO  // "  "
                        in 85..85 -> PARED   // ║
                        else -> CAMINO
                    }
                    19 -> when (x) {
                        in 0..0 -> PARED     // ║
                        in 1..1 -> CAMINO    // " "
                        in 2..2 -> PARED     // └
                        in 3..26 -> PARED    // ────────────────────────
                        in 27..27 -> PARED   // ┘
                        in 28..34 -> CAMINO  // "       "
                        in 35..35 -> PARED   // └
                        in 36..61 -> PARED   // ──────────────────────────────
                        in 62..62 -> PARED   // ┘
                        in 63..69 -> CAMINO  // "       "
                        in 70..70 -> PARED   // └
                        in 71..82 -> PARED   // ────────────────
                        in 83..83 -> PARED   // ┘
                        in 84..84 -> CAMINO  // "  "
                        in 85..85 -> PARED   // ║
                        else -> CAMINO
                    }
                    20 -> when (x) {
                        in 0..0 -> PARED     // ║
                        in 1..79 -> CAMINO   // "                                                                                     "
                        in 80..80 -> PARED   // ║
                        else -> CAMINO
                    }
                    21 -> when (x) {
                        in 0..0 -> PARED     // ╠
                        in 1..10 -> PARED    // ══════════
                        in 11..11 -> PARED   // ╦
                        in 12..63 -> PARED   // ═══════════════════════════════════════════════════════════════
                        in 64..64 -> PARED   // ╦
                        in 65..74 -> PARED   // ══════════
                        in 75..75 -> PARED   // ╣
                        else -> CAMINO
                    }
                    22 -> when (x) {
                        in 0..0 -> PARED     // ║
                        in 1..10 -> CAMINO   // "          "
                        in 11..11 -> PARED   // ║
                        in 12..63 -> CAMINO  // "                                                               "
                        in 64..64 -> PARED   // ║
                        in 65..74 -> CAMINO  // "          "
                        in 75..75 -> PARED   // ║
                        else -> CAMINO
                    }
                    23 -> when (x) {
                        in 0..0 -> PARED     // ╚
                        in 1..1 -> PARED     // ═
                        in 2..3 -> ENTRADA   // 🚪🚪
                        in 4..10 -> CAMINO   // "      "
                        in 11..11 -> PARED   // ╚
                        in 12..63 -> PARED   // ═══════════════════════════════════════════════════════════════
                        in 64..64 -> PARED   // ╝
                        in 65..70 -> CAMINO  // "     "
                        in 71..72 -> ENTRADA // 🚪🚪
                        in 73..74 -> PARED   // ═╝
                        else -> CAMINO
                    }
                    else -> CAMINO
                }
            }
        }

        // Verifica si una posición es válida y transitable
        fun isValidPosition(x: Int, y: Int): Boolean {
            return x >= 0 && x < MAP_WIDTH && y >= 0 && y < MAP_HEIGHT
        }

        // Obtiene el valor en una posición específica
        fun getMapValue(x: Int, y: Int): Int {
            return if (isValidPosition(x, y)) {
                mapaAuditorio[y][x]
            } else {
                PARED
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

        // Verifica si una posición es el escenario
        fun isEscenario(x: Int, y: Int): Boolean {
            return isValidPosition(x, y) && mapaAuditorio[y][x] == ESCENARIO
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
                // Solo verificamos que esté dentro de los límites del mapa
                if (isValidPosition(newX, newY)) {
                    adjacent.add(Pair(newX, newY))
                }
            }

            return adjacent
        }
    }
}
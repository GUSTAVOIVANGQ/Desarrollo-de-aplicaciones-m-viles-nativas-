package ovh.gabrielhuav.sensores_escom_v2.presentation.components.mapview

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log

/**
 * Provee matrices específicas para cada mapa del juego.
 * Cada mapa tiene su propia configuración de colisiones y puntos interactivos.
 */
class MapMatrixProvider {
    companion object {
        // Constantes compartidas para tipos de celdas
        const val INTERACTIVE = 0
        const val WALL = 1
        const val PATH = 2
        const val INACCESSIBLE = 3

        // Tamaño estándar de la matriz
        const val MAP_WIDTH = 40
        const val MAP_HEIGHT = 40

        // Constantes para los mapas
        const val MAP_MAIN = "escom_main"
        const val MAP_BUILDING2 = "escom_building2"
        const val MAP_SALON2009 = "escom_salon2009"
        const val MAP_SALON2010 = "escom_salon2010"
        const val MAP_CAFETERIA = "escom_cafeteria"
        const val MAP_BIBLIOTECA = "escom_biblioteca"
        const val MAP_AUDITORIO = "escom_auditorio"
        const val MAP_HOUSE = "house" // Añadir constante para el mapa de la casa

        // Puntos de transición entre mapas
        val MAIN_TO_BUILDING2_POSITION = Pair(15, 10)
        val BUILDING2_TO_MAIN_POSITION = Pair(5, 5)  // Posición segura en la esquina superior izquierda
        val BUILDING2_TO_SALON2009_POSITION = Pair(15, 16)  // Punto en el pasillo principal
        val SALON2009_TO_BUILDING2_POSITION = Pair(1, 20)  // Punto en la puerta del salón

        val BUILDING2_TO_SALON2010_POSITION = Pair(20, 20)  // Desde edificio 2
        val MAIN_TO_SALON2010_POSITION = Pair(25, 25)       // Desde mapa principal
        val SALON2010_TO_BUILDING2_POSITION = Pair(5, 5)    // Vuelta al edificio 2
        val SALON2010_TO_MAIN_POSITION = Pair(1, 1)         // Vuelta al mapa principal

        val MAIN_TO_CAFETERIA_POSITION = Pair(2, 2)       // Desde mapa principal
        val CAFETERIA_TO_MAIN_POSITION = Pair(1, 1)         // Vuelta al mapa principal

        val MAIN_TO_BIBLIOTECA_POSITION = Pair(30, 30)  // Desde mapa principal
        val BIBLIOTECA_TO_MAIN_POSITION = Pair(5, 38)   // Vuelta al mapa principal

        // Agregar punto de transición para el auditorio
        val MAIN_TO_AUDITORIO_POSITION = Pair(7, 20)
        val AUDITORIO_TO_MAIN_POSITION = Pair(1, 1)
        val MAIN_TO_HOUSE_POSITION = Pair(10, 0)  // Nueva posición desde el mapa principal
        val HOUSE_TO_MAIN_POSITION = Pair(1, 38)  // Mantener la posición de regreso

        /**
         * Obtiene la matriz para el mapa especificado
         */
        fun getMatrixForMap(mapId: String): Array<Array<Int>> {
            return when (mapId) {
                MAP_MAIN -> createMainMapMatrix()
                MAP_BUILDING2 -> createBuilding2Matrix()
                MAP_SALON2009 -> createSalon2009Matrix()  // Nueva matriz para el salón 2009
                MAP_SALON2010 -> createSalon2010Matrix()  // Nueva matriz para el salón 2010
                MAP_CAFETERIA -> createCafeESCOMMatrix()
                MAP_BIBLIOTECA -> createBibliotecaMatrix()
                MAP_AUDITORIO -> createAuditorioMatrix()
                MAP_HOUSE -> createHouseMatrix() // Añadir el nuevo mapa
                else -> createDefaultMatrix() // Por defecto, un mapa básico
            }
        }

        /**
         * Matriz para el mapa principal del campus
         */
        private fun createMainMapMatrix(): Array<Array<Int>> {
            val matrix = Array(MAP_HEIGHT) { Array(MAP_WIDTH) { PATH } }

            // Configuración de bordes
            for (i in 0 until MAP_HEIGHT) {
                for (j in 0 until MAP_WIDTH) {
                    // Bordes exteriores
                    if (i == 0 || i == MAP_HEIGHT - 1 || j == 0 || j == MAP_WIDTH - 1) {
                        matrix[i][j] = WALL
                    }
                    // Zonas interactivas (edificios, entradas)
                    else if ((i == 10 && j == 15) || // Entrada al edificio 2
                            (i == 7 && j == 20) || // Entrada al auditorio
                            (i == 2 && j == 7))    // Entrada a la casa
                    {
                        matrix[i][j] = INTERACTIVE
                    }
                    // Obstáculos (árboles, bancas, etc)
                    else if (i % 7 == 0 && j % 8 == 0) {
                        matrix[i][j] = INACCESSIBLE
                    }
                    // Caminos especiales
                    else if ((i % 5 == 0 || j % 5 == 0) && i > 5 && j > 5) {
                        matrix[i][j] = PATH
                    }
                }
            }

            // Áreas de juego específicas
            // Zona central despejada
            for (i in 15..25) {
                for (j in 15..25) {
                    matrix[i][j] = PATH
                }
            }

            return matrix
        }

        /**
         * Matriz para el edificio 2
         * Basada exactamente en el mapa ASCII:
         * +-------------------------------------------------------------------------+
         * |                               Edificio 2                                |
         * |                              Planta Baja                                |
         * |                                                                         |
         * |  +--------+--------+--------+-----+--------+--------+--------+----+     |
         * |  |  2001  |  2002  |  2003  | ⬆️  |  2004  |  2005  |  2006  | 🚾 |     |
         * |  |🏫 Aula |🏫 Aula |🏫 Aula | 🪜  |🏫 Aula |🏫 Aula |🏫 Aula | WC |     |
         * |  +🚪------+🚪------+🚪------+ ⬇️  +🚪------+🚪------+🚪------+🚪--+     |
         * |                                                                         |
         * |                      [    Pasillo Principal 🚶    ]                     |
         * |                                                                         |
         * +-------------------------------------------------------------------------+
         */
        /**
         * Matrix for Building 2
         * Based exactly on the ASCII map:
         * +-------------------------------------------------------------------------+
         * |                               Edificio 2                                |
         * |                              Planta Baja                                |
         * |                                                                         |
         * |  +--------+--------+--------+-----+--------+--------+--------+----+     |
         * |  |  2001  |  2002  |  2003  | ⬆️  |  2004  |  2005  |  2006  | 🚾 |     |
         * |  |🏫 Aula |🏫 Aula |🏫 Aula | 🪜  |🏫 Aula |🏫 Aula |🏫 Aula | WC |     |
         * |  +🚪------+🚪------+🚪------+ ⬇️  +🚪------+🚪------+🚪------+🚪--+     |
         * |                                                                         |
         * |                      [    Pasillo Principal 🚶    ]                     |
         * |                                                                         |
         * +-------------------------------------------------------------------------+
         */

        /**
        *Matrix for auditorio
         * Based exactly on the ASCII map:
        ╔══════════╦═════════════════════════════════════════════════════════════════╦════════╗
        ║          ║              ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓                   ║       ║
        ║          ║                                                                  ║       ║
        ║          ║                                                                  ║       ║
        ║          ║                   Scenario                                       ║       ║
        ║          ║                                                                  ║       ║
        ║          ║ Ladders║                                                 ║Ladders║       ║
        ║          ║ ↗️↘️  ║                                                  ║ ↗️↘️ ║       ║
        ╠══════════╩       ╩══════════════════════════════════════════════════╩       ╩═══════╣
        🚪Exit                                                                            Exit🚪
        🚪Exit                                                                            Exit🚪
        ║                                                                                     ║
        ║ ┌────────────────────┐       ┌──────────────────────────┐       ┌────────────────┐  ║
        ║ │   [_]   [_]   [_]  │       │   [_]   [_]   [_]   [_]  │       │ [_]   [_]   [_]│  ║
        ║ │   [_]   [_]   [_]  │       │   [_]   [_]   [_]   [_]  │       │ [_]   [_]   [_]│  ║
        ║ │   [_]   [_]   [_]  │       │   [_]   [_]   [_]   [_]  │       │ [_]   [_]   [_]│  ║
        ║ │   [_]   [_]   [_]  │       │   [_]   [_]   [_]   [_]  │       │ [_]   [_]   [_]│  ║
        ║ │   [_]   [_]   [_]  │       │   [_]   [_]   [_]   [_]  │       │ [_]   [_]   [_]│  ║
        ║ │   [_]   [_]   [_]  │       │   [_]   [_]   [_]   [_]  │       │ [_]   [_]   [_]│  ║
        ║ │   [_]   [_]   [_]  │       │   [_]   [_]   [_]   [_]  │       │ [_]   [_]   [_]│  ║
        ║ │   [_]   [_]   [_]  │       │   [_]   [_]   [_]   [_]  │       │ [_]   [_]   [_]│  ║
        ║ │   [_]   [_]   [_]  │       │   [_]   [_]   [_]   [_]  │       │ [_]   [_]   [_]│  ║
        ║ │   [_]   [_]   [_]  │       │   [_]   [_]   [_]   [_]  │       │ [_]   [_]   [_]│  ║
        ║ │   [_]   [_]   [_]  │       │   [_]   [_]   [_]   [_]  │       │ [_]   [_]   [_]│  ║
        ║ │   [_]   [_]   [_]  │       │   [_]   [_]   [_]   [_]  │       │ [_]   [_]   [_]│  ║
        ║ │   [_]   [_]   [_]  │       │   [_]   [_]   [_]   [_]  │       │ [_]   [_]   [_]│  ║
        ║ │   [_]   [_]   [_]  │       │   [_]   [_]   [_]   [_]  │       │ [_]   [_]   [_]│  ║
        ║ │   [_]   [_]   [_]  │       │   [_]   [_]   [_]   [_]  │       │ [_]   [_]   [_]│  ║
        ║ │   [_]   [_]   [_]  │       │   [_]   [_]   [_]   [_]  │       │ [_]   [_]   [_]│  ║
        ║ │   [_]   [_]   [_]  │       │   [_]   [_]   [_]   [_]  │       │ [_]   [_]   [_]│  ║
        ║ │   [_]   [_]   [_]  │       │   [_]   [_]   [_]   [_]  │       │ [_]   [_]   [_]│  ║
        ║ └────────────────────┘       └──────────────────────────┘       └────────────────┘  ║
        ║                                                                                     ║
        ╠══════════╦═══════════════════════════════════════════════════════════════╦══════════╣
        ║          ║                                                               ║          ║
        ╚═🚪🚪      ╚═══════════════════════════════════════════════════════════════╝     🚪🚪═╝
        */

        /**
         * Approach 5: ASCII Art-Based Implementation
         * Creates the matrix directly from an ASCII representation
         */
        /**
         * Implementación combinada mejorada para el Edificio 2
         * Combina elementos de los enfoques 1 y 5 con mejoras
         */
        private fun createBuilding2Matrix(): Array<Array<Int>> {
            // Crear matriz con PATH (caminable) por defecto
            val matrix = Array(MAP_HEIGHT) { Array(MAP_WIDTH) { PATH } }

            // Constantes para dimensiones del edificio
            val roomTop = 8           // Posición superior de las aulas
            val roomHeight = 8        // Altura de las aulas (más grandes)
            val roomBottom = roomTop + roomHeight
            val corridorTop = roomBottom + 1
            val corridorHeight = 3    // Altura del pasillo principal
            val corridorBottom = corridorTop + corridorHeight

            // Número de aulas + baño
            val numRooms = 7
            val roomWidth = (MAP_WIDTH - 2) / numRooms

            // Crear bordes del edificio
            // Borde superior del edificio
            for (x in 0 until MAP_WIDTH) {
                matrix[roomTop - 1][x] = WALL
            }

            // Borde inferior del edificio
            if (corridorBottom + 1 < MAP_HEIGHT) {
                for (x in 0 until MAP_WIDTH) {
                    matrix[corridorBottom + 1][x] = WALL
                }
            }

            // Bordes laterales del edificio
            for (y in roomTop - 1..corridorBottom + 1) {
                if (y < MAP_HEIGHT) {
                    matrix[y][0] = WALL
                    if (MAP_WIDTH - 1 < MAP_WIDTH) {
                        matrix[y][MAP_WIDTH - 1] = WALL
                    }
                }
            }

            // Crear divisiones verticales entre aulas
            for (i in 0..numRooms) {
                val x = 1 + (i * roomWidth)
                if (x < MAP_WIDTH) {
                    for (y in roomTop until roomBottom) {
                        matrix[y][x] = WALL
                    }
                }
            }

            // Bordes horizontales de las aulas
            for (x in 1 until MAP_WIDTH - 1) {
                // Borde superior de las aulas
                matrix[roomTop][x] = WALL

                // Borde inferior de las aulas (justo encima del pasillo)
                matrix[roomBottom][x] = WALL
            }

            // Crear el área de escaleras (entre las aulas 3 y 4)
            val stairsIndex = 3
            val stairsX = 1 + (stairsIndex * roomWidth)

            // Limpiar el área de escaleras
            for (y in roomTop + 1 until roomBottom) {
                for (x in stairsX until stairsX + roomWidth) {
                    if (x < MAP_WIDTH) {
                        matrix[y][x] = PATH
                    }
                }
            }

            // Hacer las escaleras interactivas
            val stairsCenterX = stairsX + roomWidth/2
            val stairsCenterY = roomTop + roomHeight/2

            // Definir área interactiva alrededor del centro
            for (y in stairsCenterY - 1..stairsCenterY + 1) {
                for (x in stairsCenterX - 1..stairsCenterX + 1) {
                    if (x >= 0 && x < MAP_WIDTH && y >= 0 && y < MAP_HEIGHT) {
                        matrix[y][x] = INTERACTIVE
                    }
                }
            }

            // Crear puertas para cada aula
            for (i in 0 until numRooms) {
                if (i == stairsIndex) continue // Saltar escaleras

                val doorX = 1 + (i * roomWidth) + (roomWidth / 2)
                if (doorX < MAP_WIDTH) {
                    matrix[roomBottom][doorX] = PATH

                    // Hacer las puertas más anchas para facilitar el acceso
                    if (doorX - 1 >= 0) matrix[roomBottom][doorX - 1] = PATH
                    if (doorX + 1 < MAP_WIDTH) matrix[doorX + 1][doorX + 1] = PATH
                }
            }

            // Crear pasillo principal - amplio y completamente caminable
            for (y in corridorTop until corridorTop + corridorHeight) {
                if (y < MAP_HEIGHT) {
                    for (x in 1 until MAP_WIDTH - 1) {
                        matrix[y][x] = PATH
                    }
                }
            }

            // Añadir puntos interactivos para las transiciones

            // Entrada a la sala 2009 (en el pasillo, centrado)
            val corridorCenterY = corridorTop + corridorHeight/2

            // Múltiples puntos interactivos a lo largo del pasillo
            val interactivePoints = listOf(
                (MAP_WIDTH / 2),
                (MAP_WIDTH / 3),
                (2 * MAP_WIDTH / 3),
                stairsCenterX
            )

            for (x in interactivePoints) {
                if (x >= 0 && x < MAP_WIDTH && corridorCenterY >= 0 && corridorCenterY < MAP_HEIGHT) {
                    matrix[corridorCenterY][x] = INTERACTIVE
                }
            }

            // Salida al mapa principal (lado izquierdo)
            if (corridorCenterY < MAP_HEIGHT) {
                matrix[corridorCenterY][2] = INTERACTIVE
            }

            // Hacer el interior de las aulas navegable
            for (i in 0 until numRooms) {
                if (i == stairsIndex) continue  // Saltar escaleras

                val roomStartX = 1 + (i * roomWidth) + 1
                val roomEndX = 1 + ((i + 1) * roomWidth) - 1

                for (y in roomTop + 1 until roomBottom) {
                    for (x in roomStartX until roomEndX + 1) {
                        if (x < MAP_WIDTH) {
                            matrix[y][x] = PATH
                        }
                    }
                }
            }

            return matrix
        }


        /**
         * Matriz para el salón 2009
         */
        private fun createSalon2009Matrix(): Array<Array<Int>> {
            val matrix = Array(MAP_HEIGHT) { Array(MAP_WIDTH) { WALL } }

            // Dimensiones del aula
            val roomWidth = 30
            val roomHeight = 25
            val startX = 5
            val startY = 5

            // Interior del salón (espacio abierto)
            for (i in startY until startY + roomHeight) {
                for (j in startX until startX + roomWidth) {
                    matrix[i][j] = PATH
                }
            }

            // Puerta de salida hacia el edificio 2 (lado izquierdo)
            matrix[startY + roomHeight/2][1] = INTERACTIVE

            // Pizarrón (pared frontal)
            for (j in startX + 2 until startX + roomWidth - 2) {
                matrix[startY + 1][j] = INACCESSIBLE
            }
            // Centro del pizarrón es interactivo
            matrix[startY + 1][startX + roomWidth/2] = INTERACTIVE

            // Escritorio del profesor
            for (j in startX + 10 until startX + 20) {
                for (i in startY + 3 until startY + 6) {
                    matrix[i][j] = INACCESSIBLE
                }
            }

            // Filas de pupitres para estudiantes
            for (row in 0 until 4) {
                val rowY = startY + 8 + (row * 4)

                // 5 pupitres por fila
                for (desk in 0 until 5) {
                    val deskX = startX + 3 + (desk * 5)

                    // Cada pupitre ocupa 3x2
                    for (i in rowY until rowY + 2) {
                        for (j in deskX until deskX + 3) {
                            matrix[i][j] = INACCESSIBLE
                        }
                    }
                }
            }

            return matrix
        }

        private fun createSalon2010Matrix(): Array<Array<Int>> {
            val matrix = Array(MAP_HEIGHT) { Array(MAP_WIDTH) { PATH } }

            // Configuración de bordes
            for (i in 0 until MAP_HEIGHT) {
                for (j in 0 until MAP_WIDTH) {
                    // Bordes exteriores
                    if (i == 0 || i == MAP_HEIGHT - 1 || j == 0 || j == MAP_WIDTH - 1) {
                        matrix[i][j] = WALL
                    }
                    // Zonas interactivas (edificios, entradas)
                    else if (i == 10 && j == 15) {
                        matrix[i][j] = INTERACTIVE // Entrada al edificio 2
                    }
                    // Obstáculos (árboles, bancas, etc)
                    else if (i % 7 == 0 && j % 8 == 0) {
                        matrix[i][j] = INACCESSIBLE
                    }
                    // Caminos especiales
                    else if ((i % 5 == 0 || j % 5 == 0) && i > 5 && j > 5) {
                        matrix[i][j] = PATH
                    }
                }
            }

            // Áreas de juego específicas
            // Zona central despejada
            for (i in 15..25) {
                for (j in 15..25) {
                    matrix[i][j] = PATH
                }
            }

            return matrix
        }

        /**
         * Matriz para el mapa principal del campus
         */
        private fun createCafeESCOMMatrix(): Array<Array<Int>> {
            val matrix = Array(MAP_HEIGHT) { Array(MAP_WIDTH) { PATH } }

            // Definición de constantes para mejorar legibilidad
            val PARED = WALL
            val CAMINO = PATH
            val BANCA = INACCESSIBLE
            val INTERACTIVO = INTERACTIVE

            // Bordes exteriores - paredes del restaurante
            for (i in 0 until MAP_HEIGHT) {
                for (j in 0 until MAP_WIDTH) {
                    // Bordes exteriores
                    if (i == 0 || i == MAP_HEIGHT - 1 || j == 0 || j == MAP_WIDTH - 1) {
                        matrix[i][j] = PARED
                    }
                }
            }

            // COCINA (esquina superior izquierda)
            for (i in 2..8) {
                for (j in 2..15) {
                    if (i == 2 || i == 8 || j == 2 || j == 15) {
                        matrix[i][j] = PARED // Paredes de la cocina
                    }
                }
            }
            // Mostrador de la cocina
            for (i in 4..6) {
                for (j in 4..13) {
                    matrix[i][j] = BANCA
                }
            }

            // MESAS/BANCAS LARGAS (3 filas de 3 mesas cada una)
            // Primera fila de mesas
            for (row in 0..2) {
                for (col in 0..2) {
                    // Cada mesa es un rectángulo
                    val baseI = 12 + (row * 8)
                    val baseJ = 10 + (col * 10)

                    for (i in baseI..baseI+2) {
                        for (j in baseJ..baseJ+8) {
                            matrix[i][j] = BANCA
                        }
                    }
                }
            }

            // CAJA (parte inferior)
            for (i in 30..33) {
                for (j in 15..19) {
                    matrix[i][j] = BANCA
                }
            }

            // ENTRADA
            for (i in 37..38) {
                for (j in 15..25) {
                    matrix[i][j] = INTERACTIVO
                }
            }

            // Agregar elementos interactivos: Tacos, Burritos, Guacamole y Chile
            // Tacos (representados como puntos interactivos)
            matrix[12][8] = INTERACTIVO
            matrix[12][32] = INTERACTIVO
            matrix[28][8] = INTERACTIVO
            matrix[28][32] = INTERACTIVO

            // Burritos
            matrix[12][33] = INTERACTIVO
            matrix[28][33] = INTERACTIVO

            // Guacamole
            matrix[20][8] = INTERACTIVO

            // Chile
            matrix[20][32] = INTERACTIVO

            return matrix
        }

        /**
         * Matriz para la biblioteca
         */
        private fun createBibliotecaMatrix(): Array<Array<Int>> {
            val matrix = Array(MAP_HEIGHT) { Array(MAP_WIDTH) { WALL } }

            // Dimensiones de la biblioteca
            val startX = 5
            val startY = 5
            val width = 30
            val height = 30

            // Crear espacio interior principal
            for (y in startY until startY + height) {
                for (x in startX until startX + width) {
                    matrix[y][x] = PATH
                }
            }

            // Estanterías de libros (como obstáculos)
            for (row in 0..4) {  // 5 filas de estanterías
                val shelfY = startY + 5 + (row * 5)
                for (x in startX + 2 until startX + width - 2 step 4) {
                    // Cada estantería ocupa 2x4 espacios
                    for (dy in 0..1) {
                        for (dx in 0..2) {
                            matrix[shelfY + dy][x + dx] = INACCESSIBLE
                        }
                    }
                }
            }

            // Área de lectura (mesas y sillas como obstáculos)
            val readingAreaY = startY + height - 8
            for (x in startX + 5 until startX + width - 5 step 6) {
                // Mesas de estudio (2x3)
                for (dy in 0..1) {
                    for (dx in 0..2) {
                        matrix[readingAreaY + dy][x + dx] = INACCESSIBLE
                    }
                }
            }

            // Mostrador de préstamos (punto interactivo)
            for (x in startX + 10..startX + 15) {
                matrix[startY + 2][x] = INTERACTIVE
            }

            // Punto de salida (interactivo)
            matrix[BIBLIOTECA_TO_MAIN_POSITION.second][BIBLIOTECA_TO_MAIN_POSITION.first] = INTERACTIVE

            return matrix
        }

        /**
         * Matriz para el auditorio
         */
        private fun createAuditorioMatrix(): Array<Array<Int>> {
            return AuditorioMap.mapaAuditorio
        }
        /**
         * Matriz para la casa (exterior)
         * Basada en el siguiente diseño ASCII:
        ╔══════════════════════════════════════════════════╗
        ║ 🏠      🏠     🌳     🏢           🏪     🏠   ║
        ╠═════╪═══╪═════╪═══╪═════╪═══╪═════╪═══╪═══╪══════╣
        ║                                                  ║
        ║=====    =====    =====    =====        =====     ║
        ║                                                  ║ 
        ║ 🏠      🏠      🏠      🏠          🏠      🏠  ║
        ╠═════╪═══╪═════╪═══╪═════╪═══╪═════╪═══╪═══╪══════╣
        ║                                                  ║ 
        ║                                                  ║ 
        ║≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡ ║
        ║                                                  ║ 
        ║≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡  ║
        ║                                                  ║ 
        ║                      🌉                         ║
        ╠═════╪═══╪═════╪═══╪═════╪═══╪═════╪═══╪═══╪════ ═╣
        ║ 🏠      🏠      🏠      🏠          🏠      🏠 ║
        ╠═════╪═══╪═════╪═══╪═════╪═══╪═════╪═══╪═══╪═════ ╣
        ║ 🚶      🚶      🚶      🚶          🚶      🚶     ║
        ║                                                  ║ 
        ╚══════════════════════════════════════════════════╝
         */


        /**
         * Matriz para el interior de la casa
         * Basada en el siguiente diseño ASCII:
         * ╔══════════════════════════════════╗
         * ║         Entrada Principal        ║
         * ║              ▓▓▓▓                ║
         * ║     Jardin   ▓▓▓▓   Garage       ║
         * ║              ▓▓▓▓                ║
         * ║     🌳🌳     ▓▓▓▓     🚗        ║
         * ║              ▓▓▓▓                ║
         * ╠══════════════════════════════════╣
         * ║    Sala      Comedor   Cocina    ║
         * ║                                  ║
         * ║   [_]  [_]   [__]     🍳 🥘     ║
         * ║     TV                           ║
         * ╠═══════════╦════════════════════╗ ║
         * ║  Baño     ║     Recámara       ║ ║
         * ║   🚽      ║     [___]          ║ ║
         * ║           ║        🛏️          ║ ║
         * ╚═══════════╩════════════════════╝ ║
         * ╚══════════════════════════════════╝
         */
        private fun createHouseMatrix(): Array<Array<Int>> {
            val matrix = Array(MAP_HEIGHT) { Array(MAP_WIDTH) { PATH } }
            
            // Constantes para mejorar legibilidad
            val PARED = WALL
            val CAMINO = PATH
            val MUEBLE = INACCESSIBLE
            val ENTRADA = INTERACTIVE

            // Bordes exteriores de la casa
            for (i in 0 until MAP_HEIGHT) {
                for (j in 0 until MAP_WIDTH) {
                    if (i == 0 || i == MAP_HEIGHT - 1 || j == 0 || j == MAP_WIDTH - 1) {
                        matrix[i][j] = PARED
                    }
                }
            }

            // Entrada principal
            matrix[0][20] = ENTRADA
            
            // Jardín (área izquierda superior)
            for (i in 5..10) {
                for (j in 5..10) {
                    matrix[i][j] = PATH // Área verde
                }
            }

            // Garage (área derecha superior)
            for (i in 5..10) {
                for (j in 25..30) {
                    matrix[i][j] = PATH // Área para el auto
                }
            }

            // División interna (paredes)
            for (i in 15..25) {
                matrix[i][15] = PARED // Pared vertical central
            }

            // Muebles y objetos interactivos
            // Sala
            matrix[10][5] = MUEBLE  // Sofá
            matrix[10][7] = MUEBLE  // Sillón
            matrix[11][6] = INTERACTIVE // TV

            // Comedor
            matrix[10][20] = MUEBLE // Mesa
            matrix[11][20] = MUEBLE // Sillas

            // Cocina
            matrix[10][30] = INTERACTIVE // Estufa
            matrix[11][30] = INTERACTIVE // Refrigerador

            // Baño
            matrix[20][5] = INTERACTIVE // Inodoro
            matrix[20][7] = INTERACTIVE // Lavabo

            // Recámara
            matrix[20][25] = MUEBLE // Cama
            matrix[21][25] = INTERACTIVE // Ropero

            return matrix
        }

        /**
         * Matriz predeterminada para cualquier otro mapa
         */
        private fun createDefaultMatrix(): Array<Array<Int>> {
            val matrix = Array(MAP_HEIGHT) { Array(MAP_WIDTH) { PATH } }

            // Borde simple
            for (i in 0 until MAP_HEIGHT) {
                for (j in 0 until MAP_WIDTH) {
                    if (i == 0 || i == MAP_HEIGHT - 1 || j == 0 || j == MAP_WIDTH - 1) {
                        matrix[i][j] = WALL
                    }
                }
            }

            return matrix
        }

        /**
         * Comprueba si la coordenada especificada es un punto de transición entre mapas
         */
        fun isMapTransitionPoint(mapId: String, x: Int, y: Int): String? {
            // Imprimimos para depuración
            Log.d("MapTransition", "Checking transition at $mapId: ($x, $y)")

            // Para el edificio 2, cualquier punto interactivo cerca del centro del pasillo
            // nos lleva al salón 2009
            if (mapId == MAP_BUILDING2) {
                // Si estamos en o cerca de las coordenadas (15,16) o cualquiera de las alternativas
                val nearCenter = (x >= 14 && x <= 16 && y >= 15 && y <= 17)
                val alternative1 = (x == 20 && y == 20)
                val alternative2 = (x == 25 && y == 16)

                if (nearCenter || alternative1 || alternative2) {
                    Log.d("MapTransition", "Transition to salon2009 triggered!")
                    return MAP_SALON2009
                }

                if (x == 2 && y == 5) {
                    return MAP_SALON2010
                }

                // Punto para regresar al mapa principal
                if (x == 5 && y == 5) {
                    return MAP_MAIN
                }
            }

            // Si estamos en el salón 2009, la coordenada (1,20) nos lleva de vuelta al edificio 2
            if (mapId == MAP_SALON2009 && x == 1 && y == 20) {
                return MAP_BUILDING2
            }

            if (mapId == MAP_SALON2010) {
                if (x == 5 && y == 5) {
                    return MAP_BUILDING2
                }
                if (x == 10 && y == 10) {
                    return MAP_MAIN
                }
            }

            if (mapId == MAP_MAIN && x == 33 && y == 34) {
                return MAP_CAFETERIA
            }

            // Añadir transiciones para la biblioteca
            if (mapId == MAP_MAIN && x == MAIN_TO_BIBLIOTECA_POSITION.first && 
                y == MAIN_TO_BIBLIOTECA_POSITION.second) {
                return MAP_BIBLIOTECA
            }
            
            if (mapId == MAP_BIBLIOTECA && x == BIBLIOTECA_TO_MAIN_POSITION.first && 
                y == BIBLIOTECA_TO_MAIN_POSITION.second) {
                return MAP_MAIN
            }

            // Añadir transición al auditorio
            if (mapId == MAP_MAIN && x == MAIN_TO_AUDITORIO_POSITION.first && 
                y == MAIN_TO_AUDITORIO_POSITION.second) {
                return MAP_AUDITORIO
            }
            
            if (mapId == MAP_AUDITORIO && AuditorioMap.isExit(x, y)) {
                return MAP_MAIN
            }

            // Transiciones para la casa
            if (mapId == MAP_MAIN && x == MAIN_TO_HOUSE_POSITION.first && 
                y == MAIN_TO_HOUSE_POSITION.second) {
                return MAP_HOUSE
            }
            
            if (mapId == MAP_HOUSE && x == HOUSE_TO_MAIN_POSITION.first && 
                y == HOUSE_TO_MAIN_POSITION.second) {
                return MAP_MAIN
            }

            return null
        }


        /**
         * Obtiene la posición inicial para un mapa destino
         */
        fun getInitialPositionForMap(mapId: String): Pair<Int, Int> {
            return when (mapId) {
                MAP_MAIN -> Pair(15, 15)  // Posición central en el mapa principal
                MAP_BUILDING2 -> Pair(20, 16)  // Centro del pasillo principal del edificio 2
                MAP_SALON2009 -> Pair(20, 20)  // Posición central dentro del salón 2009
                MAP_SALON2010 -> Pair(20, 20)  // Posición central dentro del salón 2010
                MAP_CAFETERIA -> Pair(2, 2)  // Posición central dentro de la escomCAFE
                MAP_BIBLIOTECA -> Pair(20, 35)  // Posición inicial en la biblioteca
                MAP_AUDITORIO -> AuditorioMap.getSpawnPoints().firstOrNull() ?: Pair(1, 1)
                MAP_HOUSE -> Pair(20, 20)  // Posición inicial en la casa
                else -> Pair(MAP_WIDTH / 2, MAP_HEIGHT / 2)
            }
        }
    }
}

/**
 * Gestor de matriz para un mapa específico
 */
class MapMatrix(private val mapId: String) {
    private val matrix: Array<Array<Int>> = MapMatrixProvider.getMatrixForMap(mapId)

    private val paints = mapOf(
        MapMatrixProvider.INTERACTIVE to Paint().apply {
            color = Color.rgb(0, 255, 255)  // Cian brillante para puntos interactivos
            alpha = 200
        },
        MapMatrixProvider.WALL to Paint().apply {
            color = Color.rgb(139, 69, 19)  // Marrón (simular madera) para paredes
        },
        MapMatrixProvider.PATH to Paint().apply {
            color = Color.rgb(220, 220, 255)  // Gris azulado claro para caminos
        },
        MapMatrixProvider.INACCESSIBLE to Paint().apply {
            color = Color.rgb(178, 34, 34)  // Rojo ladrillo para objetos inamovibles
            alpha = 180
        }
    )

    fun getValueAt(x: Int, y: Int): Int {
        return if (x in 0 until MapMatrixProvider.MAP_WIDTH && y in 0 until MapMatrixProvider.MAP_HEIGHT) {
            matrix[y][x]
        } else {
            -1
        }
    }

    fun isValidPosition(x: Int, y: Int): Boolean {
        if (mapId == MapMatrixProvider.MAP_AUDITORIO) {
            return x in 0 until MapMatrixProvider.MAP_WIDTH && 
                   y in 0 until MapMatrixProvider.MAP_HEIGHT
        }
        
        // Para otros mapas, mantener la lógica original
        return x in 0 until MapMatrixProvider.MAP_WIDTH &&
               y in 0 until MapMatrixProvider.MAP_HEIGHT &&
               matrix[y][x] != MapMatrixProvider.WALL &&
               matrix[y][x] != MapMatrixProvider.INACCESSIBLE
    }

    fun isInteractivePosition(x: Int, y: Int): Boolean {
        return x in 0 until MapMatrixProvider.MAP_WIDTH &&
                y in 0 until MapMatrixProvider.MAP_HEIGHT &&
                matrix[y][x] == MapMatrixProvider.INTERACTIVE
    }

    fun isMapTransitionPoint(x: Int, y: Int): String? {
        return MapMatrixProvider.isMapTransitionPoint(mapId, x, y)
    }

    fun drawMatrix(canvas: Canvas, width: Float, height: Float) {
        val matrixHeight = matrix.size
        val matrixWidth = if (matrixHeight > 0) matrix[0].size else 0
        
        val cellWidth = width / matrixWidth
        val cellHeight = height / matrixHeight

        for (y in 0 until matrixHeight) {
            for (x in 0 until matrixWidth) {
                try {
                    val paint = paints[matrix[y][x]] ?: paints[MapMatrixProvider.PATH]!!
                    canvas.drawRect(
                        x * cellWidth,    // left
                        y * cellHeight,   // top
                        (x + 1) * cellWidth,  // right
                        (y + 1) * cellHeight, // bottom
                        paint
                    )
                } catch (e: Exception) {
                    Log.e("MapMatrix", "Error drawing cell at ($x, $y)", e)
                }
            }
        }
    }
}
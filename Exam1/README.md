# Examen 1: Sistema Binario

# Documento de Decisiones de Diseño e Implementación

## Introducción
El presente documento describe las decisiones de diseño e implementación tomadas durante el desarrollo de la aplicación móvil nativa "Sistema Binario". Esta aplicación está orientada a enseñar a niños de primaria sobre la representación binaria de la información a través de una herramienta educativa interactiva y divertida.

## Requerimientos Funcionales
Para desarrollar la aplicación de "Sistema Binario" de manera estructurada, se definieron los siguientes requerimientos funcionales:

1. Diseño de Arquitectura
2. Desarrollo del Módulo Educativo
3. Implementación del Juego de Interruptores
4. Modo Práctica y Desafíos
5. Funciones de Configuración
6. Pruebas y Optimización

## Decisiones de Diseño

### 1. Diseño de Arquitectura
#### Decisiones:
- **Implementación de 3 Activities principales**: Se decidió estructurar la aplicación en tres actividades principales: `MainActivity` para la pantalla de inicio, `EducationalActivity` para la sección educativa y `GamesActivity` para la sección de juegos. Esto permite una clara separación de responsabilidades y facilita la navegación entre las distintas partes de la aplicación.
- **Creación de Fragments**: Utilizamos `Fragments` para modularizar el contenido de cada sección, permitiendo una mayor flexibilidad y reusabilidad de componentes. Por ejemplo, el `BinaryBasicsFragment` y el `BinaryVisualizationFragment` se utilizan dentro de `EducationalActivity`.
- **Navegación entre pantallas**: Se implementó una navegación fluida entre las pantallas utilizando `Intents` y `FragmentTransactions`, asegurando una experiencia de usuario intuitiva.

### 2. Desarrollo del Módulo Educativo
#### Decisiones:
- **Interfaz de usuario**: Se diseñó una interfaz amigable y visualmente atractiva que incluye texto, imágenes y gráficos para explicar los conceptos básicos del sistema binario.
- **Visualizaciones y animaciones**: Se incorporaron visualizaciones y animaciones interactivas que ayudan a los niños a entender cómo funciona la conversión de binario a decimal y viceversa. Se utilizó la biblioteca `Lottie` para crear animaciones atractivas y pedagogicamente efectivas.
- **Ejemplos interactivos**: Se implementaron ejemplos prácticos y ejercicios interactivos dentro del `BinaryBasicsFragment` y el `BinaryVisualizationFragment`, permitiendo a los usuarios introducir números y ver su representación binaria en tiempo real.

### 3. Implementación del Juego de Interruptores
#### Decisiones:
- **Interfaz visual de interruptores**: Se diseñó una interfaz de usuario que simula interruptores (0/1) para que los niños puedan interactuar con ellos y entender la conversión binario-decimal.
- **Lógica de conversión en tiempo real**: Se implementó la lógica de conversión binario-decimal en tiempo real utilizando `ViewModel` y `LiveData` para mantener la UI reactiva y actualizada.
- **Indicadores visuales**: Se añadieron indicadores visuales que resaltan los valores de posición de cada bit, facilitando la comprensión del sistema binario.

### 4. Modo Práctica y Desafíos
#### Decisiones:
- **Ejercicios de conversión**: Se desarrollaron ejercicios que desafían a los niños a convertir números decimales a binarios y viceversa, proporcionando retroalimentación inmediata y puntuación.
- **Sistema de retroalimentación y puntuación**: Se implementó un sistema de retroalimentación que indica si la respuesta es correcta o incorrecta, y un sistema de puntuación que motiva a los niños a mejorar.
- **Niveles de dificultad**: Se añadieron niveles de dificultad progresivos que aumentan la complejidad de los ejercicios a medida que el usuario avanza.

### 5. Funciones de Configuración
#### Decisiones:
- **Selector de temas**: Se implementó un selector de temas que permite a los usuarios elegir entre dos temas distintos: Tema Guinda (representativo del IPN) y Tema Azul (representativo de la ESCOM). Esto se realizó mediante el uso de `SharedPreferences` para guardar la preferencia del usuario y aplicar el tema seleccionado en toda la aplicación.

### 6. Pruebas y Optimización
#### Decisiones:
- **Pruebas en distintos dispositivos**: Se realizaron pruebas exhaustivas en diferentes dispositivos y tamaños de pantalla para asegurar la correcta funcionalidad y apariencia de la aplicación.
- **Ajuste de interfaces**: Se ajustaron las interfaces de usuario para que se adapten correctamente a diferentes resoluciones y tamaños de pantalla, utilizando `ConstraintLayout` y recursos específicos para cada tipo de pantalla.

## Conclusión
La aplicación "Sistema Binario" es una herramienta educativa innovadora que combina elementos visuales, interactivos y de juego para enseñar a los niños sobre el sistema binario. Las decisiones de diseño e implementación se centraron en crear una experiencia de usuario intuitiva, atractiva y pedagógicamente efectiva.

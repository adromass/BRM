# BRM – Buscador de Restaurantes
### Exploración gastronómica inteligente con Material 3

BRM es una aplicación nativa para Android que transforma la manera en que los usuarios descubren establecimientos gastronómicos. Basada en una arquitectura moderna y reactiva, la app ofrece resultados precisos mediante geolocalización en tiempo real o búsquedas específicas en cualquier ciudad del mundo.

## Evolución del Diseño (Figma vs. Producto Final)

El proyecto partió de una base conceptual en Figma; sin embargo, durante el desarrollo se realizó un pivot estratégico en la interfaz de usuario. 

Se decidió evolucionar el diseño original para adoptar los estándares de Material 3 (Google Moderno), permitiendo:
*   Mayor Funcionalidad: Integración de una fila de acciones rápidas (Llamar, Web, Compartir, Navegación) que no estaban contempladas inicialmente.
*   Interfaz Inmersiva: Transición de un diseño estático a uno dinámico con carruseles de imágenes edge-to-edge y mapas integrados sin bordes.
*   Coherencia Visual: Implementación de un sistema de formas con bordes redondeados de 28dp, optimizando la jerarquía visual y la facilidad de uso.

Nota: El diseño final prioriza la "regla de un solo vistazo", reduciendo el scroll y permitiendo al usuario tomar decisiones en segundos.

## Funcionalidades Principales

*   Búsqueda Geográfica Precisa: Uso de Google Places Autocomplete para centrar búsquedas mediante coordenadas exactas (LatLng).
*   Modo "Cerca de mí": Detección instantánea de restaurantes cercanos vía GPS.
*   Filtrado Inteligente:
    *   Rango de distancia ajustable (5km - 20km).
    *   Nivel de presupuesto sugerido ($-$$$$).
    *   Más de 15 categorías gastronómicas (Desde Pizzería hasta Brunch o Comida Vegana).
*   Ficha de Detalles Optimizada:
    *   Carrusel visual de fotos del establecimiento.
    *   Mapa estático interactivo mediante Google Static Maps.
    *   Acceso directo a navegación GPS paso a paso en Google Maps.

## Tecnologías Utilizadas

*   UI: Jetpack Compose (100% Declarativo).
*   Lenguaje: Kotlin 2.0+.
*   APIs: Google Places SDK, Google Maps SDK, Google Static Maps API.
*   Imágenes: Coil (Carga asíncrona y caché).
*   Gestión de Dependencias: Kotlin DSL (Gradle).

## Optimización del Proyecto

Para garantizar un rendimiento óptimo, se eliminaron todos los componentes obsoletos de la arquitectura antigua:
*   Remoción de Fragments y Navigation XML.
*   Desactivación de ViewBinding para reducir tiempos de compilación.
*   Código limpio y libre de comentarios redundantes, siguiendo las mejores prácticas de desarrollo Android actual.

---

### Instalación

1. Clona este repositorio.
2. Define tu MAPS_API_KEY en el archivo local.properties.
3. Compila usando Android Studio Ladybug o superior.

---

Enlace original a Figma: [Ver Wireframes Iniciales](https://www.figma.com/community/file/1594910947161950850)

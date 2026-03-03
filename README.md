# BRM – Buscador de Restaurantes

## Descripción del proyecto
BRM – Buscador de Restaurantes es una aplicación móvil desarrollada para la plataforma Android cuyo objetivo es facilitar a los usuarios la búsqueda de restaurantes cercanos según su ubicación actual o mediante la consulta de una ubicación específica. La aplicación se inspira en el enfoque de plataformas como Expedia, pero enfocada exclusivamente en la exploración de establecimientos gastronómicos.

La aplicación consume servicios de terceros mediante APIs públicas para obtener información actualizada sobre restaurantes, tales como nombre, dirección, calificación, tipo de comida, nivel de precios estimado y datos de contacto. Como valor agregado, la app permite filtrar los resultados por tipo de comida y calificación, además de ofrecer accesos directos para realizar llamadas telefónicas o iniciar conversaciones vía WhatsApp con los establecimientos disponibles.

---

## Exposición del problema
Actualmente, los usuarios que desean encontrar restaurantes cercanos o en una ubicación específica suelen enfrentarse a información dispersa o a aplicaciones con múltiples funciones que no están enfocadas exclusivamente en la experiencia gastronómica. Esto dificulta la toma de decisiones rápidas, especialmente cuando se busca un tipo de comida específico o se desea contactar directamente con el establecimiento.

La falta de una aplicación simple, enfocada y optimizada para la búsqueda de restaurantes genera una experiencia poco eficiente para el usuario. BRM busca resolver este problema ofreciendo una solución centralizada, clara y orientada a la funcionalidad.

---

## Plataforma
La aplicación será desarrollada exclusivamente para dispositivos Android, utilizando Android Studio como entorno de desarrollo integrado y Kotlin como lenguaje de programación principal. La app hace uso de servicios de localización del dispositivo y APIs REST para el consumo de datos de terceros.

---

## Interfaz de usuario e interfaz de administrador

### Interfaz de usuario
La interfaz de usuario está diseñada para ser clara, intuitiva y funcional. La aplicación incluye:
- Pantalla de inicio con detección de ubicación actual.
- Búsqueda de restaurantes por ubicación ingresada por el usuario.
- Listado de restaurantes en formato de lista con imágenes.
- Pantalla de detalle del restaurante con información relevante y opciones de contacto.

### Interfaz de administrador
La aplicación no cuenta con una interfaz de administrador propia, ya que la gestión y actualización de la información es responsabilidad del servicio externo utilizado como backend.

---

## Funcionalidad
Las principales funcionalidades de la aplicación son:
- Detección de la ubicación actual del usuario.
- Búsqueda de restaurantes por ubicación específica.
- Listado de restaurantes con imágenes, nombre y calificación.
- Filtrado por tipo de comida y calificación mínima.
- Visualización detallada de la información del restaurante.
- Accesos directos para llamadas telefónicas y WhatsApp.
- Integración con aplicaciones de mapas para navegación.
- Consumo dinámico de datos mediante APIs externas.

---

## Diseño (Wireframes)

El diseño de la aplicación se apoya en wireframes elaborados en Figma, los cuales representan la estructura general y el flujo de navegación de las pantallas principales: pantalla de inicio y búsqueda, listado de restaurantes y detalle del restaurante.

Enlace a Figma: https://www.figma.com/community/file/1594910947161950850

---

## Tecnologías utilizadas
- Android Studio
- Kotlin
- Google Places API
- Google Maps SDK
- APIs REST

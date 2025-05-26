# Log de cambios del proyecto
Todos los cambios representativos del proyecto serán documentados en este archivo. Este formato está basado en "Keep a Changelog", y este proyecto se adhiere al uso de versionamiento semántico.

## 1.0.0 - 2025-05-25

### Agregado

Gestión completa de recetas: Permite añadir, visualizar, editar y eliminar recetas.
Funcionalidad de Favoritos: Marcar y desmarcar recetas como favoritas, con almacenamiento persistente por usuario.
Gestión de Usuario: Registro, inicio de sesión y cierre de sesión de usuarios con Firebase Authentication.
Navegación principal de la aplicación (Navigation Drawer) con Home, Favoritos, Perfil y Agregar Receta.
Integración de Firebase Storage para el almacenamiento de imágenes de recetas.
Módulo de perfil de usuario básico con información editable (nombre, email, descripción).
Pantalla de detalle de receta.

### Cambiado

Refactorización mayor de Receta.kt para asegurar la correcta deserialización de datos desde Firestore, incluyendo imagenUri y categoria.
Ajuste en ListaRecetasPorCategoriaActivity.kt para el manejo consistente de la categoría pasada vía Intent y la normalización de cadenas.
Modificación de SessionManager.kt para incluir el cierre de sesión de Firebase Authentication (FirebaseAuth.getInstance().signOut()) al hacer logout, garantizando una desconexión completa.
Optimización de la verificación de sesión al inicio de la aplicación para una navegación fluida.
Proceso de registro simplificado: Eliminada la opción de seleccionar imagen de perfil durante el registro para una experiencia de usuario más rápida.

### Corregido

Bug crítico de deserialización de recetas donde la categoría y la URI de la imagen no se cargaban correctamente debido a un constructor secundario en Receta.kt.
Problemas de filtrado de recetas por categoría que no mostraban resultados debido a inconsistencias en la normalización de los nombres de categorías.
Error de redirección tras cierre de sesión, que no enviaba al usuario a la pantalla de inicio de sesión.
Advertencias menores de accesibilidad en elementos de UI (TextView con área táctil pequeña).

## 0.5.0 - 2025-04-15

### Agregado

Diseño y maquetación de la interfaz de usuario principal (MainActivity, HomeFragment).
Implementación de la navegación por categorías de recetas.
Funcionalidad de agregar nueva receta.
Configuración inicial de Firebase Firestore para almacenamiento de datos.

### Cambiado

Adaptación de la interfaz de usuario a los principios de Material Design.
0.1.0 - 2025-03-20

### Agregado

Proyecto inicial creado con Android Studio.
Configuración básica del entorno de desarrollo.
Integración inicial de Firebase Core.
Definición de modelos de datos clave (Receta, User).

### Cambiado

Formato en archivos de markup (README.md, CHANGELOG.md).

## 0.0.3 - 2025-03-10

### Agregado

Lista de funcionalidades iniciales de la aplicación.
Definición de paleta de colores y estilos.

### Corregido

Errores de ortografía en la documentación del proyecto.

## 0.0.2 - 2025-03-05

### Agregado

Mockups de diseño de UI.
Estructura inicial de paquetes.

### Corregido

Errores de ortografía y algunos links.

## 0.0.1 - 2025-03-01

### Agregado

Archivo readme README.md
Log de cambios changelog.md
Ideas de proyecto ideas.md

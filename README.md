# GastroGenius: Tu Compañero Culinario Inteligente

Este proyecto es una aplicación móvil para un blog y gestor de recetas de cocina. Permite a los usuarios explorar una amplia variedad de recetas, guardar sus favoritas y compartir sus propias creaciones culinarias con la comunidad.

## 📱 Aplicación Android Nativa

"GastroGenius" es una aplicación Android nativa desarrollada en Kotlin. Se enfoca en ofrecer una experiencia de usuario fluida e intuitiva para los amantes de la cocina.

## ✨ Funcionalidades Principales

1.  **Exploración de Recetas:**
    * Consulta una lista de recetas disponibles en la pantalla de inicio del blog culinario.
    * Visualización detallada de cada receta (nombre, descripción, ingredientes, pasos de preparación e imagen).
    * Funcionalidad de búsqueda para encontrar recetas específicas.

2.  **Administración de Recetas:**
    * **Crear Receta (Create):** Los usuarios autenticados pueden agregar nuevas recetas al blog mediante un formulario de creación.
    * **Leer Receta (Read):** Visualiza tus propias recetas y las de otros usuarios.
    * **Editar Receta (Update):** Los usuarios pueden modificar los detalles de sus propias recetas existentes.
    * **Eliminar Receta (Delete):** Los usuarios pueden eliminar las recetas que han creado.

3.  **Gestión de Favoritos:**
    * Guarda tus recetas preferidas en una sección dedicada para un acceso rápido y fácil.

4.  **Autenticación de Usuarios:**
    * **Iniciar Sesión (Login):** Los usuarios pueden acceder a la aplicación utilizando sus credenciales.
    * **Registrarse (Registro):** Los nuevos usuarios pueden crear una cuenta para empezar a interactuar con la plataforma.
    * **Cerrar Sesión:** Permite a los usuarios cerrar sus sesiones de forma segura.

5.  **Gestión de Perfil de Usuario:**
    * Próximamente: Opciones para visualizar y gestionar la información del perfil del usuario.

## ⚙️ Integración con Backend (Firebase Firestore)

Este proyecto de Frontend (la aplicación Android) se integra con **Firebase Firestore** como su base de datos no relacional para almacenar de manera eficiente y escalable los datos de las recetas y los usuarios.

## 📸 Logotipo de la Aplicación
(![image](https://github.com/user-attachments/assets/7ad84ab4-6680-4111-b127-1e6a722f3a39)

## 🔗 Links de Referencia e Inspiración

* [Yummly](http://kitchenaid.com/recipes.html)
* [Tasty](https://tasty.co/latest)
* [Cookpad](https://cookpad.com/es/homepage)

## 🛠️ Tecnologías Utilizadas

* **Lenguaje:** Kotlin
* **Framework:** Android SDK
* **Base de Datos:** Firebase Firestore
* **Autenticación:** Firebase Authentication
* **Manejo de Datos Asíncronos:** Coroutines (Kotlinx Coroutines)
* **Patrón de Arquitectura:** MVVM (Model-View-ViewModel)
* **Navegación:** Android Jetpack Navigation Component
* **UI/UX:** Material Design Components
* **Librerías Adicionales (ejemplos):**
    * `androidx.recyclerview`
    * `androidx.cardview`
    * `com.google.android.material`
    * `androidx.lifecycle`
    * `androidx.navigation`
    * `androidx.appcompat`
    * `androidx.constraintlayout`
    * `com.google.firebase` (firestore, auth)
    * `kotlinx.coroutines`
    * `androidx.core:core-splashscreen` (para Splash Screen)
    * `androidx.activity:activity-ktx` y `androidx.fragment:fragment-ktx` (para ViewModels)

## 📝 Requisitos Previos

* Android Studio instalado en tu sistema. Puedes descargarlo [aquí](https://developer.android.com/studio).
* Java Development Kit (JDK) instalado y configurado.
* Una cuenta de Firebase y un proyecto de Firebase configurado para esta aplicación (con Firestore y Authentication habilitados).

## 🚀 Pasos para Clonar y Ejecutar el Proyecto Localmente

1.  **Clona este repositorio:**
    ```bash
    git clone <URL_DE_TU_REPOSITORIO_GASTROGENIUS>
    ```
2.  **Navega al directorio del proyecto:**
    ```bash
    cd GastroGenius
    ```
3.  **Abre el proyecto en Android Studio:**
    * En Android Studio, selecciona `File > Open` y elige la carpeta `GastroGenius` que acabas de clonar.
4.  **Configura Firebase:**
    * Sigue las instrucciones oficiales de Firebase para [agregar Firebase a tu proyecto Android](https://firebase.google.com/docs/android/setup).
    * Descarga el archivo `google-services.json` de tu proyecto de Firebase y colócalo en el directorio `app/` de tu proyecto Android.
    * Asegúrate de que las dependencias de Firebase estén correctamente sincronizadas en tu `build.gradle`.
5.  **Sincroniza el proyecto:**
    * Android Studio debería pedirte que sincronices el proyecto con los archivos Gradle. Si no, ve a `File > Sync Project with Gradle Files`.
6.  **Ejecuta la aplicación:**
    * Conecta un dispositivo Android o inicia un emulador.
    * Haz clic en el botón `Run 'app'` (el ícono de triángulo verde) en la barra de herramientas de Android Studio.

## 👨‍💻 Autor

* **Jeison Steven Niño Rojas**

---

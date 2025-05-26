# Funcionalidades de la aplicación

## Restricciones técnicas

1. Esta aplicación está diseñada para funcionar en dispositivos Android con sistema operativo versión 8.0 (Oreo) o superior.
2. La información de las recetas y los perfiles de usuario son de uso exclusivo de la aplicación y se gestionan a través de los servicios de Google Firebase (Firestore, Authentication, Storage).
3. La aplicación requiere una conexión a internet activa para el registro, inicio de sesión, carga y guardado de recetas e imágenes.
4. Cualquier dato de sesión local (como nombre de usuario, email, etc.) se almacena de forma segura en las preferencias compartidas (SharedPreferences) del dispositivo.

## Funcionalidades de usuario

GastroGenius le permitirá al usuario gestionar, organizar y descubrir recetas de forma intuitiva a través de una interfaz amigable. El usuario podrá:

1. Registrarse y Acceder a su Cuenta: Crear una cuenta nueva usando correo electrónico y contraseña, o iniciar sesión si ya tiene una.
2. Cerrar Sesión de Forma Segura: Desconectarse de su cuenta, limpiando la sesión local y de Firebase Authentication.
3. Ver una Lista de Recetas Disponibles: Al ingresar a la aplicación, accederá a una pantalla principal que organiza las recetas por categorías.
4. Navegar por Categorías de Recetas: Seleccionar una categoría específica (ej., Desayuno, Almuerzo, Cena, Postre, Bebida, Vegetariana, etc.) para ver las recetas asociadas.
5. Ver el Detalle de una Receta: Seleccionar cualquier receta de una lista para ver su nombre, descripción, lista de ingredientes e instrucciones paso a paso.
6. Crear y Guardar Nuevas Recetas: Añadir sus propias recetas personalizadas a la aplicación, incluyendo nombre, descripción, ingredientes, pasos y una imagen.
7. Editar sus Recetas Existentes: Modificar los detalles de cualquier receta que haya creado previamente.
8. Eliminar sus Recetas: Borrar recetas que ya no desee mantener en su colección personal.
9. Marcar y Desmarcar Recetas como Favoritas: Añadir o quitar recetas de su lista de favoritos para acceder a ellas rápidamente.
10. Ver sus Recetas Favoritas: Acceder a una sección dedicada que muestra todas las recetas que ha marcado como favoritas.
11. Consultar su Perfil de Usuario: Ver su nombre completo, correo electrónico y una descripción personal, que puede ser modificada.
12. Buscar Recetas por Nombre: Utilizar una barra de búsqueda para encontrar rápidamente recetas específicas dentro de las categorías o en la lista general.

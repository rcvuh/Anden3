# Andén · avance T4.7

Proyecto Android Studio (Kotlin, Jetpack Compose, Material 3, MVVM, Room, Firebase, CameraX).

## Generar el APK
- **Android Studio:** *Build › Build App Bundle(s) / APK(s) › Build APK(s)*. El archivo queda en
  `app/build/outputs/apk/debug/app-debug.apk`.
- **Sin computadora con Android Studio:** sube la carpeta a un repositorio de GitHub y en la
  pestaña *Actions* ejecuta **Construir APK de Andén**; descarga el artefacto `Anden-debug-apk`.

## Pruebas unitarias (nuevo en T4.7)
`app/src/test/java/mx/uaemex/anden/domain/AndenRulesTest.kt` — 14 pruebas JUnit sobre
`domain/AndenRules.kt` (geocerca de 300 m, caducidad de 30 min, filtros, Haversine).
En Android Studio: clic derecho en `app/src/test` › *Run Tests*, o `./gradlew testDebugUnitTest`.

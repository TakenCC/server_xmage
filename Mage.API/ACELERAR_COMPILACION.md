# Por qué la compilación tarda tanto y cómo acelerarla

## ¿Por qué tarda tanto?

La primera vez que compilas el proyecto MAGE, Maven debe:

1. **Descargar dependencias** (10-20 minutos la primera vez)
   - Descarga cientos de librerías (Jersey, JWT, H2, ORM, etc.)
   - El proyecto tiene ~40 módulos Maven
   - Todas las dependencias se guardan en: `C:\Users\tu-usuario\.m2\repository\`

2. **Compilar código** (5-10 minutos)
   - Compila miles de archivos Java
   - Genera clases compiladas (.class)
   - Empaqueta en JARs

3. **Compilar tests** (si no usas `-DskipTests`)

**Total primera vez**: 15-30 minutos  
**Compilaciones siguientes**: 2-5 minutos (si no cambias dependencias)

---

## Cómo acelerar la compilación

### 1. Compilar solo el módulo que necesitas (más rápido)

Si solo cambiaste código en `Mage.API`, puedes compilar solo ese módulo:

```powershell
cd "C:\Users\taken\Motor MTG\server_xmage\Mage.API"
mvn clean install -DskipTests -pl . -am
```

Esto compila solo `Mage.API` y sus dependencias directas.

### 2. Compilar sin ejecutar tests (ya lo haces)

Ya estás usando `-DskipTests`, que ahorra tiempo. Mantén esto.

### 3. Compilar sin ejecutar tests de integración

```powershell
mvn clean install -DskipTests -DskipITs
```

### 4. Compilar en paralelo (Maven 3.x)

Maven puede compilar múltiples módulos en paralelo:

```powershell
mvn clean install -DskipTests -T 4
```

El `-T 4` usa 4 hilos. Puedes aumentar según tu CPU (ej: `-T 8` si tienes 8+ núcleos).

### 5. Compilar solo módulos modificados

Si solo cambiaste archivos específicos:

```powershell
# Compilar solo Mage.API y sus dependencias directas
mvn clean install -DskipTests -pl Mage.API -am

# O desde el módulo directamente
cd Mage.API
mvn clean install -DskipTests
```

### 6. Usar Maven Offline (después de primera descarga)

Si ya tienes todas las dependencias, puedes evitar verificar actualizaciones:

```powershell
mvn clean install -DskipTests -o
```

**⚠️ No uses `-o` la primera vez**, solo después de que todo esté descargado.

---

## Script optimizado para compilación rápida

He creado un script que compila más rápido usando paralelismo:

```powershell
# Usa 4 hilos, omite tests, compila solo si hay cambios
mvn clean install -DskipTests -T 4 -DskipITs
```

---

## Ver el progreso de la compilación

Si quieres ver qué está haciendo Maven:

```powershell
# Sin redirigir salida (verás todo el proceso)
mvn clean install -DskipTests
```

Esto te mostrará:
- Qué dependencias está descargando
- Qué módulos está compilando
- Errores si los hay

---

## Acelerar solo la ejecución (sin recompilar)

Si solo quieres ejecutar el servidor y ya está compilado:

```powershell
cd Mage.API
mvn exec:java
```

Esto **NO** compila, solo ejecuta lo que ya está compilado.

---

## Consejos

### Primera compilación
- **Déjala correr**: La primera vez tarda, pero las siguientes serán más rápidas
- **Ten paciencia**: Maven descarga muchas librerías

### Compilaciones siguientes
- **Solo recompila si cambiaste código**: Si solo ejecutas el servidor, no necesitas recompilar
- **Usa `-T 4`**: Compilación en paralelo ahorra tiempo
- **Compila solo módulos modificados**: Usa `-pl` para compilar solo lo necesario

### Después de cambios pequeños
Si solo cambiaste un archivo Java en `Mage.API`:

```powershell
cd Mage.API
mvn compile
mvn exec:java
```

Esto compila solo el módulo (sin instalar en repositorio local) y ejecuta.

---

## Tiempos estimados

| Acción | Primera vez | Siguientes veces |
|--------|-------------|------------------|
| Descargar dependencias | 10-20 min | 0 min (cache) |
| Compilar todo | 5-10 min | 2-5 min |
| Compilar solo Mage.API | 1-2 min | 30 seg - 1 min |
| Ejecutar (sin compilar) | - | 10-30 seg |

---

## Verificar si ya está compilado

Para ver si necesitas recompilar:

```powershell
# Ver si existe el JAR compilado
Test-Path "target\mage-api.jar"
```

Si existe, puedes ejecutar directamente sin recompilar (a menos que hayas hecho cambios).


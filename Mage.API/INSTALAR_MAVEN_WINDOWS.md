# Cómo Instalar Maven en Windows

Esta guía te ayudará a instalar Apache Maven en Windows paso a paso.

## Opción 1: Instalación Manual (Recomendada)

### Paso 1: Verificar que Java está Instalado

Maven requiere Java, así que primero verifica que lo tengas:

1. Abre PowerShell o CMD
2. Escribe:
   ```powershell
   java -version
   ```
3. Deberías ver algo como:
   ```
   java version "1.8.0_xxx"
   ```
   
   Si ves un error como "java no se reconoce como comando", necesitas instalar Java primero desde: https://adoptium.net/

### Paso 2: Descargar Maven

1. Ve a la página de descargas de Maven: https://maven.apache.org/download.cgi
2. Busca la sección "Files" y descarga el archivo **Binary zip archive**
   - Busca `apache-maven-3.9.x-bin.zip` (la versión más reciente)
   - Ejemplo: `apache-maven-3.9.6-bin.zip`

### Paso 3: Extraer Maven

1. Crea una carpeta para Maven (por ejemplo: `C:\Program Files\Apache\maven`)
2. Extrae el contenido del ZIP que descargaste en esa carpeta
3. Deberías tener una estructura como:
   ```
   C:\Program Files\Apache\maven\apache-maven-3.9.6\
   ├── bin\
   ├── boot\
   ├── conf\
   └── lib\
   ```

### Paso 4: Agregar Maven al PATH

**Método 1: Usando la interfaz gráfica (más fácil)**

1. Presiona `Windows + R`
2. Escribe: `sysdm.cpl` y presiona Enter
3. Ve a la pestaña **"Opciones avanzadas"**
4. Clic en el botón **"Variables de entorno"**
5. En la sección **"Variables del sistema"** (la parte de abajo), busca la variable `Path`
6. Selecciona `Path` y haz clic en **"Editar"**
7. Clic en **"Nuevo"**
8. Agrega la ruta completa a la carpeta `bin` de Maven:
   ```
   C:\Program Files\Apache\maven\apache-maven-3.9.6\bin
   ```
   (Ajusta la ruta si la pusiste en otra ubicación)
9. Clic en **"Aceptar"** en todos los cuadros de diálogo

**Método 2: Usando PowerShell (para usuarios avanzados)**

Abre PowerShell como Administrador y ejecuta:

```powershell
[Environment]::SetEnvironmentVariable("Path", $env:Path + ";C:\Program Files\Apache\maven\apache-maven-3.9.6\bin", "Machine")
```

(Ajusta la ruta según donde hayas extraído Maven)

### Paso 5: Verificar la Instalación

1. **IMPORTANTE**: Cierra todas las ventanas de PowerShell/CMD que tengas abiertas
2. Abre una **NUEVA** ventana de PowerShell o CMD
3. Escribe:
   ```powershell
   mvn -version
   ```
4. Deberías ver algo como:
   ```
   Apache Maven 3.9.6
   Maven home: C:\Program Files\Apache\maven\apache-maven-3.9.6
   Java version: 1.8.0_xxx
   ...
   ```

¡Listo! Maven está instalado correctamente.

---

## Opción 2: Usando Chocolatey (Más Rápido)

Si ya tienes Chocolatey instalado, puedes instalar Maven con un solo comando:

1. Abre PowerShell como **Administrador**
2. Ejecuta:
   ```powershell
   choco install maven
   ```
3. Verifica con: `mvn -version`

**Nota**: Si no tienes Chocolatey, puedes instalarlo desde: https://chocolatey.org/install

---

## Opción 3: Usando Scoop

Si usas Scoop como gestor de paquetes:

1. Abre PowerShell
2. Ejecuta:
   ```powershell
   scoop install maven
   ```
3. Verifica con: `mvn -version`

---

## Solución de Problemas

### Error: "mvn no se reconoce como comando"

**Causa**: El PATH no se actualizó correctamente o no cerraste y reabriste la terminal.

**Solución**:
1. Cierra TODAS las ventanas de PowerShell/CMD
2. Abre una nueva ventana
3. Si aún no funciona:
   - Verifica que la ruta a `bin` esté correcta en las Variables de Entorno
   - Asegúrate de que la carpeta `bin` existe y contiene `mvn.cmd`

### Error: "Java no se reconoce como comando"

**Solución**: Instala Java JDK primero:
- Descarga desde: https://adoptium.net/
- Instala Java 8 o superior
- Verifica con `java -version`

### Error: "JAVA_HOME no está definido"

Maven necesita saber dónde está Java. Si tienes problemas:

1. En Variables de Entorno, crea una nueva variable de sistema:
   - Nombre: `JAVA_HOME`
   - Valor: La ruta donde está Java (ej: `C:\Program Files\Java\jdk1.8.0_xxx`)
2. Asegúrate de que el PATH incluya `%JAVA_HOME%\bin`

### Verificar JAVA_HOME

Para verificar si JAVA_HOME está configurado:

```powershell
echo $env:JAVA_HOME
```

Si no muestra nada, necesitas configurarlo (ver arriba).

---

## Comandos Útiles de Maven

Una vez instalado, estos son los comandos más comunes:

```powershell
mvn -version          # Ver la versión de Maven
mvn clean             # Limpiar archivos compilados
mvn compile           # Compilar el proyecto
mvn test              # Ejecutar pruebas
mvn package           # Crear el JAR
mvn install           # Instalar en el repositorio local
mvn clean install     # Limpiar, compilar e instalar
```

---

## Siguiente Paso

Una vez que Maven esté instalado, puedes continuar con:

1. Compilar el proyecto MAGE:
   ```powershell
   cd "C:\Users\taken\Motor MTG\server_xmage"
   mvn clean install -DskipTests
   ```

2. Ejecutar el servidor:
   ```powershell
   cd Mage.API
   mvn exec:java
   ```

¡Buena suerte! 🚀


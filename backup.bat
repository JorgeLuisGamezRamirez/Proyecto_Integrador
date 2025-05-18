@echo off
echo Creando respaldo del Sistema de Punto de Venta...

:: Crear carpeta de respaldo con fecha
set backupName=Backup_POS_%date:~-4,4%%date:~-7,2%%date:~-10,2%
md "%backupName%"

:: Copiar archivos y carpetas esenciales
echo Copiando codigo fuente...
xcopy "src" "%backupName%\src\" /E /I /H

echo Copiando librerias...
xcopy "lib" "%backupName%\lib\" /E /I /H

echo Copiando imagenes y recursos...
xcopy "Img" "%backupName%\Img\" /E /I /H

echo Copiando archivos de configuracion...
copy ".classpath" "%backupName%\"
copy "pom.xml" "%backupName%\" 2>nul

echo Respaldo creado en la carpeta: %backupName%
echo.
echo IMPORTANTE: Al restaurar el respaldo:
echo 1. Asegurate de tener instalado JDK 11 o superior
echo 2. Configura las librerias en tu IDE
echo 3. Verifica la conexion a la base de datos
pause

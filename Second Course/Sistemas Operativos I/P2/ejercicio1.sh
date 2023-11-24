#!/bin/bash
#Comprobamos inicialmente que el número de argumentos és correcto.
#En caso contrario, el programa termina con un código de error. 
if [ $# -ne 2 ]; then
	echo "ERROR: Número incorrecto de argumentos"
	echo "Argumentos esperados: 2    ./ejercicio1.sh <directorio><extension>"
	exit 1
fi

#Comprobamos ahora que el directorio pasado por argumento existe.
#En caso contrario, el programa termina con un código de error.
if [ ! -d "$1" ]; then
	echo "ERROR: El directorio no existe"
	exit 1
fi

#Buscamos con find todos los archivos en el directorio pasado y sus subdirectorios y nos quedamos
#con su información detallada con ls. Luego los ordenamos numéricamente por tamaño de mayor a menor
#con sort, nos quedamos con los 5 primeros con head y listamos sus nombres por pantalla con awk.
find "$1" -type f -name "*.$2" -exec ls -l {} \; | sort -rnk5 | head -5 | awk '{print $9}'

#Salimos del programa.
exit 0
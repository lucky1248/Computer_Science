#!/bin/bash

#Comprobamos inicialmente que el número de argumentos és correcto.
#En caso contrario, el programa termina con un código de error. 
if [ $# -ne 1 ]; then
  echo "ERROR: Número incorrecto de argumentos"
  echo "Argumentos esperados: 1    ./ejercicio2.sh <directorio>"
  exit 1
fi

#Comprobamos ahora que el directorio pasado por argumento existe.
#En caso contrario, el programa termina con un código de error.
if [ ! -d "$1" ]; then
  echo "ERROR: El directorio no existe"
  exit 1
fi


#Buscamos todos los subdirectorios del directorio pasado por argumento, ordenamos alfabeticamente
#con el comando sort (para que tenga una salida similar a la esperada) y los recorremos.
for subdirectorio in $(find "$1" -type d | sort); do
  #Se establece una variable total en 0
  total=0
  #Para cada archivo dentro del subdirectorio...
  for archivo in "$subdirectorio"/*; do
    #...si realmente se trata de un fichero...
    if [ -f "$archivo" ]; then
      #Usamos wc -c para contar el numero de bytes y se añade en total, que se usa 
      #como acumulador que va sumando el tamaño de todos los ficheros dentro del subdirectorio.
      total=$((total + $(wc -c < "$archivo")))
    fi
  done
  #Mostramos por pantalla el total del tamaño en bytes y el nombre del subdirectorio.
  echo "$total $subdirectorio"
done

#Salimos del programa.
exit 0
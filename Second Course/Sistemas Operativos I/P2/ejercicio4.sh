#!/bin/bash
#Comprobamos inicialmente que el número de argumentos és correcto.
#En caso contrario, el programa termina con un código de error. 
if [ $# -ne 2 ]; then
	echo "ERROR: Número incorrecto de argumentos"
	echo "Argumentos esperados: 2    ./ejercicio4.sh <directorio original><directorio copia>"
	exit 1
fi

#Comprobamos ahora que los directorios pasados por argumento existen.
#En caso contrario, el programa termina con un código de error.
if [ ! -d "$1" ] || [ ! -d "$2" ]; then
  echo "ERROR: Algún directorio no existe"
  exit 1
fi

#Inicializamos una variable auxiliar a 0 que nos indica que no hemos encontrado ningún archivo
#que esté en el primer directorio y no en el segundo.
trobat=0

#Con el find, seleccionamos todos los archivos en el primer directorio y los recorremos.
for archivo in $(find "$1" -type f); do
	#Para cada uno de ellos, mediante instrucciones de modificación de cadenas que aprendimos
	#en la anterior práctica, cambiamos la ruta, cambiando el directorio y dejando el nombre
	#del archivo cómo tal.
	#(Otra manera podria haver sido también usando el comando sed.)
	#Si el fichero no existe, es decir, no se encuentra en el segundo directorio, lo mostramos por pantalla.
	#Además, ponemos nuestra variable auxialar a 1.
	if [ ! -f "$2${archivo#*"$1"}" ]; then
		echo $archivo
		trobat=1
	fi
done

#Si no se ha encontrado ningún archivo, lo mostramos por pantalla.
if [ $trobat -eq 0 ];then
	echo "El segundo directorio tiene todos los archivos del primero."
fi

#Salimos del programa.
exit 0

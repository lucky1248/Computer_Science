#!/bin/bash
#Comprobamos inicialmente que el número de argumentos és correcto.
#Si no se proporciona ningún archivo, el programa termina con un código de error. 
if [ $# -ne 1 ]; then
	echo "ERROR: Número incorrecto de argumentos"
	echo "Argumentos esperados: 1    ./ejercicio.sh <archivo>"
	exit 1
fi

#Comprobamos ahora que el archivo pasado por argumento existe y no es un directorio.
#En caso contrario, el programa termina con un código de error.
#Primero miramos si se trata de un archivo existente, y, después, para dar una salida correcta
#por pantalla, diferenciaremos entre si se trata de un directorio, o el archivo no existe.  
if [ ! -f "$1" ]; then
	if [ -d "$1" ]; then
		echo "ERROR: Se trata de un directorio"
	else
		echo "ERROR: El archivo no existe"
	fi
	exit 1
fi

#Guardamos el contenido del archivo en la variable possibles_arch.
#Como sabemos que en los .txt los possibles archivos o directorios se encuentran todos en la
#primera columna, podriamos usar también el comando: possibles_arch=$(awk '{print $1}' "$1").
possibles_arch=$(cat "$1")

#Inicializamos  las variables que usaremos como contadores.
arch_existentes=0
arch_no_existentes=0

#Recorremos todos los elemetos de possibles_arch.
for arch in $possibles_arch; do
	#Si el elemento no se trata ni de un archivo ni de un directorio, incrementamos en 1 el contador
	#encargado de contar los archivos que no existen.
	if [ ! -f $arch ] && [ ! -d $arch ]; then
		((arch_no_existentes++))		#Equivale a arch_no_existentes=$(($arch_no_existentes+1))
	#En caso contrario, incrementamos el otro contador.
	else
		((arch_existentes++))			#Equivale a arch_existentes=$(($arch_existentes+1))		

	fi
done

#Mostramos los resultados por pantalla y terminamos el programa.
echo "Existen: $arch_existentes"
echo "No existen: $arch_no_existentes"
exit 0

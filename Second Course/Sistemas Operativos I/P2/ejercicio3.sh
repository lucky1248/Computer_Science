#!/bin/bash
#Comprobamos inicialmente que el número de argumentos és correcto.
#En caso contrario, el programa termina con un código de error. 
if [ $# -ne 3 ]; then
	echo "ERROR: Número incorrecto de argumentos"
	echo "Argumentos esperados: 3    ./ejercicio3.sh <extension><cadena original><cadena reemplazo>"
	exit 1
fi

#Mostrar las veces que se muestra la cadena a reemplazar en cada fichero
#es imposible sin iterar ni poder usar otros comandos como sh.
#Entonces, buscamos todos los ficheros del directorio pasado por argumento y sus subdirectorios, ordenamos
#alfabeticamente con el comando sort (para que tenga una salida similar a la esperada) y los recorremos. 
for archivo in $(find . -name "*.$1" -type f | sort); do
    	#Para cada uno de ellos, mostramos por pantalla su nombre juntamente con el nombre de veces que aparece
	#la cadena. Para ello, con grep la listamos cada vez que se encuentra y con wc -l sumamos el número de líneas.
	echo "$archivo : $(grep -o "$2" $archivo | wc -l)"
	#Con el comando sed intercambiamos las cadenas deseadas
	sed -i "s/$2/$3/g" $archivo
done


#Si se quisiera hacer el cambio en una solo linea: 
#Volvemos a buscar todos los ficheros y ahora para cada uno con el comando sed
#intercambiamos las cadenas deseadas. 
#No lo hacemos así pues el programa tardaria bastante mas tiempo.
#find . -name "*.$1" -type f -exec sed -i "s/$2/$3/g" {} \;

#Salimos del programa.
exit 0
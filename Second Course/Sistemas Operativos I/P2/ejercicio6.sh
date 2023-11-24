#!/bin/bash
#Comprobamos inicialmente que el número de argumentos és correcto.
#En caso contrario, el programa termina con un código de error. 
if [ $# -ne 2 ]; then
	echo "ERROR: Número incorrecto de argumentos"
	echo "Argumentos esperados: 2    ./ejercici6.sh <usuario><VSZ o RSS>"
	exit 1
fi

#Comprobamos que el segundo argumento és VSZ o RSS.
#En caso contrario, el programa termina con un código de error. 
if [ "$2" != "VSZ" ] && [ "$2" != "RSS" ]; then
	echo "ERROR: El segundo argumento tiene que ser VSZ o RSS"
	exit 1
fi

#Comprobamos que el usuario tenga algún proceso asociado. Con grep buscamos
#que el nombre del usuario se encuentra en la línea del proceso (ponemos ^ para buscar-lo
#nada más en el inicio de la línea).
#En caso contrario, el programa termina con un código de error. 
if [ "$(ps aux | grep "^$1 ")" = "" ]; then
	echo "ERROR: $1 no tiene ningún proceso asociado"
	exit 1
fi

#Dependiendo de si se quiere listar por VSZ o RSS, lo que hacemos es tuberías con ps aux para listar los procesos,
#grep para quedar-nos con los procesos del usuario deseado, sort para ordenar-los númericamente de mayor a menor
#(VSZ = 5a columna y RSS = 6a columna), head para quedadr-nos con los 5 primeros y awk para mostrar las columnas desadas.
if [ "$2" == "VSZ" ]; then
	ps aux | grep "^$1 " | sort -rnk5 | head -5 | awk '{print $5, $6, $11}'
else
	ps aux | grep "^$1 " | sort -rnk6 | head -5 | awk '{print $5, $6, $11}'
fi

#Salimos del programa.
exit 0
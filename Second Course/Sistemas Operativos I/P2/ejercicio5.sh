#!/bin/bash
#Comprobamos inicialmente que el número de argumentos és correcto.
#En caso contrario, el programa termina con un código de error. 
if [ $# -ne 3 ]; then
	echo "ERROR: Número incorrecto de argumentos"
	echo "Argumentos esperados: 3    ./ejercicio5.sh <matrícula><parámetro a modificar><nuevo status>"
	exit 1
fi

#El archivo con el que trabajaremos es coches.csv. Se podria modificar el script
#rapidamente para que el nombre del archivo fuera entrado por argumento.
archivo="coches.csv"
#Comprobamos si el archivo existe en el directorio donde nos encontramos.
#En caso contrario, el programa termina con un código de error. 
if [ ! -f $archivo ]; then
	echo "ERROR: Archivo $archivo no encontrado."
	exit 1
fi

#Comprobamos si los parametros pasados por argumentos son validos.
#En caso contrario, el programa termina con un código de error. 
if [ $2 != "ITV" ] && [ $2 != "Seguro" ] && [ $2 != "IVTM" ]; then
        echo "ERROR: Los parámetros válidos para el segundo argumento son: ITV | Seguro | IVTM"
    exit 1
fi
if [ $3 != "SI" ] && [ $3 != "NO" ]; then
        echo "ERROR: Los parámetros válidos para el tercer argumento son: SI | NO"
    exit 1
fi

#Buscamos la fila correspondiente de la matrícula en el archivo.
fila=$(grep $1 $archivo)
#En caso de que esté vacía, el programa termina con un código de error.
#(Con -z verificamos si $fila esta vacio, seria eqivalente a [ $fila == "" ])
if [ -z $fila ]; then
  echo "ERROR: Ese vehículo no se encuetra en la base de datos."
  exit 1
fi

#Vemos los anteriores datos, con el awk -F ',' para separar las columnas entre comas.
itv=$(awk -F ',' '{print $2}' <<< "$fila")
seguro=$(awk -F ',' '{print $3}' <<< "$fila")
ivtm=$(awk -F ',' '{print $4}' <<< "$fila")
#Dado que la longitud de los datos en cada columna es igual (misma longitud de matrícula
#y long("SI") = long("NO")), tamíén se podrian sacar los datos de la siguiente manera:
#itv=${fila:8:2}; seguro=${fila:11:2}; ivtm=${fila:14:2}. No lo hacemos así pues si se
#quisieran añadir o modificar datos, podria suponer un problema.

#Dpendiendo de qual parámetro se quiera modificar, con sed cambiamos la linea deseada,
#cambiando únicamente el parámetro deseado por el tercer argumento dado. 
if [ $2 = "ITV" ]; then
    sed -i "s/$fila/$1,$3,$seguro,$ivtm/g" $archivo  
elif [ $2 = "Seguro" ]; then
    sed -i "s/$fila/$1,$itv,$3,$ivtm/g" $archivo  
else
    sed -i "s/$fila/$1,$itv,$seguro,$3/g" $archivo  
fi

#Mostramos un mensaje final de correcta ejecución y salimos del programa.
echo "Estatus de la $2 actualizado para el vehiculo $1."
exit 0
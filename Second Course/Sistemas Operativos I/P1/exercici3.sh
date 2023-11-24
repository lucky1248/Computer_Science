if [ $# -ne 2 ]
then
        echo "Error: wrong args number"
        exit 1
fi

count=0

ls $1 -l > temp.txt
noms=($(awk '{print $9}' temp.txt))
rm temp.txt


i=0
len=${#noms[@]}
while [ $i -lt $len ]
do
	
	nom=${noms[$i]}

	if [ -f  $nom ]
        then
		nom=${nom%.*} #asumim que l'extensio es nomes un .ext (?) (sino .tra.gz o fi.le.txt)

		op1=$2
		op2=${nom%*$2}
		op1+=${nom#$2*}
		op2+=$2

		if [ $op1 = $nom ] || [ $nom = $op2 ]
		then
			(( count++ ))
		fi
                
        fi
        (( i++ ))


done


if [ $count -ne 0 ]
then 
	echo "Numero de Archivos: $count"
else
	echo "No se han encontrado archivos"
fi

exit 0

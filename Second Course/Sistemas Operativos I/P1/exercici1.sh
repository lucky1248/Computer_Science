sum=0
count=0
if [ $# -ne 2 ]
then
	echo "Error: wrong args number"
fi
ls $1 -l > temp.txt
noms=($(awk '{print $9}' temp.txt))
sizes=($(awk '{print $5}' temp.txt))
rm temp.txt


i=0
len=${#noms[@]}
while [ $i -lt $len ]
do
	
	if [ -f  ${noms[$i]} ] && [ ${sizes[$i]} -gt $2 ]
	then
		(( count++ ))
		sum=$(expr ${sizes[$i]} + $sum)
	fi
	(( i++ ))


done

echo "Numero de archivos: $count"
echo "Tamaño total: $sum"

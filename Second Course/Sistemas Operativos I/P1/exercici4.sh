if [ $# -ne 2 ]
then
        echo "Error: wrong args number"
        exit 1
fi

count=0


#ls $1 -l > temp.txt

#noms=($(awk '{print $9}' temp.txt))
noms=($(ls $1))
#rm temp.txt

tempDir=$1
tempDir+="/tempDir"

mkdir $tempDir

i=0
len=${#noms[@]}


while [ $i -lt $len ]
do
	
	nom=${noms[$i]}
	fNom=$1
        fNom+="/"
        fNom+=$nom
	echo "$fNom"
	if [ -f  $fNom ]
        then
		nomMod=${nom%.*} 

		nomMod+="."
		nomMod+=$2
		
	

		if [ $nom = $nomMod ]
		then

		

			cp $fNom $tempDir 			
			(( count++ ))
		fi
                
        fi
        (( i++ ))


done

tarDest=$1
tarDest+="/archivos.tar.gz"

if [ $count -ne 0 ]
then 
	tar -czf $tarDest $tempDir
	echo "He comprimido todos los archivos de extensión $2. Los tienes en $tarDest"
else
	echo "El directorio no contiene ningún archivo ningún archivo con la extensión especificada"
fi

for i in $(ls $tempDir)
do
		
	frt=$tempDir
	frt+="/"
	frt+=$i
	rm $frt
done
rmdir $tempDir

exit 0

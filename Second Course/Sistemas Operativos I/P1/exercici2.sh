um=0
count=0
if [ $# -ne 2 ]
then
        echo "Error: wrong args number"
	exit 1
fi

wd1=$1
wd2=$2

anag=1

lenT=${#wd2}
len=${#wd1}

if [ ${#wd2} -ne ${#wd1} ]
then
	anag=0

fi
if [ $anag -eq 1 ]
then
	i=0
	while [ $i -lt $len ] && [ $anag -eq 1 ]
	do      
        	
        	wd21=${wd2#*${wd1:$i:1}}
        	wd22=${wd2%%${wd1:$i:1}*}
        	wd22+=$wd21
		wd2=$wd22

		if [ ${#wd2} -ne $(expr $lenT - 1) ]
        	then
                	anag=0
        	fi
		lenT=$(expr $lenT - 1)
		(( i++ ))
	done

fi


if [ $anag -eq 1 ]
then 
	echo "Les paraules son anagrama"
else
	echo "Les paraules NO son anagrama"
fi
exit 0

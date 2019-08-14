#!usr/bin/env bash

echo -e "\e[33mExtracting IS09 Data from .wav files\e[0m"

INP=$1
DIR=$2
CWD=$3
CFG="$3/config/IS09_emotion.conf"
ERR=false


if [ ! -d "$DIR" ]
then
	echo -e "\e[33mMaking \"output\" Dir\e[0m"
	mkdir output
	if [ -d "$DIR" ]
	then
		echo -e "\e[32mSucessfully Created Directory\e[0m"
	else
		echo -e "\e[31m[Error] Directory Creation Failed\e[0m"
		$ERR=true
	fi
fi

if [ "$ERR" = false  ]
then
	if [ -f "$CFG" ]
	then
		for filename in $INP; do
			echo -e "\e[33mProcessing $filename\e[0m"
			#ITM=${filename/input\//}
			OUT=${filename/wav/arff}
			$CWD/bin/linux_x64_standalone_libstdc6/SMILExtract -C "$CFG" -I "$filename" -O "$OUT"
			if [ ! -f "$OUT" ]
			then
				echo -e  "\e[31m[ERROR] Unable to create output for ${filename}\e[0m"
			else
				echo -e "\e[32m[MSG] "$OUT" Created\e[0m"	
			fi
		done 
	else
		echo -e "\e[31m[ERROR] Config File (\"${CFG}\") Not Found\e[0m"
	fi
else
	echo -e "\e[31m[ERROR] Unexpected\e[0m"
fi
echo -e "\e[33mProcess Completed\e[0m"

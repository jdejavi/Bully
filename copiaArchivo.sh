#!/bin/bash

#Copia el fichero $1 del ordenador actual al directorio $2 del host $3

if [ $# -eq 4 ]
then
	opcion=$1
	fichero=$2
	directorio=$3
	host=$4
	
	clear
	if [ "$1" -eq "1" ]
	then
		echo "[!] Copiando el fichero [$fichero] en el directorio es [$directorio] del host [$host]"
		scp $fichero $host:/home/i3840766/Escritorio/$directorio &>/dev/null
		if [ $? -eq 0 ]
		then	
			echo "[!] Fichero [$fichero] copiado satisfactoriamente en [$host]"
		else
			echo "[!] Error al copiar el fichero [$fichero]"
		fi

		exit
	elif [ "$1" -eq "2" ]
	then
		echo "[!] Copiando el directorio [$fichero] en el directorio es [$directorio] del host [$host]"
		scp -r $fichero $host:/home/i3840766/Escritorio/$directorio &>/dev/null
		if [ $? -eq 0 ]
		then	
			echo "[!] Directorio [$fichero] copiado satisfactoriamente en [$host]"
		else
			echo "[!] Error al copiar el directorio [$fichero]"
		fi
		exit
	else
		echo "[!] Opcion no disponible..."
		echo "[!] Modo de uso: $0 [Fichero|Directorio] [Directorio_final] [Usuario@Ip_Host]"
		echo "[!] Opcion 1 -> ficheros"
		echo "[!] Opcion 2 -> directorios"	
	fi
	
fi

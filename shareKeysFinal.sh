#!/bin/bash

if [ $# -eq 1 ]
then
	host=$1
	find .ssh/ &>/dev/null
	if [ $? -eq 1 ]
	then
		echo "[!] Creando la carpeta .ssh en el host $host"
		mkdir .ssh
	fi
	cd ~/.ssh
	ssh-keygen -b 4096 -t rsa &>/dev/null
	echo "[!] Copiando las claves en el host $host"
	
	ssh-copy-id $host &>/dev/null
	ssh-add &>/dev/null
else
	echo "[!] Modo de uso --> $0 [User@Ip]"
	exit 0

fi

#!/bin/bash

if [ $# -eq 1 ]
then
	host=$1
	find .ssh/ 2>/dev/null
	if [ $? -eq 1 ]
	then
		echo "[!] Creating folder..."
		mkdir .ssh
	fi
	cd ~/.ssh
	ssh-keygen -b 4096 -t rsa &>/dev/null
	clear
	echo "[!] Copying the keys to the host..."
	
	ssh-copy-id $host &>/dev/null
	ssh-add &>/dev/null
else
	echo "[!] Modo de uso --> $0 [User@Ip]"
	exit 0

fi

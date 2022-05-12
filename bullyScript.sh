#!/bin/bash

#########################################################################
#	Script de arranque de la practica abuson			#
#	Autores:							#
#		- Javier Matilla Martin					#
#		- Oscar Vicente Vicente					#
#########################################################################
echo "----------------------------------------------"
echo "[!] Iniciando la ejecucion del script final..."
echo "----------------------------------------------"
echo;echo;echo;


echo "[!] Clonando el repositorio de la práctica...";echo
git clone https://github.com/jdejavi/Bully &>/dev/null
chmod +x Bully/*.sh
echo "[!] Descomprimiendo servidores..."
tar -xvzf Bully/tomcats.tar.gz &>/dev/null
mv tomcat8080/ Bully/
mv tomcat8081/ Bully/

echo "[!] Arrancando servidores en $(hostname -I)";echo
./Bully/tomcat8080/bin/startup.sh &>/dev/null
./Bully/tomcat8081/bin/startup.sh &>/dev/null
echo "[!] Servidores descomprimidos y arrancados";echo

echo "[!] Compilando clases del proyecto..."
ant -find Bully/PracticaFinal_v4/build.xml &>/dev/null
echo;echo "[!] Compilando el jar del proyecto...";echo
ant -find Bully/PracticaFinal_v4/build.xml jar &>/dev/null
echo "[!] Compilando el war del proyecto...";echo
ant -find Bully/PracticaFinal_v4/build.xml war &>/dev/null
echo "[!] Todos los archivos han sido compilados.";echo;echo
echo "[!] Copiando los ficheros war en los servidores...";echo
cp Bully/PracticaFinal_v4/PracticaFinal_v4.war Bully/tomcat8080/webapps/
cp Bully/PracticaFinal_v4/PracticaFinal_v4.war Bully/tomcat8081/webapps/
echo "[!] archivos war copiados satisfactoriamente";echo


echo "--------------------------------------------"
echo "Empezando la clonacion de claves en los host"
echo "--------------------------------------------";echo
while true
do
	echo "[!] Introduce el host (usuario@ip):"
	read host
	if [ "$host" = "q" ]
	then
		echo "[!] Saliendo..."
		exit 1
	else
		echo "[!] Copiando el par de claves en el host $host"
		./Bully/shareKeysFinal.sh $host
		echo "[!] Claves copiadas correctamente en el host $host"
		echo "[!] Copiando los servidores en el host $host"
		./Bully/copiaArchivo.sh 2 Bully/tomcat8080/ /home/i3840766/Escritorio/ $host &>/dev/null
		./Bully/copiaArchivo.sh 2 Bully/tomcat8081/ /home/i3840766/Escritorio/ $host &>/dev/null
		echo "[!] Directorios copiados satisfactoriamente $host"
		echo "[!] Arrancando servidores en el host $host"
		ssh $host "./Escritorio/tomcat8080/bin/startup.sh; ./Escritorio/tomcat8081/bin/startup.sh; exit";echo
		echo "[!] Servidores arrancados correctamente"
		 
	fi
	
done
clear
echo "----------------------------------------------------------------"
echo "|		Iniciando la practica final... (o no)  		|"
echo "----------------------------------------------------------------"

java -jar Bully/PracticaFinal_v4/Bully.jar Bully/PracticaFinal_v4/args.csv



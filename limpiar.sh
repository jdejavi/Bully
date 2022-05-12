#limpia cualquier carpeta con nombre $2 en la mAquina@usuario $1
if [ $# -eq 1 ]
then
	host=$1
	#path=$2
	echo "[!] Borrando carpeta Bully del host $(hostname -I)"
	echo "[!] Parando los servidores en el host $host"
	ssh $host "./Escritorio/tomcat8080/bin/shutdown.sh; ./Escritorio/tomcat8081/bin/shutdown.sh; exit"
	echo "[!] Servidores parados correctamente"
	echo "[!] Borrando los servidores en el $host"
	ssh $host "rm -rf /home/i3840766/Escritorio/tomcat8080;rm -rf /home/i3840766/Escritorio/tomcat8081; exit;"
	echo "[!] Directorios limpiados satisfactoriamente"
else
	echo "[!] Uso: $0 [Usuario@Ip maquina]"
	exit
fi

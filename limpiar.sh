#limpia cualquier carpeta con nombre $2 en la mAquina@usuario $1
if [ $# -eq 2 ]
then
	host=$1
	path=$2
	
	echo "[!] Lanzando limpiar directorio [$path] en el host [$host]"
	ssh $host "rm -rf /home/i3840766/Escritorio/$path/*; exit;"
	echo "[!] Directorio limpiada satisfactoriamente"
else
	echo "[!] Uso: $0 [Usuario@Ip maquina] [directorio]"
	exit
fi

/*
 * Practica obligatoria para Sistemas Distribuidos
 * Implementacion del algoritmo "Bully"
 * Autores:
 * 		Javier Matilla Martin (maatii@usal.es)
 * 		Oscar Vicente Vicente (oscarvicente@usal.es)
 * */

package dtos;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InitDTO {
	
	public Map<Integer,String> mapa;
	public Integer id;
	
	public InitDTO(Map<Integer,String> map, Integer iD) {
		this.mapa=map;
		this.id=iD;
	}
}

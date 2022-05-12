/*
 * Practica obligatoria para Sistemas Distribuidos
 * Implementacion del algoritmo "Bully"
 * Autores:
 * 		Javier Matilla Martin (maatii@usal.es)
 * 		Oscar Vicente Vicente (oscarvicente@usal.es)
 * */

package gestor;

import java.awt.Menu;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.ClientProperties;

import dtos.*;
import com.google.gson.Gson;
import java.util.Scanner;

public class Gestor {

	static Map<Integer,String> mapa = new HashMap<Integer,String>();
	static Client client = ClientBuilder.newClient().property(ClientProperties.CONNECT_TIMEOUT, 100).property(ClientProperties.READ_TIMEOUT, 100);
	static Client client2 = ClientBuilder.newClient();
	static String menu = "\n\nEsto es todo lo que puedes hacer con los hilos:\n \t1 --> Arrancar\n \t2 --> Parar\n "
				   + "\t3 --> Comprobar estado de los procesos\n \t4 --> Arrancar todos los procesos\n
				   + "\t5 --> Inicializar\n\th --> Menu de ayuda\n \tq --> Salir";
	static String menuAyuda = 
	 "==================================================="
    +"\n|     Has entrado en el menu de ayuda al usuario  |\n"
    +"==================================================="
    +"\nLa opcion 1 arranca el proceso seleccionado"
    +"\nLa opcion 2 para el proceso seleccionado"
    +"\nLa opcion 3 comprueba el estado de cada proceso"
    +"\nLa opcion 4 arranca todos los procesos"
    +"\nLa opcion 5 inicializa un proceso de un servidor que se ha caido previamente"
    +"\nLa opcion h despliega este menu.\n\n"
    +"\nLa opcion q te saca de la app.\n\n";
	
	private static void runMenu(){
        /*Metodo que enseña el menu para accionar los hilos
        */
        String opcion = null;
        String[] opciones = {"1","2","3","4","h","q"};
        Scanner sc = new Scanner(System.in);
        Integer proceso;
        
        boolean salir = false;
        do{
        	System.out.println(menu);
        	System.out.print("Opcion: ");
            opcion = sc.next().toLowerCase();
            switch(opcion){
            
                case "1":
                	System.out.print("\n¿Que proceso deseas arrancar? :");
                	proceso = sc.nextInt();
                	try {
        				URI uri = UriBuilder.fromUri("http://"+mapa.get(proceso)+"/PracticaFinal_v4").build();
        				WebTarget target = client2.target(uri);
        				System.out.printf(uri.toString());
        				String res = target.path("rest").path("proceso").path("arrancar")
        						.request()
        						.get(String.class);
        			}catch(Exception e) {
        				e.printStackTrace();
        			}
                break;
                
                case "2":
                	System.out.println("\n¿Que proceso deseas parar? :");
	            	proceso = sc.nextInt();
	            	try {
	    				URI uri = UriBuilder.fromUri("http://"+mapa.get(proceso)+"/PracticaFinal_v4").build();
	    				WebTarget target = client2.target(uri);
	    				System.out.printf(uri.toString());
	    				String res = target.path("rest").path("proceso").path("parar")
	    						.request()
	    						.get(String.class);
	    			}catch(Exception e) {
	    				e.printStackTrace();
	    			}
            	break;
            	
                case "3":
                	comprobarEstado();
         
                break;
                case "4":
                	
                	try {
                		for(Map.Entry<Integer, String> entry : mapa.entrySet()) {
                			System.out.println("Arrancando proceso con id ->"+entry.getKey());
                    		URI uri = UriBuilder.fromUri("http://"+entry.getValue()+"/PracticaFinal_v4").build();
    	    				WebTarget target = client2.target(uri);
    	    				System.out.printf(uri.toString());
    	    				String res = target.path("rest").path("proceso").path("arrancar")
    	    						.request()
    	    						.get(String.class);
    	    				System.out.println(res);
    	    				Thread.sleep(1000);
                    	}
                    	break;
                	}catch(Exception e){}
		case "5":
			System.out.print("\n¿Que proceso deseas inicializar (no arrancar)? :");
                	proceso = sc.nextInt();
			try {
				InitDTO dto = new InitDTO(mapa, proceso);
				String json = new Gson().toJson(dto);
				
				URI uri = UriBuilder.fromUri("http://"+mapa.get(proceso)+"/PracticaFinal_v4").build();
				WebTarget target = client.target(uri);
				System.out.printf(uri.toString());
				Response res = target.path("rest").path("proceso").path("init2")
						.request("text/plain")
						.post(Entity.text(json));
			}catch(Exception e) {
				
			}
                		
                case "h":
                	System.out.println(menuAyuda);
                	break;
                case "q": salir=true; break;
                
                default: System.out.println("\nNo messirve tu opcion\n\n"); break;
            }
        }while(!salir);
    }
	
	public static void main(String[] args){  
		Gson pruebaGson = new Gson();
		
		String line = "";  
		String splitBy = ",";  
		try{  
	  
			BufferedReader br = new BufferedReader(new FileReader(args[0]));
			while ((line = br.readLine()) != null){     
				String[] linea = line.split(splitBy);   
				
				mapa.put(Integer.parseInt(linea[0]), linea[1]);
			}
			iniciar();
			System.out.flush(); 
			runMenu();
		}
		catch (IOException e){ 
			e.printStackTrace();  
		}  
	}
	
	public static void iniciar() {
		for(Map.Entry<Integer, String> entry : mapa.entrySet()) {
			try {
				InitDTO dto = new InitDTO(mapa, entry.getKey());
				String json = new Gson().toJson(dto);
				
				URI uri = UriBuilder.fromUri("http://"+entry.getValue()+"/PracticaFinal_v4").build();
				WebTarget target = client.target(uri);
				System.out.printf(uri.toString());
				Response res = target.path("rest").path("proceso").path("init2")
						.request("text/plain")
						.post(Entity.text(json));
			}catch(Exception e) {
				
			}
			
			
		}
		
	}
	private static void comprobarEstado() {
		Client clienteQuery = ClientBuilder.newClient().property(ClientProperties.CONNECT_TIMEOUT, 300).property(ClientProperties.READ_TIMEOUT, 300);
		try {
    		while (System.in.available() == 0) {
    			System.out.println("|                    URL                   |"
    		+	"|     ID    |"
        	 + "|  Coordinador   |"
        	 + "|  Estado eleccion  |"
        	 + "|   Estado    ");
    			for(Map.Entry<Integer, String> entry : mapa.entrySet()) {
            		try {
            		URI uri = UriBuilder.fromUri("http://"+entry.getValue()+"/PracticaFinal_v4").build();
    				WebTarget target = clienteQuery.target(uri);
    				System.out.printf(uri.toString());
    				String res = target.path("rest").path("proceso").path("comprobar")
    						.request()
    						.get(String.class);
    				System.out.println(res);
            		}catch(Exception e) {
            			System.out.println("  ||      Timeout     ||");
            		}
            	}
    			Thread.sleep(1500);
    			System.out.print("\n");
    		}	
    	} catch(Exception ex) {}
	}
}    

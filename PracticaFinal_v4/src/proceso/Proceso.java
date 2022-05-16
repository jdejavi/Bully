/*
 * Practica obligatoria para Sistemas Distribuidos
 * Implementacion del algoritmo "Bully"
 * Autores:
 *         Javier Matilla Martin (maatii@usal.es)
 *         Oscar Vicente Vicente (oscarvicente@usal.es)
 * */

package proceso;

import java.net.URI;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import dtos.*;
import org.glassfish.jersey.client.ClientProperties;
import com.google.gson.Gson;

@Singleton
@Path("proceso")
public class Proceso {
	final Integer timeout = 6000;
    Boolean recibido=false;
    Boolean coordEnviado=false;
    Client client = ClientBuilder.newClient().property(ClientProperties.CONNECT_TIMEOUT, timeout).property(ClientProperties.READ_TIMEOUT, timeout);;
    Map<Integer,String> mapaIds = new ConcurrentHashMap<Integer,String>();
    Integer id;
    Integer coordinadorActual;
    List<String> arrayRespuestas = new CopyOnWriteArrayList<String>();
    String estadoEleccion;
    Boolean estaEncendido;
    Semaphore encendidoSemaphore = new Semaphore(1);
    Boolean tenemosCoordinador = false;
    
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Path("init2")
    
    public void init2(String json) {
        Gson deserialize = new Gson();
        InitDTO dto = deserialize.fromJson(json, InitDTO.class);
        this.mapaIds.putAll(dto.mapa);
        this.id=dto.id;
        try {
            encendidoSemaphore.acquire();
            estaEncendido=false;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        run();
    }
    
    private Integer obtenerKeyMayor() {
        Integer mayorK = 0;
        for(Map.Entry<Integer, String> entry : mapaIds.entrySet()) {
            Integer key = entry.getKey();
            if(key>mayorK) {
                mayorK=key;
            }
        }
        return mayorK;
    }
    
    //Timeout para detectar cuando ha fallado el coordinador
    
    
    public void run() {
        
        while(true) {
            
            System.out.printf("El id es -> "+id+ " El coordinador actual es -> "+coordinadorActual+"\n");
            if(!estaEncendido) {
                
                //Bloquear
                try {
                    
                    encendidoSemaphore.acquire();
                    encendidoSemaphore.release();
                } catch (InterruptedException e) {
                    
                    e.printStackTrace();
                }
                
            }else {
            	try {
                    Thread.sleep(new Random().nextInt(500)+500);
                    if(estaEncendido && estadoEleccion.equalsIgnoreCase("Acuerdo")) {
                    	//Llamada al coordinador
                        URI uri = UriBuilder.fromUri("http://"+mapaIds.get(coordinadorActual)+"/PracticaFinal_v4").build();
                        WebTarget target = client.target(uri);
                        
                        try {
                            String res = target.path("rest").path("proceso").path("computar")
                                    .request()
                                    .get(String.class);
                            
                            if(Integer.parseInt(res)<0 && estaEncendido) {
                                
                                this.convocarElecciones();
                            }
                        } catch(Exception ex) {
                            this.convocarElecciones();
                        }
                    }
                    
                    
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Path("coordinadorSet")
    
    public String coordinadorSeteado(String idCoordNuevo) {
        if(!estaEncendido){
		    this.coordinadorActual=0;
	    }else{
            this.coordinadorActual = Integer.parseInt(idCoordNuevo);

            this.estadoEleccion="Acuerdo";
            this.tenemosCoordinador=true;
        }
        return "OK del coordinador mayor seteado";
    }
    
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("convocarElecciones")
    public void convocarElecciones() {
        
        recibido=false;
        coordEnviado=false;
        
        arrayRespuestas.clear();
        
        if(!estaEncendido) return;
        
        
        int numMaxMsg = 0;
        
        if(obtenerKeyMayor() == id) {
        
            for(Map.Entry<Integer, String> entry : mapaIds.entrySet()) {
                URI uri = UriBuilder.fromUri("http://"+entry.getValue()+"/PracticaFinal_v4").build();
                WebTarget target = client.target(uri);
        	try{
			Response res = target.path("rest").path("proceso").path("coordinadorSet")
				.request("text/plain")
				.post(Entity.text(""+id));
		}catch(Exception e){}
                
            }
        }else {
            Client clienteConTimeout = ClientBuilder.newClient().property(ClientProperties.CONNECT_TIMEOUT, timeout).property(ClientProperties.READ_TIMEOUT, timeout);
            
            numMaxMsg = obtenerKeyMayor()-this.id;
            this.tenemosCoordinador=false;
            
            for(Map.Entry<Integer, String> entry : mapaIds.entrySet()) {
                if(entry.getKey() > this.id) {
        
                    URI uri = UriBuilder.fromUri("http://"+entry.getValue()+"/PracticaFinal_v4").build();
                    WebTarget target = clienteConTimeout.target(uri);
                    estadoEleccion="Eleccion Activa";
                    
                    try {
                        String res = target.path("rest").path("proceso").path("elecciones")
                                .request()
                                .get(String.class);
                        
                        arrayRespuestas.add(res);
                    } catch(Exception ex) {
                        arrayRespuestas.add("MAMASTE");
                    }
                }
                
            }
        
                Boolean algunOK = false;
                for(String resp : arrayRespuestas) {
                    if(resp.equalsIgnoreCase("OK")) {
                        algunOK=true;
                        estadoEleccion="Eleccion Pasiva";
                    }
                }
                
                
                if(!algunOK) {
        
                    for(Map.Entry<Integer, String> entry2 : mapaIds.entrySet()) {
                        if(entry2.getKey()<this.id) {
                            try {
                                URI uri2 = UriBuilder.fromUri("http://"+entry2.getValue()+"/PracticaFinal_v4").build();
                                WebTarget target2 = client.target(uri2);
                                System.out.print(this.id+" "+algunOK);
                                Response res2 = target2.path("rest").path("proceso").path("coordinadorSet")
                                        .request("text/plain")
                                        .post(Entity.text(""+id));
                                coordEnviado=true;
                            }catch(Exception e) {}
                        }else if(entry2.getKey() == this.id) {
                        	coordinadorSeteado(id+"");
                        }
                        
                    }
                }else {
                    try {
                        Thread.sleep(timeout*2);
                        if(!tenemosCoordinador) {
                            this.convocarElecciones();
                        }
                        else {
                        	//estadoEleccion="Acuerdo";
                        }
                    }catch(Exception e) {
                        
                    }
                }
            //}
        }
    
    }
    
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("elecciones")
    public String elecciones() {
        
        if(!estaEncendido) return "KO";
        
        try {
            Client clienteSinTimeout = ClientBuilder.newClient().property(ClientProperties.CONNECT_TIMEOUT, 100).property(ClientProperties.READ_TIMEOUT, 100);
            URI uri = UriBuilder.fromUri("http://"+mapaIds.get(this.id)+"/PracticaFinal_v4").build();
            WebTarget target = clienteSinTimeout.target(uri);
            estadoEleccion="Eleccion Activa";
            String res = target.path("rest").path("proceso").path("convocarElecciones")
                        .request()
                        .get(String.class);
        } catch(Exception ex) {
            
        }
        return "OK";
    }
    
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("computar")
    public String computar() {
        if(!estaEncendido) {
        
            return -1+"";
        }
        else {
            if(coordinadorActual == id) {
            	try {
                    Thread.sleep(new Random().nextInt(200)+100);
                    //return "1"
                    return 1+"";
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return -1+"";
                }
            } else {
            	return -1+"";
            }
        }
    }
    
    //Arranca un proceso
    @GET
    @Path("arrancar")
    public void arrancar() {
        System.out.printf("Arrancando hilo con id = %d...\n", this.id);
        
        if(!estaEncendido) {
            estaEncendido=true;
            encendidoSemaphore.release();
            this.convocarElecciones();
        }
        
    }
    
    //Para un proceso
    @GET
    @Path("parar")
    public void parar(){
        System.out.printf("Parando hilo con id = %d...\n", this.id);
        if(estaEncendido) {
            try {
                encendidoSemaphore.acquire();
                estaEncendido=false;
                
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("comprobar")
    public String comprobarEstados() {
        String state;
        if(estaEncendido) state="Encendido";
        else state = "Apagado";
        return "  ||     " + id + "     |"
        	 + "|      " + coordinadorActual + "      |"
        	 + "|        " + estadoEleccion +"       |"
        	 + "|    " + state + "     ";
        	 
    }
    
}

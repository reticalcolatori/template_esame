package model;

import java.io.*;
import java.net.*;


public class TCP_Server
{
    // porta nel range consentito 1024-65535!
    // dichiarata come statica perche caratterizza il server
	private static final int PORT = 54321;
	private final static int N = 10;
	
	public static Viaggio orariTreni[];

	public static void main (String[] args) throws IOException
	{
      	// Porta sulla quale ascolta il server
		int port = -1;

      	// Controllo degli argomenti
		try 
		{
			if (args.length == 1) 
			{
				port = Integer.parseInt(args[0]);
	    		// controllo che la porta sia nel range consentito 1024-65535
				if (port < 1024 || port > 65535) 
				{
					System.out.println("Usage: java TCP_Server [serverPort>1024]");
					System.exit(1);
				}
			} 
			else if (args.length == 0) 
			{
				port = PORT;
			} 
			else 
			{
				System.out.println("Usage: java TCP_Server or java TCP_Server port");
				System.exit(1);
			}
		} 
		catch (Exception e) 
		{
			System.out.println("Problemi, i seguenti: ");
			e.printStackTrace();
			System.out.println("Usage: java TCP_Server or java TCP_Server port");
			System.exit(1);
		}
		
		/******** INIZIALIZZAZIONE TABELLA  + stampa************/
			
		inizializza();
		stampa();
		
		/*********************************************/

		ServerSocket serverSocket = null;
		Socket clientSocket = null;
		try 
		{
			serverSocket = new ServerSocket(port);
			serverSocket.setReuseAddress(true);
			System.out.println("TCP_Server: avviato ");
			System.out.println("Server: creata la server socket: " + serverSocket);
		}
		catch (Exception e) 
		{
			System.err.println("Server: problemi nella creazione della server socket: "+ e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}

		try 
		{
			while (true) 
			{
				System.out.println("Server: in attesa di richieste...\n");

				try 
				{
	    			// bloccante finchè non avviene una connessione
					clientSocket = serverSocket.accept();
	    			// timeout per evitare che un thread si blocchi indefinitivamente
					clientSocket.setSoTimeout(100000);

					System.out.println("Server: connessione accettata: " + clientSocket);
				}
				catch (Exception e) 
				{
					System.err.println("Server: problemi nella accettazione della connessione: "+ e.getMessage());
					e.printStackTrace();
					continue;
	    			// il server continua a fornire il servizio ricominciando dall'inizio del ciclo
				}

	  			// delego il servizio ad un nuovo thread
				try 
				{
					new TCP_ServerThread(clientSocket).start();
				}
				catch (Exception e) 
				{
					System.err.println("Server: problemi nel server thread: "+ e.getMessage());
					e.printStackTrace();
					continue;
	   				// il server continua a fornire il servizio ricominciando dall'inizio del ciclo
				}

			} 
		}
	     // qui catturo le eccezioni non catturate all'interno del while
	     // in seguito alle quali il server termina l'esecuzione
		catch (Exception e) 
		{
			e.printStackTrace();
			System.out.println("TCP_Server: termino...");
			System.exit(2);
		}

	}

	public static void stampa() {
		System.out.println("ID\tTIPO\tPARTENZA\tARRIVO\tORA ATTESA\tRITARDO\tAUDIO");

		for (int i = 0; i < orariTreni.length; i++) {
			System.out.println(orariTreni[i].getId() + "\t" + orariTreni[i].getTipo() + "\t" + orariTreni[i].getPartenza() + "\t" + orariTreni[i].getArrivo() + "\t" + orariTreni[i].getHh() + ":" + orariTreni[i].getMm() + "\t" + orariTreni[i].getRitardo() + "\t" + orariTreni[i].getFileAudio());
		}
	}

	private static void inizializza() {
		orariTreni = new Viaggio[10];
		
		orariTreni[0] = new Viaggio("SATA1234", "Partenza", "Bologna", "Bari", 12, 15, 0, "SATA1234.mp4Audio");
		orariTreni[1] = new Viaggio("MATA3333", "Arrivo", "Milano", "Bologna", 16, 30, 190, "MATA3333.mp4Audio");
		orariTreni[2] = new Viaggio("CATA1111", "Partenza", "Bologna", "Napoli", 13, 00, 190, "CATA1111.mp4Audio");

		
		
		for(int i = 3; i < N; i++) {
			orariTreni[i] = new Viaggio("L", "L", "L", "L", -1, -1, -1, "L");
		}
		
	}
} // TCP_Server
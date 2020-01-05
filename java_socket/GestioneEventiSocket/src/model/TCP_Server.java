package model;

/************************************
*           *
*     TCP_Server.java		    *
*           		            *
************************************/
import java.io.*;
import java.net.*;

public class TCP_Server {
	// porta nel range consentito 1024-65535!
	// dichiarata come statica perche caratterizza il server
	private static final int PORT = 54321;

	private static final int N = 10;

	public static Evento eventi[];

	public static void main(String[] args) throws IOException {
		// Porta sulla quale ascolta il server
		int port = -1;

		// Controllo degli argomenti
		try {
			if (args.length == 1) {
				port = Integer.parseInt(args[0]);
				// controllo che la porta sia nel range consentito 1024-65535
				if (port < 1024 || port > 65535) {
					System.out.println("Usage: java TCP_Server [serverPort>1024]");
					System.exit(1);
				}
			} else if (args.length == 0) {
				port = PORT;
			} else {
				System.out.println("Usage: java TCP_Server or java TCP_Server port");
				System.exit(1);
			}
		} catch (Exception e) {
			System.out.println("Problemi, i seguenti: ");
			e.printStackTrace();
			System.out.println("Usage: java TCP_Server or java TCP_Server port");
			System.exit(1);
		}
		/******** INIZIALIZZAZIONE TABELLA ************/

		eventi = new Evento[N];
		inizializza();
		stampa();

		/*********************************************/

		ServerSocket serverSocket = null;
		Socket clientSocket = null;
		try {
			serverSocket = new ServerSocket(port);
			serverSocket.setReuseAddress(true);
			System.out.println("TCP_Server: avviato ");
			System.out.println("Server: creata la server socket: " + serverSocket);
		} catch (Exception e) {
			System.err.println("Server: problemi nella creazione della server socket: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}

		try {
			while (true) {
				System.out.println("Server: in attesa di richieste...\n");

				try {
					// bloccante finchè non avviene una connessione
					clientSocket = serverSocket.accept();
					// timeout per evitare che un thread si blocchi indefinitivamente
					clientSocket.setSoTimeout(60000);

					System.out.println("Server: connessione accettata: " + clientSocket);
				} catch (Exception e) {
					System.err.println("Server: problemi nella accettazione della connessione: " + e.getMessage());
					e.printStackTrace();
					continue;
					// il server continua a fornire il servizio ricominciando dall'inizio del ciclo
				}

				// delego il servizio ad un nuovo thread
				try {
					new TCP_ServerThread(clientSocket).start();
				} catch (Exception e) {
					System.err.println("Server: problemi nel server thread: " + e.getMessage());
					e.printStackTrace();
					continue;
					// il server continua a fornire il servizio ricominciando dall'inizio del ciclo
				}

			}
		}
		// qui catturo le eccezioni non catturate all'interno del while
		// in seguito alle quali il server termina l'esecuzione
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("TCP_Server: termino...");
			System.exit(2);
		}

	}

	private static void stampa() {
		System.out.println("Descrizione\tTipo\tData\tLuogo\tDisponibilita\tPrezzo");
		for (int i = 0; i < N; i++) {
			System.out.println(eventi[i].getDescrizione() + "\t" + eventi[i].getTipo() + "\t" + eventi[i].getGg() + "/"
					+ eventi[i].getMm() + "/" + eventi[i].getAaaa() + "\t" + eventi[i].getLuogo() + "\t"
					+ eventi[i].getDisponibilita() + "\t" + eventi[i].getPrezzo());
		}
	}

	private static void inizializza() {
		for (int i = 0; i < N; i++) {
			eventi[i] = new Evento("L", "L", -1, -1, -1, -1, -1, "L");
		}

		eventi[1].setDescrizione("String");
		eventi[1].setTipo("Concerto");
		eventi[1].setGg(12);
		eventi[1].setAaaa(2012);
		eventi[1].setMm(12);
		eventi[1].setDisponibilita(40);
		eventi[1].setPrezzo(40);
		eventi[1].setLuogo("Bologna");

		eventi[2].setDescrizione("JuveBolog");
		eventi[2].setTipo("Concerto");
		eventi[2].setGg(23);
		eventi[2].setAaaa(2012);
		eventi[2].setMm(12);
		eventi[2].setDisponibilita(32);
		eventi[2].setPrezzo(67);
		eventi[2].setLuogo("Bologna");

		eventi[4].setDescrizione("GPCroce");
		eventi[4].setTipo("Formula1");
		eventi[4].setGg(34);
		eventi[4].setAaaa(2001);
		eventi[4].setMm(1);
		eventi[4].setDisponibilita(430);
		eventi[4].setPrezzo(340);
		eventi[4].setLuogo("Milano");

	}
} // TCP_Server

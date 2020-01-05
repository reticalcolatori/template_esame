package model;
/* CLIENTE CON CONNESSIONE*/

import java.net.*;
import java.io.*;

public class TCP_Client {

	/**
	 * @param args - Usage: java TCP_Client <address> <port>
	 */

	public static void main(String[] args) throws IOException {
		/*
		 * Come argomenti si devono passare un nome di host valido e una porta
		 *
		 */
		InetAddress addr = null;
		int port = -1;

		// Controllo argomenti
		try {
			if (args.length == 2) {
				addr = InetAddress.getByName(args[0]);
				port = Integer.parseInt(args[1]);
			} else {
				System.out.println("Usage: java TCP_Client serverAddr serverPort");
				System.exit(1);
			}
		} catch (NumberFormatException e) {
			System.out.println("Numero di porta errato: " + args[1]);
			e.printStackTrace();
			System.out.println("Usage: java TCP_Client serverAddr serverPort");
			System.exit(1);
		} catch (Exception e) {
			System.out.println("Problemi, i seguenti: ");
			e.printStackTrace();
			System.out.println("Usage: java TCP_Client serverAddr serverPort");
			System.exit(1);
		}

		/** ----- Inizializzazione strutture dati Cliente ----- **/
		Socket socket = null;
		DataInputStream inSock = null;
		DataOutputStream outSock = null;
		String richiesta = null;

		// Creazione socket
		// Creazione stream di input/output su socket
		try {
			socket = new Socket(addr, port);
			// Setto il timeout per non bloccare indefinitivamente il client
			socket.setSoTimeout(30000);
			System.out.println("Creata la socket: " + socket);
			inSock = new DataInputStream(socket.getInputStream());
			outSock = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			System.out.println("Problemi nella creazione degli stream su socket: ");
			e.printStackTrace();
			System.exit(1);
		} catch (Exception e) {
			System.out.println("Problemi nella creazione della socket: ");
			e.printStackTrace();
			System.exit(2);
		}

		// Eventuale stream per l'interazione con l'utente da tastiera

		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

		System.out.println("Inserisci V per visualizzazione eventi tipo/luogo");
		System.out.println("Inserisci D per eventi disponibili con prezzo <= a soglia");
		System.out.print("\n^D(Unix)/^Z(Win)+invio per uscire.");

		try {
			while ((richiesta = stdIn.readLine()) != null) {

				if (richiesta.equalsIgnoreCase("V") || richiesta.equalsIgnoreCase("T")) {
					try {
						// Elaborazione richiesta e invio
						outSock.writeUTF(richiesta);
						System.out.println("Inviata richiesta: " + richiesta);
					} catch (Exception e) {
						System.out.println("Problemi nell'invio della richiesta " + richiesta);
						e.printStackTrace();

						System.out.print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti input: ");
						continue;
						// il client continua l'esecuzione riprendendo dall'inizio del ciclo
					}
				}

				//outSock.flush();

				if (richiesta.equalsIgnoreCase("V")) {

					System.out.println("Inserisci il tipo:");
					String tipo = stdIn.readLine();

					if (tipo == null) {
						// EOF
						break;
					}

					System.out.println("Inserisci il luogo:");
					String luogo = stdIn.readLine();

					if (luogo == null) {
						// EOF
						break;
					}

					// Invio dei parametri.

					try {
						// Elaborazione richiesta e invio
						outSock.writeUTF(tipo);
						outSock.writeUTF(luogo);
					} catch (Exception e) {
						e.printStackTrace();
						System.out.print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti input: ");
						continue;
					}

					// Ricezione della risposta

					int risposta;
					String currEvento = null;
					
					try {
						risposta = inSock.readInt();
						
						for (int i = 0; i < risposta; i++) {
							currEvento = inSock.readUTF();
							System.out.println(currEvento);
						}
						
					} catch (SocketTimeoutException ste) {
						System.out.println("Timeout scattato: ");
						ste.printStackTrace();
						System.out.print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti input: ");
						continue;
					} catch (EOFException e) {
						System.out.println("Raggiunta la fine delle ricezioni, chiudo...");
						socket.close();
						System.out.println("TCP_Client: termino...");
						System.exit(0);
					} catch (Exception e) {
						System.out.println("Problemi nella ricezione della risposta, i seguenti: ");
						e.printStackTrace();
						System.out.print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti input: ");
						continue;
					}
				} else if (richiesta.equalsIgnoreCase("T")) {
					
					System.out.println("Inserisci la soglia max:");
					int soglia = Integer.parseInt(stdIn.readLine());
					
					try {
						// Elaborazione richiesta e invio
						outSock.writeInt(soglia);
						
					} catch (Exception e) {
						e.printStackTrace();
						System.out.print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti input: ");
						continue;
					}
					
					// Ricezione della risposta

					int risposta;
					String currEvento = null;
					
					try {
						risposta = inSock.readInt();
						
						for (int i = 0; i < risposta; i++) {
							currEvento = inSock.readUTF();
							System.out.println(currEvento);
						}
						
					} catch (SocketTimeoutException ste) {
						System.out.println("Timeout scattato: ");
						ste.printStackTrace();
						System.out.print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti input: ");
						continue;
					} catch (EOFException e) {
						System.out.println("Raggiunta la fine delle ricezioni, chiudo...");
						socket.close();
						System.out.println("TCP_Client: termino...");
						System.exit(0);
					} catch (Exception e) {
						System.out.println("Problemi nella ricezione della risposta, i seguenti: ");
						e.printStackTrace();
						System.out.print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti input: ");
						continue;
					}
					
				} else {
					System.out.println("Errore nella scelta...");
				}

// Tutto ok, pronto per nuova richiesta
				System.out.print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti input: ");

			} // while

			System.out.println("TCP_Client: termino...");
		}

// qui catturo le eccezioni non catturate all'interno del while
// quali per esempio la caduta della connessione con il server
// in seguito alle quali il client termina l'esecuzione
		catch (Exception e) {
			System.err.println("Errore irreversibile, il seguente: ");
			e.printStackTrace();
			System.err.println("Chiudo!");
			System.exit(3);
		}
	} // main
}

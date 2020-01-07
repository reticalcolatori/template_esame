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
		System.out.print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti input, A per aggiungere un viaggio: ");

		// variabili per aggiunta di viaggio sul server
		String id, tipo, stringOra, fileAudio;
		int hh, mm;

		try {

			while ((richiesta = stdIn.readLine()) != null) {

				if (richiesta.equalsIgnoreCase("A")) {
					try {
						// Elaborazione richiesta e invio
						outSock.writeUTF(richiesta);
						System.out.println("Inviata richiesta: " + richiesta);
						// chiudo output della socket se non devo inviare altri paramentri al server ma
						// aspettarmi solo il risultato
//		      			socket.shutdownOutput();
//		      			System.out.println("Chiusura output della socket: " + socket);

						System.out.println("Inserisci ID del viaggio che intendi aggiungere:");
						id = stdIn.readLine();

						System.out.println("Inserisci tipo di viaggio (Partenza/Arrivo):");
						tipo = stdIn.readLine();

						System.out.println("Inserisci le ore di arrivo previste (hh:mm):");
						stringOra = stdIn.readLine();

						System.out.println("Inserisci nome del file audio associato al viaggio:");
						fileAudio = stdIn.readLine();

						String splitString[] = stringOra.split(":");
						try {
							hh = Integer.parseInt(splitString[0]);
							mm = Integer.parseInt(splitString[1]);
						} catch (NumberFormatException e) {
							e.printStackTrace();
							System.out.print(
									"\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti input, A per aggiungere un viaggio: ");
							continue;
						}
					} catch (Exception e) {
						System.out.println("Problemi nell'invio della richiesta " + richiesta);
						e.printStackTrace();
						System.out.print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti input: ");
						continue;
						// il client continua l'esecuzione riprendendo dall'inizio del ciclo
					}

					// --> qui logica

					// mi aspetto in ordine sottostante:
					/*
					 * 1) ID 2) TIPO 3) PARTENZA 4) ARRIVO 5) ORA ATTESA (PRIMA INTERO PER HH POI
					 * MINUTI) 6) NOME FILE AUDIO (E POI RICEVE IL FILE IN QUESTO MODO IL CLIENT
					 * MANDA PRIMA NOME POI LUNGHEZZA DEL FILE IN BYTE, POI INVIA IL FILE EFFETTIVO
					 * 
					 * RITORNA ESITO OP > 0 SE OK < 0 SE VIAGGIO CON STESSO ID GIÀ PRESENTE
					 * STRUTTURA DATI PIENA FALLISCONO I CONTROLLI SUI DATI IN INPUT
					 */

					outSock.writeUTF(id);
					outSock.writeUTF(tipo);
					outSock.writeInt(hh);
					outSock.writeInt(mm);
					outSock.writeUTF(fileAudio);
					// lunghezza

					//PRIMA DI INVIARE LA LUNGHEZZA ATTENDO CHE IL SERVER MI DIA OK PUOI INVIARE IL FILE POICHé IO NON LO HO.
					boolean goOn;
					goOn = inSock.readBoolean();
					
					
					if(goOn) {
						
						try {
							File currFile = new File(fileAudio);
							long lunghezzaFileCorrente = currFile.length();
							outSock.writeLong(lunghezzaFileCorrente);

							BufferedInputStream streamFile = new BufferedInputStream(new FileInputStream(currFile));

							int currByte = -1;
							
							/*int x;
							while ((x = streamFile.read()) != -1) {
								outSock.write(x);
							}
							System.out.println("File inviato al server!");
							*/
							int count;
							byte x[] = new byte[255];
							while ((count = streamFile.read(x)) > 0) {
								outSock.write(x, 0, count);
							}
							System.out.println("File inviato al server!");

							streamFile.close();

						} catch(FileNotFoundException ex) {
							System.out.println("Impossibile aprire il file specificato!");
							System.out.print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti input, A per aggiungere un viaggio: ");
							continue;
						}
						
					}

					// Ricezione della risposta

					int risposta;
					try {
						risposta = inSock.readInt();
						System.out.println("Risposta: " + risposta);
						// chiudo input della socket
						// socket.shutdownInput();
						// System.out.println("Chiusura input della socket: " + socket);
					} catch (SocketTimeoutException ste) {
						System.out.println("Timeout scattato: ");
						ste.printStackTrace();
						socket.close();
						System.out.print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti input: ");
						continue;
						// il client continua l'esecuzione riprendendo dall'inizio del ciclo
					}

					catch (EOFException e) {
						System.out.println("Raggiunta la fine delle ricezioni, chiudo...");
						socket.close();
						System.out.println("TCP_Client: termino...");
						System.exit(0);
					} catch (Exception e) {
						System.out.println("Problemi nella ricezione della risposta, i seguenti: ");
						e.printStackTrace();
						socket.close();
						System.out.print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti input: ");
						continue;
						// il client continua l'esecuzione riprendendo dall'inizio del ciclo
					}
				} else if (richiesta.equalsIgnoreCase("D")) {
					try {
						// Elaborazione richiesta e invio
						outSock.writeUTF(richiesta);
						System.out.println("Inviata richiesta: " + richiesta);
						// chiudo output della socket se non devo inviare altri paramentri al server ma
						// aspettarmi solo il risultato
//		      			socket.shutdownOutput();
//		      			System.out.println("Chiusura output della socket: " + socket);

						System.out.println("Inserisci ora attuale nel formato (hh:mm):");
						String hourString = stdIn.readLine();
						
						String splitString[] = hourString.split(":");
						
						try {
							hh = Integer.parseInt(splitString[0]);
							mm = Integer.parseInt(splitString[1]);
						} catch (NumberFormatException e) {
							e.printStackTrace();
							System.out.print(
									"\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti input, A per aggiungere un viaggio: ");
							continue;
						}
						
						//SCRIVO hh poi mm
						outSock.writeInt(hh);
						outSock.writeInt(mm);
						
						//QUA LA RICEZIONE DEI FILE
						
						//Numero totale dei file, nome, lunghezza, contenuto
						int nFile;
						nFile = inSock.readInt();
						
						System.out.println("Devo ricevere " + nFile + " audio di treni in arrivo nella prossima ora");
						
					} catch (Exception e) {
						System.out.println("Problemi nell'invio della richiesta " + richiesta);
						e.printStackTrace();
						System.out.print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti input: ");
						continue;
						// il client continua l'esecuzione riprendendo dall'inizio del ciclo
					}

					// TODO
				} else
					System.out.println("Errore nella scelta...");

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
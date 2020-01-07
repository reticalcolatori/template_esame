package model;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteOrder;

//Thread lanciato per ogni richiesta accettata
class TCP_ServerThread extends Thread {
	private Socket clientSocket = null;

	// COSTRUTTORE - va opportunamente creato
	public TCP_ServerThread(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

	public void run() {
		System.out.println("Info: Attivazione server thread. (Thread=" + getName() + ")");

		DataInputStream inSock;
		DataOutputStream outSock;

		String richiesta = null;
		String risposta = null;

		try {
			try {
				// creazione stream di input e out da socket
				inSock = new DataInputStream(clientSocket.getInputStream());
				outSock = new DataOutputStream(clientSocket.getOutputStream());
			} catch (IOException ioe) {
				System.out.println("Problemi nella creazione degli stream di input/output " + "su socket: ");
				ioe.printStackTrace();
				// il server continua l'esecuzione riprendendo dall'inizio del ciclo
				return;
			} catch (Exception e) {
				System.out.println("Problemi nella creazione degli stream di input/output " + "su socket: ");
				e.printStackTrace();
				// il server continua l'esecuzione riprendendo dall'inizio del ciclo
				return;
			}

			// Ricezione della richiesta
			richiesta = null;
			
			//RICEZIONE DELLA RICHIESTA FINO A EOF
			while((richiesta = inSock.readUTF()) != null) {


				// --------------------------------------------------------------------------------------------------------------------------
				// OPERAZIONE PER AGIGUNTA DI UN TRENO!

				// mi aspetto in ordine sottostante:
				/*
				 * 1) ID 2) TIPO 3) ORA ATTESA (nella forma hh:mm) 4) NOME FILE AUDIO (E POI
				 * RICEVE IL FILE IN QUESTO MODO IL CLIENT MANDA PRIMA NOME POI LUNGHEZZA DEL
				 * FILE IN BYTE, POI INVIA IL FILE EFFETTIVO)
				 * 
				 * RITORNA ESITO OP > 0 SE OK < 0 SE VIAGGIO CON STESSO ID GIÀ PRESENTE
				 * STRUTTURA DATI PIENA FALLISCONO I CONTROLLI SUI DATI IN INPUT
				 */
				if (richiesta.equalsIgnoreCase("A")) {
					// --------------------------INPUT-------------------------------------
					String id = null;
					String tipo = null;
					int hh, mm;
					String nomeFileAudioCorrente = null;
					long lunghezzaFileCorrente;

					// --------------------------OUTPUT------------------------------------
					int res = 0; // contiene le info da ritornare al cliente (< 0 se qualcosa non va)

					try {
						id = inSock.readUTF();
						tipo = inSock.readUTF();
						hh = inSock.readInt();
						mm = inSock.readInt();
						nomeFileAudioCorrente = inSock.readUTF();
						//TODO RICORDA DI AGGUNGERE LA LETTURA DELLA lunghezza del file
//						lunghezzaFileCorrente = inSock.readLong();
						
						System.out.println("RICEVUTO RICHIESTA: id = " + id + " tipo " + tipo + " hh:mm " + hh + ":" + mm + " nome audio: " + nomeFileAudioCorrente);
					} catch (SocketTimeoutException ste) {
						System.out.println("Timeout scattato: ");
						ste.printStackTrace();
						clientSocket.close();
						return;
					} catch (Exception e) {
						System.out.println("Problemi nella ricezione della richiesta: ");
						e.printStackTrace();
						return;
					}

					// VALORI DI RITORNO:
					/*
					 * 1) res == -1 --> falliscono i controlli dei dati in input 2) res == -2 --> id
					 * passato è già presente all'interno della struttura 3) res == -3 --> se
					 * struttura dati piena
					 */
					
					boolean goOn = true;

					if (id.isEmpty() || nomeFileAudioCorrente.isEmpty()) {
						res = -1;
						//outSock.writeInt(res);
						goOn = false;
						outSock.writeBoolean(goOn);
						outSock.writeInt(res);
					} else {
						if (tipo.isEmpty() || ((!tipo.equals("Partenza")) && (!tipo.equals("Arrivo")))) {
							res = -1;
							goOn = false;
							outSock.writeBoolean(goOn);
							outSock.writeInt(res);

						} else {
							
							outSock.writeBoolean(goOn);
							lunghezzaFileCorrente = inSock.readLong();

							System.out.println("Ricevuto lunghezza del file = " + lunghezzaFileCorrente);
							
							// VERIFICA presenza ID
							int isFull = -1;

							for (int i = 0; i < TCP_Server.orariTreni.length; i++) {
								if (id.equals(TCP_Server.orariTreni[i].getId())) {
									goOn = false;
									res = -2; // secondo codice di ritorno id duplicato
								}
								if (TCP_Server.orariTreni[i].getId().equals("L"))
									if (isFull == -1)
										isFull = i;
							}

							if (isFull == -1) // quando esco se ho trovato che la struttura dati è piena setto il valore di
												// ritorno all'apposito res
								res = -3;

							if (res < 0) { // se ci sono stati degli errori ritorno già il valore.
								goOn = false;
								outSock.writeBoolean(goOn);
								outSock.writeInt(res);
							} else {
								// LOGICA APPLICATIVA
								// Apertura del file
								//File myFile = new File(nomeFileAudioCorrente);
								
								//USO UN NOME DI FILE DIVERSO PER DEBUG
								File myFile = new File("provacopia.mp3");
								myFile.createNewFile();

								BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(myFile));

								int currByte = -1;
/*
 *NO POICHé READ E WRITE FUNZIONANO CORRETTAMENTE SOLO SE STAI LAVORANDO CON DEI FILE DI TESTO. 
 *
								while(lunghezzaFileCorrente > 0){
									int x;
									x = inSock.read();
									if(x == -1) break;
									writer.write(x);
									lunghezzaFileCorrente--;
								}
								System.out.println("Ricevuto file dal client");
*/								int count;
								byte x[] = new byte[255];
								while(lunghezzaFileCorrente > 0){
									
									count = inSock.read(x);
									//if(x == -1) break;
									writer.write(x, 0, count);
									lunghezzaFileCorrente -= count;
								}
								System.out.println("Ricevuto file dal client");
								
								
								writer.close();

								// lo aggiungo alla tabella sul server
								TCP_Server.orariTreni[isFull].setId(id);
								TCP_Server.orariTreni[isFull].setTipo(tipo);
								TCP_Server.orariTreni[isFull].setHh(hh);
								TCP_Server.orariTreni[isFull].setMm(mm);
								TCP_Server.orariTreni[isFull].setFileAudio(nomeFileAudioCorrente);

								res = 0;
								
								TCP_Server.stampa();

								try {
									outSock.writeInt(res);

								} catch (SocketTimeoutException ste) {
									System.out.println("Timeout scattato: ");
									ste.printStackTrace();
									clientSocket.close();
									System.out.print("\n^D(Unix)/^Z(Win)+invio per uscire, solo invio per continuare: ");
									// il client continua l'esecuzione riprendendo dall'inizio del ciclo
									return;
								} catch (Exception e) {
									System.err.println("\nProblemi nell'invio della risposta: " + e.getMessage());
									e.printStackTrace();
									clientSocket.close();
									System.out.println("Terminata connessione con " + clientSocket);
									return;
									// il server continua a fornire il servizio ricominciando
									// dall'inizio del ciclo esterno
								}

							}

						}
					}

				} else if (richiesta.equalsIgnoreCase("D")) {
					
					//DEVO LEGGERE hh:mm
					int hh, mm;
					int nTreniTraUnOra = 0;
					
					hh = inSock.readInt();
					mm = inSock.readInt();
					
					System.out.println("Ricevuto orario: " + hh + ":" + mm);
					
					//LOGICA APPLICATIVA: trovo i viaggi in ARRIVO tra un ora ritardo compreso.
					for (int i = 0; i < TCP_Server.orariTreni.length; i++) {
						//ricalcolo il tempo vero solo se settato il ritardo (ritardo > 0)
						if(TCP_Server.orariTreni[i].getTipo().equals("Arrivo") && TCP_Server.orariTreni[i].getRitardo() > 0) {
							//ho un ritardo che devo sommare
							
							int ritardo = TCP_Server.orariTreni[i].getRitardo();
							
							int oreDaSommare = ritardo / 60;
							System.out.println(" ore da sommare " + oreDaSommare);
							
							int mmDaSommare = ritardo % 60;
							System.out.println("mm da sommare " + mmDaSommare);
							
							int hhReali, mmReali;
							hhReali = TCP_Server.orariTreni[i].getHh() + oreDaSommare;
							mmReali = TCP_Server.orariTreni[i].getMm() + mmDaSommare;
							
							if(hhReali <= hh + 1 && hhReali >= hh) {
								nTreniTraUnOra++;
							}
								
						}
					}
					
					
					//poi invio al client numerofile, nome, lunghezza, contenuto
					
					/*
					
					// Richiedo ulteriori info.
					String direttorio = null;
					int soglia = 0;

					// Invio prima il numero di file da inviare.
					int nFiles = 0;

					try {
						direttorio = inSock.readUTF();
						soglia = inSock.readInt();
					} catch (SocketTimeoutException ste) {
						System.out.println("Timeout scattato: ");
						ste.printStackTrace();
						clientSocket.close();
						return;
					} catch (Exception e) {
						System.out.println("Problemi nella ricezione della richiesta: ");
						e.printStackTrace();
						return;
					}

					// Checks
					if (direttorio.isEmpty()) {
						risposta = "Direttorio non valido";
					} else {
						if (soglia < 0) {
							risposta = "Soglia >=0";
						} else {

							// Verifica disponiblità direttorio
							File myDir = new File(direttorio);

							if (myDir.isDirectory()) {
								File[] ls = myDir.listFiles();

								for (File element : ls) {
									if (element.isFile()) {
										if (element.canRead()) {
											if (element.length() >= soglia) {
												// posso inviarlo allo zio.
												nFiles++;
											}
										}
									}
								}

								risposta = "ok";

								try {
									outSock.writeUTF(risposta);
									outSock.writeInt(nFiles);

									for (File element : ls) {
										if (element.isFile()) {
											if (element.canRead()) {
												if (element.length() >= soglia) {
													// posso inviarlo allo zio.

													// Invio il file all'utente: nomeFile: dimensione : contenuto
													outSock.writeUTF(element.getName());
													outSock.writeLong(element.length());

													BufferedInputStream streamFile = new BufferedInputStream(
															new FileInputStream(element));

													int x = -1;

													// Restituisce -1 se EOF
													while ((x = streamFile.read()) != -1) {
														outSock.write(x);
													}

													streamFile.close();
												}
											}
										}
									}
								} catch (SocketTimeoutException ste) {
									System.out.println("Timeout scattato: ");
									ste.printStackTrace();
									clientSocket.close();
									System.out.print("\n^D(Unix)/^Z(Win)+invio per uscire, solo invio per continuare: ");
									// il client continua l'esecuzione riprendendo dall'inizio del ciclo
									return;
								} catch (Exception e) {
									System.err.println("\nProblemi nell'invio della risposta: " + e.getMessage());
									e.printStackTrace();
									clientSocket.close();
									System.out.println("Terminata connessione con " + clientSocket);
									return;
								}

							} else {
								risposta = "Direttorio non è direttorio";
							}
						}
					}

					try {
						outSock.writeUTF(risposta);
						clientSocket.shutdownOutput();
						System.out.println("Terminata connessione con " + clientSocket);
					} catch (SocketTimeoutException ste) {
						System.out.println("Timeout scattato: ");
						ste.printStackTrace();
						clientSocket.close();
						System.out.print("\n^D(Unix)/^Z(Win)+invio per uscire, solo invio per continuare: ");
						// il client continua l'esecuzione riprendendo dall'inizio del ciclo
						return;
					} catch (Exception e) {
						System.err.println("\nProblemi nell'invio della risposta: " + e.getMessage());
						e.printStackTrace();
						clientSocket.close();
						System.out.println("Terminata connessione con " + clientSocket);
						return;
					}
					
					*/
					
				} else {
					try {
						outSock.writeUTF("Comando non riconosciuto");
						clientSocket.shutdownOutput();
						System.out.println("Terminata connessione con " + clientSocket);
					} catch (SocketTimeoutException ste) {
						System.out.println("Timeout scattato: ");
						ste.printStackTrace();
						clientSocket.close();
						System.out.print("\n^D(Unix)/^Z(Win)+invio per uscire, solo invio per continuare: ");
						// il client continua l'esecuzione riprendendo dall'inizio del ciclo
						return;
					} catch (Exception e) {
						System.err.println("\nProblemi nell'invio della risposta: " + e.getMessage());
						e.printStackTrace();
						clientSocket.close();
						System.out.println("Terminata connessione con " + clientSocket);
						return;
					}
				}
			}
		}
		// qui catturo le eccezioni non catturate all'interno del while
		// in seguito alle quali il server termina l'esecuzione
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("Errore irreversibile, TCP_ServerThread: termino...");
			System.exit(3);
		}
	} // run

} // TCP_ServerThread

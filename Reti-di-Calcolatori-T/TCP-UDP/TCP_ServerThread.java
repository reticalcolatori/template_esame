
// Thread lanciato per ogni richiesta accettata
class TCP_ServerThread extends Thread
{
	private Socket clientSocket = null;

    //COSTRUTTORE - va opportunamente creato
	public TCP_ServerThread(Socket clientSocket) 
	{
		this.clientSocket = clientSocket;
	}

	public void run() 
	{
		System.out.println("Info: Attivazione server thread. (Thread=" + getName() + ")");
		DataInputStream inSock;
		DataOutputStream outSock;
		try 
		{
			try 
			{
				// creazione stream di input e out da socket
				inSock = new DataInputStream(clientSocket.getInputStream());
				outSock = new DataOutputStream(clientSocket.getOutputStream());
			}
			catch (IOException ioe) 
			{
				System.out.println("Problemi nella creazione degli stream di input/output "+ "su socket: ");
				ioe.printStackTrace();
				// il server continua l'esecuzione riprendendo dall'inizio del ciclo
				return;
			}
			catch (Exception e) 
			{
				System.out.println("Problemi nella creazione degli stream di input/output "+ "su socket: ");
				e.printStackTrace();
				// il server continua l'esecuzione riprendendo dall'inizio del ciclo
				return;
			}

	    	// Ricezione della richiesta
			String richiesta;

			//WHILE QUI SE FAI UNA CONNESSIONE PER SESSIONE!

			try 
			{
				richiesta = inSock.readUTF();
				if(richiesta == null)
				{
					System.out.println("EOF esco");
					clientSocket.shutdownOutput();
					clientSocket.shutdownInput();
					clientSocket.close();
					return;
				}
				System.out.println("Richiesta: " + richiesta);
			}
			catch(EOFException ex){
				System.out.println("EOF Ricevuto");
				clientSocket.shutdownInput();
				clientSocket.shutdownOutput();
				clientSocket.close();
				return;
			}
			catch(SocketTimeoutException ste)
			{
				System.out.println("Timeout scattato: ");
				ste.printStackTrace();
				clientSocket.shutdownOutput();
				clientSocket.shutdownInput();
				clientSocket.close();
				return;          
			}        
			catch (Exception e) 
			{
				System.out.println("Problemi nella ricezione della richiesta: ");
				e.printStackTrace();
				// servo nuove richieste
				return;
			}

			if(richiesta.equalsIgnoreCase("U"))
			{																
				String risposta = null;
	    		// Elaborazione e invio della risposta

				try
				{
					outSock.writeUTF(risposta);
					// clientSocket.shutdownOutput();
					// System.out.println("Terminata connessione con " + clientSocket);
				}
				catch(SocketTimeoutException ste)
				{
					System.out.println("Timeout scattato: ");
					ste.printStackTrace();
					clientSocket.shutdownOutput();
					clientSocket.shutdownInput();
					clientSocket.close();
					return;          
				}              
				catch (Exception e) 
				{
					System.err.println("\nProblemi nell'invio della risposta: "+ e.getMessage());
					e.printStackTrace();
					clientSocket.shutdownOutput();
					clientSocket.shutdownInput();
					clientSocket.close();
					return;
				}
			}
			else if(richiesta.equalsIgnoreCase("D"))
			{
				//....come primma
			} 												
		}
	// qui catturo le eccezioni non catturate all'interno del while
	// in seguito alle quali il server termina l'esecuzione
		catch (Exception e) 
		{
			e.printStackTrace();
			System.out.println("Errore irreversibile, TCP_ServerThread: termino...");
			System.exit(3);
		}
    } // run

} // TCP_ServerThread



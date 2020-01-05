package model;

import java.io.File;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.*;

public class RMI_Server extends UnicastRemoteObject implements RMI_interfaceFile {
	private static final long serialVersionUID = 1L;

	// qui eventuali variabili e strutture dati
	private static final int N_MAX_FILE = 25;
	
	// Costruttore
	public RMI_Server() throws RemoteException {
		super();
	}

	// Avvio del Server RMI
	public static void main(String[] args) {
		
		//creo e avvio il registry nella macchina dove risiede il server (devono risiedere sulla stessa macchina)
		try {
			LocateRegistry.createRegistry(1099);
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}
		
		int registryPort = 1099;
		String registryHost = "localhost";
		String serviceName = "ServerRMI";

		// Controllo dei parametri della riga di comando
		if (args.length != 0 && args.length != 1) {
			System.out.println("Sintassi: ServerRMI [REGISTRYPORT]");
			System.exit(1);
		}
		if (args.length == 1) {
			try {
				registryPort = Integer.parseInt(args[0]);
			} catch (Exception e) {
				System.out.println("Sintassi: ServerRMI [REGISTRYPORT], REGISTRYPORT intero");
				System.exit(2);
			}
		}

		// Registrazione del servizio
		String completeName = "//" + registryHost + ":" + registryPort + "/" + serviceName;

		try {
			RMI_Server serverRMI = new RMI_Server();
			Naming.rebind(completeName, serverRMI);
			System.out.println("Server RMI: Servizio \"" + serviceName + "\" registrato");
		} catch (Exception e) {
			System.err.println("Server RMI \"" + serviceName + "\": " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}

	
	// Implementazione dei metodi

	@Override
	public String[] lista_file(String dirName) throws RemoteException {
		
		System.out.println("Ricevuta richiesta per la directory " + dirName);
		File currDir = new File(dirName);
		
		if(!currDir.isDirectory()) {
			return null;
		}
		
		//se sono qua allora il path corrisponde ad una directory
		
		File filesFirstLevel[];
		File filesSecondLevel[];
		filesFirstLevel = currDir.listFiles();
		
		String listaFile[] = new String[filesFirstLevel.length];
		
		String vocali = "aeiouAEIOU";
		
		int pos = 0;
		
		for(int i = 0; i < filesFirstLevel.length; i++) { //per tutti gli elementi contenuti nella cartella di primo livello
			
			if(filesFirstLevel[i].isDirectory()) { //se item corrente è una directory ispeziona il suo contenuto
				
				System.out.println("Ispezione della directory " + filesFirstLevel[i].getName());
				
				filesSecondLevel = filesFirstLevel[i].listFiles(); //prendo tutti i file contenuti nella directory di secondo livello
				
				for(int k = 0; k < filesSecondLevel.length; k++) {
					
					if(filesSecondLevel[k].isFile()) {
						
						for(int z = 0; z < filesSecondLevel[k].getName().length() - 1; z++) {
							
							if((vocali.indexOf(filesSecondLevel[k].getName().charAt(z)) >= 0) && (vocali.indexOf(filesSecondLevel[k].getName().charAt(z + 1)) >= 0)) { //se io e il mio successivo siamo vocali allora aggiungimi
								listaFile[pos] = filesSecondLevel[k].getName();
								pos++;
								break;
							}
							
						}
						
					}
					
				}
				
			
				
			} else { //currFile è un file regolare, verifico se il suo nome contiene due vocali di fila
				
				System.out.println("Verifico se il file " + filesFirstLevel[i].getName() + " contiene due vocali consecutive");
				
				for(int j = 0; j < filesFirstLevel[i].getName().length() - 1; j++) {
					
					if((vocali.indexOf(filesFirstLevel[i].getName().charAt(j)) >= 0) && (vocali.indexOf(filesFirstLevel[i].getName().charAt(j + 1)) >= 0)) { //se io e il mio successivo siamo vocali allora aggiungimi
						listaFile[pos] = filesFirstLevel[i].getName();
						pos++;
						break;
					}
					
				}
				
			}
		}
		
		String res[] = new String[pos];
		for(int i = 0; i < pos; i++) {
			res[i] = listaFile[i];
		}
		
		return res;
	}

	@Override
	public int numerazione_righe(String fileName) throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
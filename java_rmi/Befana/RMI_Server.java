package model;

import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.*;

public class RMI_Server extends UnicastRemoteObject implements RMI_interfaceFile {
	private static final long serialVersionUID = 1L;

	// qui eventuali variabili e strutture dati
	private static final int N = 10;
	private Calza calze[] = null;

	// Costruttore
	public RMI_Server() throws RemoteException {
		super();
		calze = new Calza[N];

		for (int i = 4; i < calze.length; i++) {
			calze[i] = new Calza("L", "L" ,"L", "L", "L", 'L');
		}

		calze[0] = new Calza("MarioRossi1", "Normale", "Bologna", "Saragozza", "Bravo mario!", 'N');
		calze[1] = new Calza("MarioBianchi1", "Celiaco", "Roma", "Veneto", "Mario sei birichino", 'S');
		calze[2] = new Calza("MarioRossi12", "Normale", "Firenze", "Larga", "Mario comportati meglio!", 'S');
		calze[3] = new Calza("MariaRossi", "Normale", "Bologna", "Saragozza", "Porca maria!", 'N');

		stampa();

	}

	public void stampa() {
		System.out.println("Identificatore\tTipo\tCarbone\tCitta\tVia\tMessaggio\n");

		for (int i = 0; i < N; i++) {
			System.out.println(calze[i].getId() + "\t" + calze[i].getTipo() + "\t" + calze[i].getCarbone() + "\t" + calze[i].getCitta() + "\t" + calze[i].getVia()
					+ "\t" + calze[i].getMessaggio());
		}
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
	public String[] visualizza_lista() throws RemoteException {
		//ritornare una lista delle città dove saranno consegnate tutte le calze no RIPETIZIONI
		
		String resTmp[] = new String[N];
		
		boolean presente = false;
		int ind = 0;
		
		for (int i = 0; i < calze.length; i++) {
			for (int j = 0; j < resTmp.length; j++) {
				if(calze[i].getCitta().equals(resTmp[j])) {
					presente = true;
					break;
				}
			}
			
			if(!presente) {
				resTmp[ind] = calze[i].getCitta();
				ind++;
				presente = false;
			}
		}
		
		//ora in ind ho il numero esatto di città diverse
		String res[] = new String[ind];
		
		//non mi porto dall'altra parte dei riferimenti a null
		for(int i = 0; i < ind; i++) {
			res[i] = resTmp[i];
		}
		
		return res;
		
	}

	@Override
	public int visualizza_numero(String citta, String via) throws RemoteException {
		if(citta == null || via == null) {
			throw new RemoteException("Illegal argument");
		}
		
		int res = -1;
		
		if(citta.isBlank() || via.isBlank())
			return res;
		
		res = -1;
		
		for (int i = 0; i < calze.length; i++) {
			if((calze[i].getCitta().equals(citta)) && (calze[i].getVia().equals(via))) {
				if(res == -1)
					res = 0;
				res++;
			}
		}
		
		return res;
	}
		
}
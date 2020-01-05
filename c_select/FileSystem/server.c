//Innocenti Mattia 0000825046 NumeroCompito

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <signal.h>
#include <errno.h>
#include <fcntl.h>
#include <dirent.h>
#include <sys/types.h>
#include <sys/select.h>
#include <sys/wait.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <sys/stat.h>

/*** #DEFINE **/
#define DIM_BUFF 100
#define max(a,b) ((a) > (b) ? (a) : (b))

/*** STRUTTURA DA INVIARE ATTRAVERO LA SOCKET **/
//struttura di esempi del Client Datagram

//in questo esempio non occorre definire una struttura apposita in quanto la richiesta che proviene dal client datagram è un tipo calza
// GIÀ DEFINITA


/********************************************************/
// Eventuale struttura dati del server

#define LENGTH 50
#define LENGTH_MSG 256
#define N_ROW 10

typedef struct FileName
{
    char fileName[255];
} FileName;

/********************************************************/

void gestore(int signo)
{
	int stato;
	printf("esecuzione gestore di SIGCHLD\n");
	wait(&stato);
}

/********************************************************/

int main(int argc, char **argv)
{
	int listenfd, connfd, udpfd, nready,nread, maxfdp1; 
	char currDirettorio[LENGTH]; //tipi di risposte
    const int on = 1; //--> usata in setsockopt
    int len, nwrite, num, esito, port;
    int ris;
    struct sockaddr_in clientaddr, servaddr;
	struct hostent *hostTcp, *hostUdp;
    fd_set rset;
          
    /*** ----  Controllo argomenti ---- **/
    //server porta
    if(argc != 2) {
        printf("Error: %s port\n", argv[0]);
        exit(1);
    }

    // Controllo che la porta sia un intero
    num = 0;
    while(argv[1][num] != '\0') {
        if((argv[1][num] < '0') || (argv[1][num] > '9')) {
            printf("Argomento non intero\n");
            exit(2);	
        }
        num++;
    }
    
    // Controllo che la porta sia nel range
    port = atoi(argv[1]);
    if (port < 1024 || port > 65535) {
        printf("Error: porta non valida!\n");
        printf("1024 <= port <= 65535\n");
        exit(2);
    }
    printf("Server avviato\n");
    
    // Qui eventuale inizializzazione della struttura dati del server	
	
	//**  CREAZIONE SOCKET TCP **/
	listenfd = socket(AF_INET, SOCK_STREAM, 0);
	if (listenfd < 0) {
		perror("apertura socket TCP ");
		exit(1);
	}
	printf("Creata la socket TCP d'ascolto, fd=%d\n", listenfd);
	
      // **  INIZIALIZZAZIONE INDIRIZZO SERVER E BIND **/
	memset ((char *)&servaddr, 0, sizeof(servaddr));
	servaddr.sin_family = AF_INET;
	servaddr.sin_addr.s_addr = INADDR_ANY;
	servaddr.sin_port = htons(port);

	if (setsockopt(listenfd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0) {
		perror("set opzioni socket TCP");
		exit(2);    
	}
	printf("Set opzioni socket TCP ok\n");

	if (bind(listenfd,(struct sockaddr *) &servaddr, sizeof(servaddr)) < 0) {
		perror("bind socket TCP");
		exit(3);
	}
	printf("Bind socket TCP ok\n");
	
	if (listen(listenfd, 5) < 0) {
		perror("listen");
		exit(4);
	}
	printf("Listen ok\n");
	
    // CREAZIONE SOCKET UDP
	udpfd = socket(AF_INET, SOCK_DGRAM, 0);
	if(udpfd < 0) {
		perror("apertura socket UDP");
		exit(5);
	}
	printf("Creata la socket UDP, fd=%d\n", udpfd);

    // INIZIALIZZAZIONE INDIRIZZO SERVER E BIND
	memset ((char *)&servaddr, 0, sizeof(servaddr));
	servaddr.sin_family = AF_INET;
	servaddr.sin_addr.s_addr = INADDR_ANY;
	servaddr.sin_port = htons(port);

	if(setsockopt(udpfd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0){
		perror("set opzioni socket UDP");
		exit(6);
	}
	printf("Set opzioni socket UDP ok\n");

	if(bind(udpfd,(struct sockaddr *) &servaddr, sizeof(servaddr)) < 0){
		perror("bind socket UDP");
		exit(7);
	}
	printf("Bind socket UDP ok\n");
	
    // AGGANCIO GESTORE PER EVITARE FIGLI ZOMBIE
	signal(SIGCHLD, gestore);

    // PULIZIA E SETTAGGIO MASCHERA DEI FILE DESCRIPTOR
	FD_ZERO(&rset);
	maxfdp1 = max(listenfd, udpfd)+1;
	
	
    // CICLO DI RICEZIONE EVENTI DALLA SELECT
	for(;;) {
		FD_SET(listenfd, &rset);
		FD_SET(udpfd, &rset);
		
		if ((nready=select(maxfdp1, &rset, NULL, NULL, NULL)) < 0) {
			if (errno==EINTR) 
                continue;
			else {
				perror("select");
				exit(8);
			}
		}

//_______________________________________________________________________________________________________________________________________________________________________

	    //CASO 1: GESTIONE RICHIESTE DA SOCKET DATAGRAM

        //Eliminare tutte le occorrenze di caratteri consonanti presenti in un file di testo specificato dall'utente

        FileName fileEliminaOcc;
        int nOccEliminate = 0;

		if(FD_ISSET(udpfd,&rset)) {
            
			printf("Server: ricevuta richiesta UDP da client: \n");
			hostUdp = gethostbyaddr((char *) &clientaddr.sin_addr,sizeof(clientaddr.sin_addr), AF_INET);
			if (hostUdp == NULL) {
				printf("client host information not found\n");
			} else {
				printf("Operazione richiesta da: %s %i\n", hostUdp->h_name, (unsigned)ntohs(clientaddr.sin_port));
			}

			len = sizeof(struct sockaddr_in);
            
			if(recvfrom(udpfd, &fileEliminaOcc, sizeof(FileName), 0, (struct sockaddr *) &clientaddr, &len) < 0) {
				perror("Recvfrom");
				continue;
			}
			printf("Richiesto dal client elimina le occorrenze dal file %s\n", fileEliminaOcc.fileName);

            //verifico se file esiste e possiedo i diritti di scrittura
            int currFd;
            int fdTmpFile;
            char vowels[] = "AEIOUaeiou";


            if((currFd = open(fileEliminaOcc.fileName, O_RDWR)) < 0){
                nOccEliminate = -1;
            } else if((fdTmpFile = open("tmp.txt", O_RDWR | O_CREAT | O_TRUNC, 0644)) < 0){
                nOccEliminate = -1;
            }

            if(nOccEliminate != -1){
                //logica applicativa
                //ho creato il file tmp e ho già aperto il file in esame
                char c;
                while((read(currFd, &c, sizeof(char))) > 0){
                    if(strchr(vowels, c) != NULL) //sono effettivamente una vocale
                        if(write(fdTmpFile, &c, sizeof(char)) < 0){
                            nOccEliminate = -1;
                            break;
                        }
                    else { //sono un carattere consonante devo incrementare il conteggio delle consonanti eliminate
                        nOccEliminate++;
                    }
                }

                lseek(fdTmpFile, 0, SEEK_SET);
                close(currFd);

                currFd = open(fileEliminaOcc.fileName, O_WRONLY | O_TRUNC);
                while((read(fdTmpFile, &c, sizeof(char))) > 0){
                    if(write(currFd, &c, sizeof(char)) < 0){
                        nOccEliminate = -1;
                        break;
                    }
                }

                remove("tmp.txt");
                close(fdTmpFile);
                close(currFd);
            }

            //Invio la risposta al client
            if(sendto(udpfd, &nOccEliminate, sizeof(int), 0, (struct sockaddr *)&clientaddr, len) < 0)
            {
                perror("Errore sendto");
            //Se questo invio fallisce il client torna all'inizio del ciclo
                continue;
            }
			
			printf("Operazione conclusa.\n");
		}

//_______________________________________________________________________________________________________________________________________________________________________
		
        //CASO 2: GESTIONE RICHIESTE DA SOCKET STREAM

        //Invio di tutti i file che contengono almeno una vocale e almeno una consonante, all'interno di un direttorio specificato dall'utente
        //(solo 1° livello di ispezione)

		if(FD_ISSET(listenfd,&rset)) {
			len = sizeof(struct sockaddr_in);
			if((connfd = accept(listenfd, (struct sockaddr *) &clientaddr, &len)) < 0) {
				if(errno==EINTR)
					continue;
				else {
					perror("accept");
					exit(9);
				}
			}
			
            /* SCHEMA UN PROCESSO FIGLIO PER OGNI OPERAZIONE CLIENTE */
            //figlio
			if(fork() == 0) {

				close(listenfd);
				//chi mi fa richiesta
				hostTcp = gethostbyaddr((char *) &clientaddr.sin_addr,sizeof(clientaddr.sin_addr), AF_INET);
				printf("Dentro il figlio pid=%d\n", getpid());
				
                // CASO 1 (una socket per richiesta)--> logica applicativa del programma che si richiede <-- 
	            //Per una sola connessione decommentare il ciclo for
                for(;;) {
                    //fino a quando leggo il direttorio -> leggo il nome del direttorio
                    FileName file;

                    while((nread = read(connfd, &file, sizeof(FileName))) > 0) {
                        printf("\n%s nome della directory ricevuta\n", file.fileName);
                        //verifico che sia effettivamente una directory e ispeziono contenuto.

                        DIR *dirInput;

                        dirInput = opendir(file.fileName);
                        if(dirInput == NULL) {
                            printf("Unable to open the dir %s\n", file.fileName);
                            ris = 0;

                            //Scrivo ris
                            if((nwrite = write(connfd,&ris,sizeof(int))) < 0) {
                                perror("write");
                                exit(3);
                            }

                        } else {
                            //setto il numero dei file che rispettano le specifiche e che andrò a trasmettere al client
                            //scrivo nFileTx
                            //x ogni file -> nome, lunghezza, contenuto

                            struct dirent *currItem;

                            int pos = 0;
                            int consonante = -1;
                            int vocale = -1;
                            char currFileName[256];
                            int currLunghFileName = -1;

                            int nFileTx = 0;

                            //ritorna solo i primi possibili 6 che matchano con questa radice

                            //ciclo per contare il numero dei file
                            while((currItem = readdir(dirInput)) != NULL) {
                                if(currItem->d_type == 8) {
                                    strcpy(currFileName, currItem->d_name);
                                    currLunghFileName = strlen(currFileName);

                                    for(int i = 0; i < currLunghFileName; i++){
                                        /*
                                        se nome del file corrente contiene già cons = 0; vocale = 0; -> invio nome, lunghezza, contenuto
                                        */
                                        //CONTROLLO CARATTERE ATTUALE SE ALLA FINE CONS = 0 VOCALE = 0 ALLORA PROCEDO AD INVIARLO COME?
                                        //BREAK;
                                        //INVIO

                                        if((currFileName[i] >= 'a' && currFileName[i] <= 'z') || (currFileName[i] >= 'A' && currFileName[i] <= 'Z')){

                                            char vowels[] = "AEIOUaeiou";
                                            int vowelFound = 0;
                                            int consonantFound = 0;
                                            int idx = 0;
                                            struct stat currFile;

                                            while(currFileName[idx] != '\0' || !(vowelFound && consonantFound)) {
                                                if(strchr(vowels, currFileName[idx]) != NULL)
                                                    vowelFound = 1;
                                                else
                                                    consonantFound = 1;

                                                idx++;
                                            }

                                            if(vowelFound && consonantFound) {
                                                nFileTx++;
                                                break;
                                            }
                                        }
                                    } //for
                                    
                                }
                            }//fine primo while per contare il numero dei file da trasmettere
                            closedir(dirInput);


                            //Controllo il numero dei file da trasmettere e lo comunico già al client
                            printf("devo trasmettere al client %d\n", nFileTx);
                            if((write(connfd, &nFileTx, sizeof(int))) < 0){
                                perror("Write currfilename error");
                                exit(EXIT_FAILURE);
                            }

                            opendir(file.fileName);
                            while((currItem = readdir(dirInput)) != NULL) {
                                
                                if(currItem->d_type == 8) { //8 per regular file 4 per directory
                                    strcpy(currFileName, currItem->d_name);
                                    currLunghFileName = strlen(currFileName);

                                    for(int i = 0; i < currLunghFileName; i++){

                                        if((currFileName[i] >= 'a' && currFileName[i] <= 'z') || (currFileName[i] >= 'A' && currFileName[i] <= 'Z')){

                                            char vowels[] = "AEIOUaeiou";
                                            int vowelFound = 0;
                                            int consonantFound = 0;
                                            int idx = 0;
                                            struct stat currFile;

                                            while(currFileName[idx] != '\0' || !(vowelFound && consonantFound)) {
                                                if(strchr(vowels, currFileName[idx]) != NULL)
                                                    vowelFound = 1;
                                                else
                                                    consonantFound = 1;

                                                idx++;
                                            }

                                            if(vowelFound && consonantFound) {
                                                //nome, lunghezza, contenuto

                                                //invio il nome del file
                                                if((write(connfd, currFileName, strlen(currFileName)+1)) < 0){
                                                    perror("Write currfilename error");
                                                    exit(EXIT_FAILURE);
                                                }

                                                strcat(file.fileName, "/");

                                                strcat(file.fileName, currFileName);

                                                printf("%s\n", currFileName);

                                                //uso la system call stat per risalire alla lunghezza del file corrente
                                                if((stat(file.fileName, &currFile)) < 0){
                                                    perror("Stat currfilename error");
                                                    exit(EXIT_FAILURE);
                                                }
                                                
                                                //ricavo la lunghezza
                                                long currLength = (long) currFile.st_size;
                                                printf("lunghezza file %s = %ld\n", currFileName, currLength);

                                                //scrivo la lunghezza del file corrente
                                                if((write(connfd, &currLength, sizeof(long))) < 0){
                                                    perror("Write lunghezza file error");
                                                    exit(EXIT_FAILURE);
                                                }

                                                //apro il file corrente usando come filename: nomedirectory/nomefile
                                                int currFd;
                                                if((currFd = open(file.fileName, O_RDONLY)) < 0){
                                                    perror("File open error");
                                                    exit(EXIT_FAILURE);     
                                                }

                                                //mando il contenuto del file
                                                char currC;
                                                while(read(currFd, &currC, sizeof(char)) > 0){
                                                    if((write(connfd, &currC, sizeof(char))) < 0){
                                                        perror("File transfer error");
                                                        exit(EXIT_FAILURE);
                                                    }
                                                }

                                                close(currFd);
                                                break;
                                            }

                                        }

                                    } //for
                                } //if
                            } //while

                            closedir(dirInput);

                        } //else


                    } //while
                    
                }
				
				printf("Figlio %i: chiudo connessione e termino\n", getpid());
				close(connfd);
				exit(0);
	        }//fine figlio
	  
	    close(connfd);
      }//if listen

    } /* ciclo for della select */

    exit(EXIT_SUCCESS);

}//main
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>

#define LENGTH 50
#define LENGTH_FILE_NAME 20
#define N_ROW 10

typedef struct FileName
{
    char fileName[255];
} FileName;


int main(int argc, char *argv[])
{
    int sd, nread, port;
    char c, ok, nome_file[LENGTH_FILE_NAME];
    struct hostent *host;
    struct sockaddr_in serverAddress;  

    /* CONTROLLO ARGOMENTI ---------------------------------- */
    if(argc!=3)
    {
        printf("Error:%s serverAddress serverPort\n", argv[0]);
        exit(1);
    }
  
    printf("Client avviato\n");


    /* PREPARAZIONE INDIRIZZO SERVER ----------------------------- */
    memset((char *)&serverAddress, 0, sizeof(struct sockaddr_in));
    serverAddress.sin_family = AF_INET;

    host = gethostbyname(argv[1]);
    if (host == NULL)
    {
        printf("%s not found in /etc/hosts\n", argv[1]);
        exit(2);
    }

    //Controllo secondo argomento --> numero di porta intero
    nread = 0;
    while(argv[2][nread] != '\0')
    {
        if(argv[2][nread] < '0' || argv[2][nread] >'9')
        {
            printf("Secondo argomento non intero\n");
            exit(2);
        }
        nread++;
    }
    port = atoi(argv[2]);
    if (port < 1024 || port > 65535)
    {
        printf("Porta non corretta, range numerico sbagliato\n");
        exit(3);
    }

  
    serverAddress.sin_addr.s_addr=((struct in_addr*) (host->h_addr))->s_addr;
    serverAddress.sin_port = htons(atoi(argv[2]));


    /* CREAZIONE E CONNESSIONE SOCKET (BIND IMPLICITA) ----------------- */
    sd = socket(AF_INET, SOCK_STREAM, 0);
    if (sd < 0){
        perror("apertura socket ");
        exit(3);
    }
    printf("Creata la socket sd = %d\n", sd);

    if (connect(sd,(struct sockaddr *) &serverAddress, sizeof(struct sockaddr))<0) {
        perror("Errore in connect");
        exit(4);
    }
    printf("Connect ok\n");


    /* CORPO DEL CLIENT: */
    /* ciclo di accettazione di richieste di file ------- */

    printf("Richieste di file fino alla fine del file di input\n");
    printf("Qualsiasi tasto per procedere, EOF per terminare\n");
    printf("Inserisci la directory:\n");
    
    char consumer[256];
    char directory[LENGTH];
    char currFileName[256];
    long currFileLength;
    int nFileRx;
    FileName file;

    //leggo il tipo e appena entro verifico che sia o celiaco o normale

    while (gets(directory)) {
        
        strcpy(file.fileName, directory);

        if (write(sd, &(file), sizeof(FileName))<0) {
            perror("write");
            break;
        }
        printf("Richiesta inviata... \n");

        if (read(sd, &nFileRx, sizeof(int))<0) {
            perror("read");      
            break;
        }
        printf("Devo ricevere %d file dal server\n", nFileRx);

        for(int i = 0; i < nFileRx; i++){
            if (read(sd, currFileName, sizeof(currFileName))<0) {
                perror("read");      
                break;
            }
            printf("ricevuto nome del file corrente -> %s\n", currFileName);

            //Creo il file con il nome appena ricevuto e copio al suo interno tutto i lcontenuto che mi arriva dal server
            int fdCurrFile = open(currFileName, O_WRONLY | O_CREAT | O_TRUNC, 0644);

            if (read(sd, &currFileLength, sizeof(long))<0) {
                perror("read long");      
                break;
            }
            printf("ricevuto lunghezza del file corrente %ld\n", currFileLength);

            char currCh;
            for(long j = 0; j < currFileLength; j++){
                if(read(sd, &currCh, sizeof(char)) < 0) {
                    perror("File rx error");
                    exit(EXIT_FAILURE);
                }
                if(write(fdCurrFile, &currCh, sizeof(char)) < 0){
                    perror("Unable to transfer file from server");
                    exit(EXIT_FAILURE);
                }
            }

            //ricorda di chiudere il file che hai aperto
            close(fdCurrFile);
        }

        printf("Inserisci la directory:\n");

    }//while

    printf("\nClient Stream: termino...\n");
  
    // Chiusura socket
    close(sd);

    exit(0);
 
}
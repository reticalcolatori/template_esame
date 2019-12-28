#include <stdio.h>
#include <rpc/rpc.h>
#include <string.h>
#include <stdlib.h>
#include <sys/types.h>
#include <unistd.h>
#include <dirent.h>
#include <fcntl.h>
	
#include "RPC_xFile.h"


/*

// Eventuali strutture di supporto

#define TABLEDIM 10
#define LISTADIM 3

typedef struct
{
	char parolaChiave[LENGTH];
}Parola;

typedef struct
{
  	char titolo [LENGTH];
  	char nomeFile [LENGTH];
  	char partita [LENGTH];
  	int inizio;
  	int fine;
  	Parola parole [LISTADIM];
} Moviola;

static Moviola tabella[TABLEDIM];
static int inizializzato=0;

// Eventuali funzioni per la visualizzazione e inizializzazione di una struttura dati

void stampa()
{
	int i,j;
	
	printf("\nTitolo\tFile\tPartita\tInizio\tFine\t");
	for(j=0;j<LISTADIM;j++)
	{
		printf("Parola%d\t", j+1);
	}
	printf("\n");

	for(i=0;i<TABLEDIM;i++)
	{
		printf("\n%s\t%s\t%s\t%d\t%d\t", tabella[i].titolo, 
						tabella[i].nomeFile, 
						tabella[i].partita,
						tabella[i].inizio, 
						tabella[i].fine);
		for(j=0;j<LISTADIM;j++)
		{
			printf("%s\t", tabella[i].parole[j].parolaChiave);
		}
	}
}

void inizializza()
{
	int i,j;

	if(inizializzato==1)
		return;

	for(i=0;i<TABLEDIM;i++)
	{
		strcpy(tabella[i].titolo,"L");
		strcpy(tabella[i].nomeFile,"L");
		strcpy(tabella[i].partita,"L");
		tabella[i].inizio = -1;
		tabella[i].fine = -1;
		for(j=0;j<LISTADIM;j++)
		{
			strcpy(tabella[i].parole[j].parolaChiave,"L");
		}
	}

	strcpy(tabella[0].titolo,"mov1");
	strcpy(tabella[0].nomeFile,"1.avi");
	strcpy(tabella[0].partita,"14062010_Italia_Paraguay");
	tabella[0].inizio = 1800;
	tabella[0].fine = 1845;
	strcpy(tabella[0].parole[0].parolaChiave,"fallo");
	strcpy(tabella[0].parole[2].parolaChiave,"Gattuso");

	inizializzato = 1;

	stampa();
}

*/

int * conta_occorrenze_linea_1_svc(char **lineaRicerca, struct svc_req *sr){

    printf("Ricevuta richiesta per contare occorrenze della linea %s\n", *lineaRicerca);

	static int ris;
	ris = 0;

	// inizializza() qui richiamo l'inizializzazione della struttura dati se presente
	
	// Calcolo ris secondo la logica della funzione
	
	// stampa();

    DIR *currDir;

    char pwdPath[256];
    size_t size;

    getcwd(pwdPath, size);
    printf("Path del direttorio corrente = %s\n", pwdPath);

    currDir = opendir(pwdPath);
    if(currDir == NULL){
        printf("Unable to open dir %s\n", pwdPath);
        ris = -1;
        return (&ris);
    }


    struct dirent *currItem;

    int fdCurrFile = -1;

    while((currItem = readdir(currDir)) != NULL){
        printf("%s\n", currItem->d_name);

        if((strcmp(currItem->d_name, ".") != 0) && (strcmp(currItem->d_name, "..") != 0) && (currItem->d_type == 8)){ //== 8 costante per i file
            
            printf("Apro il file %s e verifico il numero di occorrenze di %s...\n", currItem->d_name, *lineaRicerca);

            if((fdCurrFile = open(currItem->d_name, O_RDONLY)) < 0) {
                ris = -1;
                return (&ris);
            }

            char currCh;
            char currLine[256];
            int pos = 0;

            while((read(fdCurrFile, &currCh, sizeof(char))) > 0){
                if(currCh == '\n'){ //allora confronto la linea attuale con quella da ricerca se uguali incremento il ris

                    currLine[pos] = '\0';

                    printf("Confornto la linea corrente -> %s\nCon la linea di rif -> %s\n", currLine, *lineaRicerca);

                    if(strcmp(currLine, *lineaRicerca) == 0){
                        ris++;
                    } else {
                        //non faccio niente
                    }

                    pos = 0;

                } else {
                    currLine[pos] = currCh;
                    pos++;
                }
            }

        }
    }

    /*

    DIR *currDir;
    struct dirent *currItem;

    // Verifico se esiste e ne possiedo i diritti
    currDir = opendir(nomeFile);

    if(currDir == NULL){
        printf("SERVER FIGLIO (%d): errore apertura direttorio %s\n", getpid(), nomeFile);
        // il file che mi hai passato o non esiste o non hai i diritti te lo comunico e rileggo
        strcpy(errorMsg, "Errore nella lettura del nome del file.\n");  
        if (write(fdConnect, errorMsg, strlen(errorMsg)) < 0) {
            perror("Errore scrittura su socket connessa");
            closedir(currDir);
            close(fdConnect);
        }
        write(fdConnect, &zero, sizeof(char));
        continue;
    }

    */
    
	
	return (&ris);
}

Output * lista_file_prefisso_1_svc(Input *input, struct svc_req *sr)
{
	static Output ris;

    for(int i = 0; i < 6; i++)
        strcpy(ris.files[i].name, "L");
    ris.res = -1;

    DIR *dirInput;

    dirInput = opendir(input->nomeDir);
    if(dirInput == NULL) {
        printf("Unable to open the dir %s\n", input->nomeDir);
        return (&ris);
    }

    struct dirent *currItem;

    int pos = 0;


    //ritorna solo i primi possibili 6 che matchano con questa radice

    while((currItem = readdir(dirInput)) != NULL){
        if((strncmp(currItem->d_name, input->prefix, strlen(input->prefix)) == 0)){
            if(pos == 6)
                break;

            strcpy(ris.files[pos].name, currItem->d_name);
            pos++;

            ris.res = 0;
        }
    }
	
	// inizializza() qui richiamo l'inizializzazione della struttura dati se presente
	
	// Calcolo ris secondo la logica della funzione
	
	// stampa();
	
	return (&ris);
}


#include <stdio.h>
#include <rpc/rpc.h>
#include <stdlib.h>
#include <string.h>

#include "RPC_xFile.h"

#define LENGTH 256

int main(int argc, char *argv[])
{
	CLIENT *cl;/*Gestore del trasporto*/
	char *server; /*Nome HOST*/
	
	int *ris;
	char input[LENGTH];

    //Paramentri da richiedere all'utente da passare come parametro per le RPC
	char *lineaRicerca;
    lineaRicerca = (char *) malloc(256*sizeof(char));

   // Operandi op;
	int numero;
	// Qui eventuali altre variabili di strutture utilizzate
	// Input in;
	// Aggiunta aggiunta;
	// Lista* lista;
	
	/********** CONTROLLI ***************/	
	if (argc != 2)
	{
		fprintf(stderr, "Usage: %s host\n", argv[0]);
		exit(1);
	}

	server = argv[1];

	// Possibilità di cambiare il protocollo di trasporto
	cl = clnt_create(server, FILEPROG, FILEVERS, "udp");
	if (cl == NULL)
	{
		clnt_pcreateerror(server);
		exit(1);
	}

	// Interazione utente
	printf("\nInserire: \n1 Conta occorrenze linea \n2 Conta file che iniziano con un prefisso \n ^D per terminare: ");

	while(gets(input))
	{
		if(strcmp(input, "1")!=0 && strcmp(input, "2")!=0)
		{
			printf("Servizio non disponibile!\n");
		}
		
		if(strcmp(input, "1") == 0)
		{ //conta le occorrenze di una linea
		    printf("\nScelta: conta occorrenze linea\n");

            printf("Inserisci la linea da ricercare: \n");
			gets(lineaRicerca);



		    // .....interagisco con l'utente per caricare la struttura dati da utilizzare
		    /*
		    		RICORDA COME SI FA LA MALLOC 

				   printf("Inserisci Primo Parametro: ");
				   gets(input);
				   op.primo_paramentro =(char*)malloc(strlen(input) + 1);
				   strcpy(op.primo_paramentro, input);
	

				RICORDA COME SI FA PER I NUMERI: 
				
				   printf("Inserisci Secondo parametro: ");
				   while (scanf("%i", &numero) != 1)
				   {
				      do
				      {
					      printf("%c ", c);
				      }while (c!= '\n');
				      printf("Inserisci Secondo Parametro: ");
				    }
				    op.secondo_paramentro = numero;
				    gets(input);
				
				OPPURE
				printf("Inserisci NUMERO: ");
				gets(input);
				numero = atoi(input);


		    */
		    // Invocazione remota dopo aver caricato la struttura dati
		    ris = conta_occorrenze_linea_1(&lineaRicerca, cl);

		    // Controllo del risultato
		    if(ris == NULL) 
		    { 
			    clnt_perror(cl, server); 
			    exit(1); 
		    }
		    //Eventuale errore di logica del programma
		    if(*ris == -1) {  
			    printf("Problemi nell'esecuzione della procedura remota\n");
		    }
		    //Tutto ok      	
		    else {   
			    printf("Numero occorrenze della linea %s = %i\n", lineaRicerca, *ris);           
		    }
		    printf("\nFunzione1 conclusa\n");
		}
		
		if(strcmp(input, "2") == 0)
		{ //conta i file che iniziano con un prefisso da chiedere all'utente
		    printf("\nScelta: conta files con un determinato prefisso\n");

		    // .....interagisco con l'utente per caricare la struttura dati da utilizzare
            Input input;
            Output *output;
            char dirPath[256];
            char prefix[10];

            printf("Inserisci la directory dove intendi eseguire la ricerca: \n");
			gets(dirPath);

            printf("Inserisci il prefisso da ricercare: \n");
			gets(prefix);

            strcpy(input.nomeDir, dirPath);
            strcpy(input.prefix, prefix);


		    // Invocazione remota dopo aver caricato la struttura dati
		    output = lista_file_prefisso_1(&input, cl);

		    // Controllo del risultato
		    if(output == NULL) 
		    { 
			    clnt_perror(cl, server); 
			    exit(1); 
		    }
		    //Eventuale errore di logica del programma
		    if(output->res == -1) 
		    {  
			    printf("Problemi nell'esecuzione\n");
		    }
		    //Tutto ok      	
		    else 
		    {   

			    for (int i = 0; i < 6; i++) {
                    printf("Posizione %d --> %s\n", i, output->files[i].name);
                }
                             
		    }
		    printf("\nFunzione2 conclusa\n");
		}

		printf("\nInserire: \n1 Conta occorrenze linea \n2 Conta file che iniziano con un prefisso \n ^D per terminare: ");
	}

    free(lineaRicerca);

	// Libero le risorse distruggendo il gestore di trasporto
	clnt_destroy(cl);
	printf("Fine Client \n");
}

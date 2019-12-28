/* Innocenti, Mattia, 0000825046 */
struct Input {
	char nomeDir[25];
	char prefix[10];
};

struct NomeFile {
    char name[25];
};

struct Output {
	int res;
	NomeFile files[6];
};


program FILEPROG
{
	version FILEVERS
	{
		int CONTA_OCCORRENZE_LINEA(string) = 1;
		Output LISTA_FILE_PREFISSO(Input) = 2;
	} = 1;
} = 0x20000013;
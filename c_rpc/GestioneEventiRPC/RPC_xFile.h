/*
 * Please do not edit this file.
 * It was generated using rpcgen.
 */

#ifndef _RPC_XFILE_H_RPCGEN
#define _RPC_XFILE_H_RPCGEN

#include <rpc/rpc.h>


#ifdef __cplusplus
extern "C" {
#endif


struct Inserimento {
	char descrizione[25];
	char tipo[10];
	int gg;
	int mm;
	int aaaa;
	char luogo[20];
	int disponibilita;
	int prezzo;
};
typedef struct Inserimento Inserimento;

struct Acquisto {
	char descrizione[25];
	int nBiglietti;
};
typedef struct Acquisto Acquisto;

#define FILEPROG 0x20000013
#define FILEVERS 1

#if defined(__STDC__) || defined(__cplusplus)
#define INSERIMENTO_EVENTO 1
extern  int * inserimento_evento_1(Inserimento *, CLIENT *);
extern  int * inserimento_evento_1_svc(Inserimento *, struct svc_req *);
#define ACQUISTA_BIGLIETTI 2
extern  int * acquista_biglietti_1(Acquisto *, CLIENT *);
extern  int * acquista_biglietti_1_svc(Acquisto *, struct svc_req *);
extern int fileprog_1_freeresult (SVCXPRT *, xdrproc_t, caddr_t);

#else /* K&R C */
#define INSERIMENTO_EVENTO 1
extern  int * inserimento_evento_1();
extern  int * inserimento_evento_1_svc();
#define ACQUISTA_BIGLIETTI 2
extern  int * acquista_biglietti_1();
extern  int * acquista_biglietti_1_svc();
extern int fileprog_1_freeresult ();
#endif /* K&R C */

/* the xdr functions */

#if defined(__STDC__) || defined(__cplusplus)
extern  bool_t xdr_Inserimento (XDR *, Inserimento*);
extern  bool_t xdr_Acquisto (XDR *, Acquisto*);

#else /* K&R C */
extern bool_t xdr_Inserimento ();
extern bool_t xdr_Acquisto ();

#endif /* K&R C */

#ifdef __cplusplus
}
#endif

#endif /* !_RPC_XFILE_H_RPCGEN */

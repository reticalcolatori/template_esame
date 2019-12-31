all:
	rcpgen RPC_xFile.x
	gcc RPC_Server.c RPC_xFile_svc.c RPC_xFile_xdr.c -o server
	gcc RPC_Client.c RPC_xFile_clnt.c RPC_xFile_xdr.c -o client
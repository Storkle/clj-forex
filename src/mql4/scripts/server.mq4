#include <socket.mqh>
#include <protocol.mqh>

#property show_inputs
extern int port=2007;
extern string ip_address="";
                
int listen_socket, msgsock;
int  start() {
   listen_socket = open_socket(port,ip_address);
   msgsock = start_server_loop("main",listen_socket);
   return(0);
}

int deinit() {
  sock_close(msgsock);
  sock_close(listen_socket);
  sock_cleanup();
  Print("DEINIT");
}
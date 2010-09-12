//+------------------------------------------------------------------+
//|                                                       server.mq4 |
//|                                                                  |
//|                                                                  |
//+------------------------------------------------------------------+
//THIS IS AN EA WHICH fixes a problem with using a script for communicatoin, namely you cant access the
//currency that the script is on appropriately (it offsets time somehow, maybe i can fix this?, maybe its not being updated
//or something?) On the other hand, while an ea works, it only works during trading hours (i think, maybe can change this by default?)
#property copyright ""
#property link      ""

#include <socket.mqh>
#include <protocol.mqh>

#property show_inputs
extern int port=2007;
extern string ip_address="";
                
                
//+------------------------------------------------------------------+
//| expert initialization function                                   |
//+------------------------------------------------------------------+
int listen_socket, msgsock;
int init()
  {
//----
   return(0);
  }
//+------------------------------------------------------------------+
//| expert deinitialization function                                 |
//+------------------------------------------------------------------+
int deinit()
  {
//----
     sock_close(msgsock);
     sock_close(listen_socket);
     sock_cleanup();
//----
   return(0);
  }
//+------------------------------------------------------------------+
//| expert start function                                            |
//+------------------------------------------------------------------+
int start()
  {
   listen_socket = open_socket(port,ip_address);
   msgsock = start_server_loop("main",listen_socket);
   return(0);
  }
//+------------------------------------------------------------------+
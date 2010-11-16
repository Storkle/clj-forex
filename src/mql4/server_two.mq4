
//disconnecting is tricky business. it can result in you having to restart the whole platfrom just because you left a stupid socket open
//this will only happen if you attempt to force the quit of one of the server eas. Heres how you quit properly: you have to connect or already be connected.
//the you have to remove from chart and only then disconnect() from clojure. Hopefully you wont get a bind() failed error!

//and do NOT compile the ea while any instance is running. it will screw things up (i think)
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
#include <protocol_two.mqh>

#property show_inputs
extern int port=2007;
extern string ip_address="";
                
                
//+------------------------------------------------------------------+
//| expert initialization function                                   |
//+------------------------------------------------------------------+
int listen_socket;
int init()
{
  //----
  return(0);
}
//+------------------------------------------------------------------+
//| expert deinitialization function                                 |
//+------------------------------------------------------------------+
int clean = 0;
int deinit()
{
  //----
  if (clean==0) {
  sock_close(msgsock);
  sock_close(listen_socket);
  sock_cleanup();
  //----
  }
  return(0);
}
//+------------------------------------------------------------------+
//| expert start function                                            |
//+------------------------------------------------------------------+
int start()
{
  clean = 0;
  listen_socket = open_socket(port,ip_address);
  if (errno()!=0)  {
    return(0);
    }
  start_server_loop("main",listen_socket);
  if (clean==0) {
  sock_close(listen_socket); sock_close(msgsock);
  sock_cleanup();
  }
  clean = 1;
  return(0);
}
//+------------------------------------------------------------------+
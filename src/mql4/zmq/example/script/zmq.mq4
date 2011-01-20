#include <zmq_bind.mqh>


//+------------------------------------------------------------------+
//| expert initialization function                                   |
//+------------------------------------------------------------------+
int init()
{
  return(0);
}
//+------------------------------------------------------------------+
//| expert deinitialization function                                 |
//+------------------------------------------------------------------+
int client,server,context;
//TODO: the below is needed or else you might get some nice memory leaks!
int deinit()
{
  z_close(client);
  z_close(server);
  z_term(context);
  return(0);
}
//+------------------------------------------------------------------+
//| expert start function                                            |
//+------------------------------------------------------------------+

int start()
{
  Print("using zeromq version "+z_version_string());
  
  context = z_init(1);
  client = z_socket(context,ZMQ_REQ); //server: receives first
  server = z_socket(context,ZMQ_REP); //client: sends first 

  if(z_bind(server,"tcp://127.0.0.1:2027")==-1)
    return(-1);  
  if (z_connect(client,"tcp://127.0.0.1:2027")==-1)
    return(-1); 
  z_send(client,"I am a message");
  Print("message received is " +z_recv(server));

  return(0);
}
//+------------------------------------------------------------------+


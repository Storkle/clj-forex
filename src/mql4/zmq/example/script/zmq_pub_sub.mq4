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
//NOTICE: it doesn matter which end binds or which ends connect. in general, the most stable end should bind since only one socket can bind.
int start()
{
  Print("using zeromq version "+z_version_string());
  
  context = z_init(1);
  client = z_socket(context,ZMQ_PUB); //client: sends queries 
  server = z_socket(context,ZMQ_SUB); //server: receives queries
  z_subscribe(server,"cat");
 
  if(z_bind(server,"tcp://127.0.0.1:2027")==-1) 
    return(-1);  
  if(z_connect(client,"tcp://127.0.0.1:2027")==-1)
    return(-1); 
    
  z_send(client,"cat I am a message");
  string message = z_recv(server);
  Print("test1: message received is "+message);
  
  z_unsubscribe(server,"cat");
  z_send(client,"cat i am a message");
  Print("test2: received "+z_recv(server,ZMQ_NOBLOCK));
  
  z_subscribe(server,"");
  z_send(client,"cat i am a message");
  Print("test3: received "+z_recv(server));
  return(0);
}
//+------------------------------------------------------------------+


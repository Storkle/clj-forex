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
int recv;

//TODO: the below is needed or else you might get some nice memory leaks!
int deinit()
{
  z_close(client);
  z_close(server);
  z_msg_close(recv);
  z_term(context);
  return(0);
}
//+------------------------------------------------------------------+
//| expert start function                                            |
//+------------------------------------------------------------------+

int start()
{
  Print("using zeromq version "+z_version_string());
  recv = z_msg_empty();
   
  context = z_init(1);
  client = z_socket(context,ZMQ_PUB); //client: sends queries 
  server = z_socket(context,ZMQ_SUB); //server: receives queries
  z_subscribe(server,"cat");
  //z_set_sockopt(server,ZMQ_SUBSCRIBE,"",0);
  
  if(z_bind(server,"tcp://127.0.0.1:2027")==-1) 
    return(-1);  
  if(z_connect(client,"tcp://127.0.0.1:2027")==-1)
    return(-1); 
  z_send(client,"cat I am a message");
  z_recv(server,recv);
  
  string message = z_msg(recv);
  Print("message received is "+message +" and length is "+z_msg_len(recv)+" and "+StringLen(message));
  return(0);
}
//+------------------------------------------------------------------+


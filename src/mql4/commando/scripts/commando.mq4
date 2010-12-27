//+------------------------------------------------------------------+
//|                                                     commando.mq4 |
//|                                                                  |
//|                                                                  |
//+------------------------------------------------------------------+
#include <zmq_bind.mqh>
#include <utils.mqh>
#include <PROTOCOL.mqh>

//+------------------------------------------------------------------+
//| script program start function                                    |
//+------------------------------------------------------------------+
int pull_port = 2070; 
int push_port = 2065;

int context;
int msg;
int poll; 
int pull,push;  

int deinit() {
 trace("deinitializing");
 z_term(context); 
 z_close(pull); 
 z_close(push); 
 z_msg_close(msg);
 return(0);
}

int connect () {
  context = z_init(1);
  pull = z_socket(context,ZMQ_PULL);
  push = z_socket(context,ZMQ_PUSH);
  msg = z_msg_empty();     
  Print("Attempting to bind to port "+push_port);
  if(z_bind(push,"tcp://127.0.0.1:"+push_port)==-1) 
    return(-1); 
  Print("Attempting to connect to port "+pull_port);
  if(z_connect(pull,"tcp://127.0.0.1:"+pull_port)==-1) 
    return(-1); 
  poll = z_new_poll(pull);
  return(0);
}
   
int loop () {
  while(true) {
    Print("waiting for receive...");
    while(z_poll(poll,100)==0) {
      if (IsStopped())
        return(0);
    }
    z_recv(pull,msg);  
    RefreshRates();     
    string request[];
    split(request,z_msg(msg));
    string ret = protocol(request);
    Print("sending message ...");
    z_send(push,ret); 
    Print("done!");
  }
  return(0);
} 

int start()
  {
    if (connect()==-1) {
      return(0); 
    }  
   loop();
   return(0);
  }
//+------------------------------------------------------------------+
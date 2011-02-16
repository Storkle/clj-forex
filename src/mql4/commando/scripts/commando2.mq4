#include <zmq_bind.mqh>
#include <utils.mqh>
#include <PROCESS.mqh>

extern int push_port = 3010;
extern int recv_port = 3005;
 
int context;
int poll; 
int push,recv; 

int connect () {
  context = z_init(1);
  recv = z_socket(context,ZMQ_XREQ); 
  push = z_socket(context,ZMQ_PUSH);    
  Print("Attempting to connect to recv port "+recv_port);
  if(z_connect(recv,"tcp://127.0.0.1:"+recv_port)==-1)  
    return(-1); 
  Print("connected!");
  Print("Attempting to connect to push port "+push_port);
  if (z_connect(push,"tcp://127.0.0.1:"+push_port)==-1)
    return(-1);
  Print("connected!");
  poll = z_new_poll(recv);
  return(0); 
}



int deinit() {
 trace("deinitializing");  
 z_close(push); 
 z_close(recv);
 z_term(context); 
 recv=0;push=0;context=0;
 ObjectDelete("label");
 WindowRedraw();
 return(0);
}

//send uuid of this node plus its symbol and period
void register () {
 z_send(recv,"",ZMQ_SNDMORE);
 z_send(recv,Symbol(),ZMQ_SNDMORE);
 z_send(recv,""+Period(),ZMQ_NOBLOCK);
}

int loop () { 
  Print("Entering Commando2 Loop");
  while(true) { 
    register();  
    string request = ""; 
    string command[];
    while(true) {
      if (z_poll(poll,1000)>0) {
        Print("RECEIVED!"); 
        z_recv(recv);
        for (int i=0;i<z_more(recv);i++) {
          request = z_recv(recv);
          Print("GOT "+request);
        } 
        break;
       }
      if (IsStopped())
        return(0);
     }        
    split(command,request);
    int ret = process(push,command);
    if(ret==-1)
      return(0);
    GetLastError();
    RefreshRates();
  }
  return(0);
} 


int start()
  {
  ObjectCreate("label", OBJ_LABEL, 0, 0, 0);
  ObjectSet("label", OBJPROP_XDISTANCE, 0);
  ObjectSet("label", OBJPROP_YDISTANCE, 10);
  ObjectSetText("label", "COMMANDO2", 10, "Times New Roman", Blue);
  WindowRedraw();
   if (connect()==-1) {
      return(-1); 
    }  
   loop();
   return(0);
  }
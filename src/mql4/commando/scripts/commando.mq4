//TODO: when you close a node, and then redo it - zeromq will still attempt to send it to a non existent node! yikes!

#include <utils.mqh>
#include <PROCESS.mqh>

extern int push_port = 3010;
extern int recv_port = 3005;
extern bool allow_stop = true;

int context;
int poll; 
int push,recv; 

int connect () { 
  context = z_init(1);
  recv = z_socket(context,ZMQ_PULL); 
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
 Print("deinitializing and cleaning up sockets");  
 z_close(push); 
 z_close(recv);
 z_term(context);
 recv=0;push=0;context=0;
 ObjectDelete("label");
 WindowRedraw();
 return(0);
}

string z_recv_string_multi (string & array[],int recv) {
  ArrayResize(array,100);
  int i=0;
  int size=100;  
  string idd = z_recv(recv);
  while (z_more(recv)) {
    if (i==size-1) {
      ArrayResize(array,size*2);
      size=size*2;
    }
    array[i]= z_recv(recv);
    i=i+1;
  }
  ArrayResize(array,i); 
  return(idd); 
}


int loop () { 
  Print("Entering Commando Loop");
  bool return_it = false;
  while(true) {   
    string command[];
    string requests[];
    
    while(true) {
      if (z_poll(poll,1000)>0) {
          id = z_recv_string_multi(requests,recv);
          break;
      }
      if (IsStopped() && allow_stop==true)
        return(0);
      
     } 
    int size = ArraySize(requests);
    Print("got size "+size+" and id "+id);
    for (int i=0;i<size;i++) {
       if (i==size-1) 
          flag = ZMQ_NOBLOCK; //TOOD: noblock - will it work if it is still connected?
       else
          flag = ZMQ_SNDMORE;
       split(command,requests[i]);    
       if(process(push,command)==-1)
         return_it = true;
    }
     
    if (return_it==true) 
      return(0);
    GetLastError();
    RefreshRates();
    id="";
  }
  return(0);
} 


int start()
{
  ObjectCreate("label", OBJ_LABEL, 0, 0, 0);
  ObjectSet("label", OBJPROP_XDISTANCE, 2);
  ObjectSet("label", OBJPROP_YDISTANCE, 10);
  ObjectSetText("label", "COMMANDO v2", 10, "Times New Roman", Blue);
  WindowRedraw();
   if (connect()==-1) {
      return(-1); 
    }  
   loop();
   return(0);
}
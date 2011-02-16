#include <zmq_bind.mqh>

void send_error(int socket, int id,int err) { 
  z_send(socket,StrToInteger(id),ZMQ_SNDMORE);
  z_send(socket,"error",ZMQ_SNDMORE);
  z_send(socket,err);
}
void send_array(int socket, int id,double array[]) {
  z_send(socket,StrToInteger(id),ZMQ_SNDMORE);
  z_send(socket,"double[]",ZMQ_SNDMORE);
  z_send_doubles(socket,array);
}
void send_string(int socket, int id,string item) {
  z_send(socket,StrToInteger(id),ZMQ_SNDMORE); 
  z_send(socket,"string",ZMQ_SNDMORE);
  z_send(socket,item);
}
void send_global(int socket, int id,string item) {
  z_send(socket,StrToInteger(id),ZMQ_SNDMORE); 
  z_send(socket,"global",ZMQ_SNDMORE);
  z_send(socket,item);
}
void send_identity(int socket,int id) {
  send_global(socket,id,Symbol()+" "+Period()); 
}

void process_iClose (int out,int id, string request[]) {
  string symbol = request[2];
  int timeframe = StrToInteger(request[3]);
  int shift = StrToInteger(request[4]);
  int amount = StrToInteger(request[5]); 

  string ret=""; 
  double array[];
  ArrayResize(array,amount);
  
  int j=0;
  for (int i=shift+amount-1;i>=0;i--) {
    array[j] = iClose(symbol,timeframe,i); 
    j+=1;
  }
  int err = GetLastError(); 
  if (err!=0) 
    send_error(out,id,err);
  send_array(out,id,array);
 }
 
void process_AccountBalance(int out, int id, string request[]) {
  send_string(out,id,AccountBalance());
}

int process (int out,string request[]) {
    string command = request[1];
    string id = request[0];   
    if (command=="NA") {
      return(0);  
    } else if (command=="KILL") {
      send_identity(out,id);
      return(-1);
    } else if (command=="PING") {
      send_identity(out,id);
    } else if (command=="iClose") {
      process_iClose(out,id,request);
    } else if(command=="AccountBalance") {
      process_AccountBalance(out,id,request);
    } else { 
      send_error(out,id,-1);
    }
    return(0);
 }
 
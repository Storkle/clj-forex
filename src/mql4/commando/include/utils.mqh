#include <zmq_bind.mqh>

int socket;
string id; 
void send_error(int err,int flags=ZMQ_NOBLOCK) { 
  z_send(socket,err,ZMQ_SNDMORE);
  z_send(socket,"error",ZMQ_SNDMORE);
  z_send(socket,StrToInteger(id),flags); 
}
void send_array(double array[],int flags=ZMQ_NOBLOCK) {
  z_send_doubles(socket,array,ZMQ_SNDMORE);
  z_send(socket,"double[]",ZMQ_SNDMORE);
  z_send(socket,StrToInteger(id),flags); 
}
void send_string(string item,int flags=ZMQ_NOBLOCK) {
  z_send(socket,item,ZMQ_SNDMORE);
  z_send(socket,"string",ZMQ_SNDMORE);
  z_send(socket,StrToInteger(id),flags); 
}
void send_int(string item,int flags=ZMQ_NOBLOCK) {
  z_send(socket,item,ZMQ_SNDMORE);
  z_send(socket,"int",ZMQ_SNDMORE);
  z_send(socket,StrToInteger(id),flags); 
}
void send_boolean(string item,int flags=ZMQ_NOBLOCK) {
  z_send(socket,item,ZMQ_SNDMORE);
  z_send(socket,"boolean",ZMQ_SNDMORE);
  z_send(socket,StrToInteger(id),flags); 
}
void send_long(string item,int flags=ZMQ_NOBLOCK) {
  z_send(socket,item,ZMQ_SNDMORE);
  z_send(socket,"long",ZMQ_SNDMORE);
  z_send(socket,StrToInteger(id),flags); 
}
void send_double(string item,int flags=ZMQ_NOBLOCK) {
  z_send(socket,item,ZMQ_SNDMORE);
  z_send(socket,"double",ZMQ_SNDMORE);
  z_send(socket,StrToInteger(id),flags); 
}
void send_global(string item,int flags=ZMQ_NOBLOCK) {
  z_send(socket,item,ZMQ_SNDMORE);
  z_send(socket,"global",ZMQ_SNDMORE);
  z_send(socket,StrToInteger(id),flags); 
}
void send_identity() {
  send_global(Symbol()+" "+Period()); 
}

void send_unknown() {
 send_error(-1);
}

int DEBUG_ON = 0;
void trace(string s) {
 if (DEBUG_ON==1) 
   Print(s);
}

void split(string& arr[], string str) 
{
  string sym = " ";
  ArrayResize(arr, 0);
  string item;
  int pos, size;
  
  int len = StringLen(str);
  for (int i=0; i < len;) {
    pos = StringFind(str, sym, i);
    if (pos == -1) pos = len;
    
    item = StringSubstr(str, i, pos-i);
    item = StringTrimLeft(item);
    item = StringTrimRight(item);
    
    size = ArraySize(arr);
    ArrayResize(arr, size+1);
    arr[size] = item;
    
    i = pos+1;
  }
}
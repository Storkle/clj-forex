#include <zmq_bind.mqh>

int socket;
string id; 
int flag = ZMQ_NOBLOCK;

void snd_id() {
  if (flag!=ZMQ_SNDMORE) {
    z_send(socket,id,flag); 
  }
}


void send_error(int err) { 
  z_send(socket,err,ZMQ_SNDMORE);
  z_send(socket,"error",ZMQ_SNDMORE);
  snd_id(); 
}
void send_array(double array[]) {
  z_send_double_array(socket,array,ZMQ_SNDMORE);
  z_send(socket,"double[]",ZMQ_SNDMORE);
  snd_id(); 
}
void send_string(string item) {
  z_send(socket,item,ZMQ_SNDMORE);
  z_send(socket,"string",ZMQ_SNDMORE);
  snd_id(); 
}
void send_int(string item) {
  z_send(socket,item,ZMQ_SNDMORE);
  z_send(socket,"int",ZMQ_SNDMORE);
  snd_id();
}
void send_boolean(string item) {
  z_send(socket,item,ZMQ_SNDMORE);
  z_send(socket,"boolean",ZMQ_SNDMORE);
  snd_id();
}
void send_long(string item) {
  z_send(socket,item,ZMQ_SNDMORE);
  z_send(socket,"long",ZMQ_SNDMORE);
  snd_id();
}
void send_double(string item) {
  z_send(socket,item,ZMQ_SNDMORE);
  z_send(socket,"double",ZMQ_SNDMORE);
  snd_id();
}
void send_global(string item) {
  z_send(socket,item,ZMQ_SNDMORE);
  z_send(socket,"global",ZMQ_SNDMORE);
  snd_id();
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
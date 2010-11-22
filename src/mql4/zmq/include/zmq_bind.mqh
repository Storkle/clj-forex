#include <zmq_native.mqh>
int Z_DEBUG = 0;

void z_trace(string str) {
 if (Z_DEBUG==1) {
   Print(str);
 }
}

//version
string z_version_string() {
  int version[3];
  z_version(version);
  return(version[0]+"."+version[1]+"."+version[2]);
}
void z_version(int&version[]) {
  z_trace("z_version");
  int major[1];int minor[1];int patch[1];
  zmq_version(major,minor,patch);
  version[0]=major[0]; version[1] = minor[0]; version[2] = patch[0];
}

//messages
int z_msg_empty() {
 return(z_msg_new(""));
}

int z_msg_new (string data) {
  z_trace("z_msg_new: "+data);

  int msg = zmsg_new(); 
  int ret = zmq_msg_init_data(msg,data,StringLen(data));
  if (ret==-1) 
    z_error();
  return(msg);
}

string z_msg(int msg) { //TODO: catch error if zmq_msg_data doesnt work out?
  z_trace("z_msg"); 
  return(zmq_msg_data(msg));
}

int z_msg_len(int msg) {
  z_trace("z_msg_size");
  int ret = zmq_msg_size(msg); 
  return(ret);
}

int z_msg_close(int msg) {
  z_trace("z_msg_close");
  if (msg==0) 
   return(0);
  int ret = zmq_msg_close(msg); 
  if (ret==-1)
    z_error();
  return(ret);
}
//sockets
int z_socket(int context,int type) {
 z_trace("z_socket: "+context+" "+type);
 int ret = zmq_socket(context,type); 
 if (ret==-1)
   z_error();
 return(ret);
}
int z_close(int socket) {
  z_trace("z_close");
  if (socket==0) {
   return(0);
  }
  int ret = zmq_close(socket); 
  if (ret==-1)
   z_error();
  return(ret);
}
int z_bind (int socket,string endpoint) { //for the servers
  z_trace("z_bind: "+endpoint); 
  int ret = zmq_bind(socket,endpoint); 
  if (ret==-1)
   z_error();
  return(ret);
}
int z_connect(int socket,string endpoint) { //for the clients
 z_trace("z_connect: "+endpoint);
 int ret = zmq_connect(socket,endpoint); 
 if (ret==-1) 
   z_error();
 return(ret);
}

int z_send(int socket,int msg,int flags) {
 z_trace("z_send: "+msg+" "+flags); 
 int ret = zmq_send(socket,msg,flags); 
 if (ret==-1)
   z_error(); 
 return(ret);
}
int z_recv(int socket,int msg,int flags) {
 z_trace("z_recv: "+msg+" "+flags); 
 int ret = zmq_recv(socket,msg,flags); 
 if (ret==-1)
   z_error();
 return(ret);
}

//contextes


int z_init(int io_threads) {
  z_trace("z_init: "+io_threads);
  int ret = zmq_init(io_threads);
  if (ret==NULL) //TODO: is null equal to zero?
   z_error();  
  return(ret);
}

int z_term(int context) {
 z_trace("z_term");
 if(context==NULL) 
   return(0);
 int ret = zmq_term(context); 
 if (ret==-1) 
  z_error();
 return(ret);
}

//errors

//note: smq_error will return invalid messages if you call it by itself - in other words, zmq handles various system errors related
//to sockets and zmq_errno() will return errno which will return those system errors. I got 'error 11: resource temporarily unavailable' alot
//before i figured this out.
int z_error () {
  int err = zmq_errno(); 
  if (err!=0) {
    Print("ZMQ zmq_error "+err+": "+zmq_strerror(err));
  }
  return(err);
}


#include <zmq_native.mqh>
int Z_DEBUG = 0;

void z_trace(string str) {
 if (Z_DEBUG==1) {
   Print(str);
 }
}

//timeout in milliseconds
int z_poll(int poller,int timeout) {
  int ret = _zmq_poll(poller,1,timeout);
  if (ret==-1) 
     z_error();
  return(ret);
}
int z_new_poll (int socket)  {
  return(_zmq_new_poll(socket));
}

/*TODO: how do we actually freee a pointer in memory - i tried it with free(ptr) , and it crashed :(
void z_free_poller (int poller) {
  if (poller!=0) 
    _zmq_free(poller);
}
*/


//version
string z_version_string() {
  int version[3];
  z_version(version);
  return(version[0]+"."+version[1]+"."+version[2]);
}
void z_version(int&version[]) {
  z_trace("z_version");
  int major[1];int minor[1];int patch[1];
  _zmq_version(major,minor,patch);
  version[0]=major[0]; version[1] = minor[0]; version[2] = patch[0];
}

//messages
int z_msg_empty() {
 return(z_msg_new(""));
}

int z_msg_new (string data) {
  z_trace("z_msg_new: "+data);

  int msg = _zmsg_new(); 
  int ret = _zmq_msg_init_data(msg,data,StringLen(data));
  if (ret==-1) 
    z_error();
  return(msg);
}

string z_msg(int msg) { //TODO: catch error if zmq_msg_data doesnt work out?
  z_trace("z_msg"); 
  return(_zmq_msg_data(msg));
}

int z_msg_len(int msg) {
  z_trace("z_msg_size");
  int ret = _zmq_msg_size(msg); 
  return(ret);
}

int z_msg_close(int msg) {
  z_trace("z_msg_close");
  if (msg==0) 
   return(0);
  int ret = _zmq_msg_close(msg); 
  if (ret==-1)
    z_error();
  return(ret);
}
//sockets
int z_subscribe (int socket,string val) {
  z_set_sockopt(socket,ZMQ_SUBSCRIBE,val);
}
int z_unsubscribe(int socket,string val) {
  z_set_sockopt(socket,ZMQ_UNSUBSCRIBE,val);
}

int z_set_sockopt(int socket,int option_name,string option_value) {
  if (socket==0) {
    return(0);
  }
  int ret = _zmq_setsockopt(socket,option_name,option_value,StringLen(option_value));
  if (ret==-1)
    z_error();
  return(ret);
}

  
int z_socket(int context,int type) {
 z_trace("z_socket: "+context+" "+type);
 int ret = _zmq_socket(context,type); 
 if (ret==-1)
   z_error();
 return(ret);
}
/*
int z_identity(int socket) {

  return(_zmq_get_opt_identity(socket)); 
  Print("1 is "+size[0]+" 2 is "+size[1]);
  if (ret!=0)  {
    z_error();
    return(0);
  } 
  return(size[0]);
}*/

int z_more(int socket) {
  if (socket==0) {
    return(0);
  }
  int ret = _zmq_get_opt_more(socket);
  if (ret==-1)
    z_error();
  return(ret);
}
  
int z_close(int socket) {
  z_trace("z_close");
  if (socket==0) {
   return(-1);
  }
  int ret = _zmq_close(socket); 
  if (ret==-1)
   z_error();
  return(ret);
}
int z_bind (int socket,string endpoint) { //for the servers
  z_trace("z_bind: "+endpoint); 
  int ret = _zmq_bind(socket,endpoint); 
  if (ret==-1)
   z_error();
  return(ret);
}
int z_connect(int socket,string endpoint) { //for the clients
 z_trace("z_connect: "+endpoint);
 int ret = _zmq_connect(socket,endpoint); 
 if (ret==-1) 
   z_error();
 return(ret);
}
 
//TODO: z_send_raw
int z_send_double_array(int socket,double array[],int flags=0) {
 z_trace("z_send_double_array: "+flags); 
 int ret = _zmq_send_double_array(array,ArraySize(array),socket,flags); 
 if (ret==-1)
   z_error();  
 return(ret);
}
int z_send_int_array(int socket,int array[],int flags=0) {
 z_trace("z_send_int_array: "+flags); 
 int ret = _zmq_send_int_array(array,ArraySize(array),socket,flags); 
 if (ret==-1)
   z_error();  
 return(ret);
}
 

int z_send(int socket,string msg,int flags=0) {
 z_trace("z_send: "+msg+" "+flags); 
 int message = z_msg_new(msg);
 int ret = _zmq_send(socket,message,flags); 
 z_msg_close(message);
 if (ret==-1)
   z_error();  
 return(ret);
}
string z_recv(int socket,int flags=0) {
 z_trace("z_recv: "+" "+flags);
 int message = z_msg_new(""); 
 int ret = _zmq_recv(socket,message,flags);
 string msg = ""; 
 if (ret==-1)
   z_error();
 else
   msg = z_msg(message);
 z_msg_close(message);
 return(msg);
}

//contextes


int z_init(int io_threads) {
  z_trace("z_init: "+io_threads);
  int ret = _zmq_init(io_threads);
  if (ret==NULL) //TODO: is null equal to zero?
   z_error();  
  return(ret);
}

int z_term(int context) {
 z_trace("z_term");
 if(context==NULL) 
   return(0);
 int ret = _zmq_term(context); 
 if (ret==-1) 
  z_error();
 return(ret);
}

//errors

//note: smq_error will return invalid messages if you call it by itself - in other words, zmq handles various system errors related
//to sockets and zmq_errno() will return errno which will return those system errors. I got 'error 11: resource temporarily unavailable' alot
//before i figured this out.
int z_error () {
  int err = _zmq_errno(); 
  if (err!=0) {
    Print("ZMQ zmq_error "+err+": "+_zmq_strerror(err));
  }
  return(err);
}


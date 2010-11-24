
#include "zmq.h"
#define EXPORT __declspec(dllexport)
#define API __stdcall

//i used zmq version 2.0.10
//Properties->Linker->General and Properties->Linker->Input
char *copy (void* s,zmq_msg_t* msg) {
    int len = zmq_msg_size(msg);
    char *d = (char *)(malloc (len + 1)); 
    if (d == NULL) return NULL;               
    memcpy (d,s,len);  
    d[len] = '\0';                             
    return d;                             
}


void my_free(void*data,void*hint) {
  free(data);
}
//messages: underscore before name denotes that it does something more than the actual original dll function, or that it is a function not in dll
//todo: handle error if malloc dont work?
EXPORT zmq_msg_t* API _zmsg_new ()
{
  void* ret =  malloc(sizeof(zmq_msg_t));  return ret;
}  

EXPORT int API _zmq_msg_init_data (zmq_msg_t* msg,char *data, int size) {
  char* new_data = strdup(data);
  int ret =  zmq_msg_init_data(msg,new_data,size,my_free,NULL);  
  return ret; 
}

EXPORT char* API _zmq_msg_data (zmq_msg_t *msg) {
  void* data = zmq_msg_data(msg);
  return copy(data,msg);
}


EXPORT int API _zmq_msg_size (zmq_msg_t *msg) {
  return zmq_msg_size(msg);  
}
EXPORT int API _zmq_msg_close (zmq_msg_t *msg) {
  return zmq_msg_close(msg); 
}

//context
EXPORT void* API _zmq_init (int io_threads) {
  return zmq_init(io_threads);  
}
EXPORT int API _zmq_term (void* context) {
  return zmq_term(context);  
}
//sockets
EXPORT void* API _zmq_socket (void* context, int type) {
  return zmq_socket(context,type);  
}
EXPORT int API _zmq_close(void* socket) {
 return zmq_close(socket); 
}
EXPORT int API _zmq_bind(void*socket,const char* endpoint) {
  return zmq_bind(socket,endpoint);   
}
EXPORT int API _zmq_connect(void*socket,const char*endpoint) {
  return  zmq_connect(socket,endpoint);
}
EXPORT int API _zmq_send(void*socket,zmq_msg_t*msg,int flags) {
  return zmq_send(socket,msg,flags); 
}
EXPORT int API _zmq_recv(void*socket,zmq_msg_t* msg,int flags) {
 return zmq_recv(socket,msg,flags);  
}
//error handling
EXPORT const char* API _zmq_strerror(int errnum) {
  return zmq_strerror(errnum);
}
EXPORT int API _zmq_errno() {
 return zmq_errno();
}
 
//misc
EXPORT void API _zmq_version(int *major,int*minor,int*patch) {  
  zmq_version(major,minor,patch);  
} 




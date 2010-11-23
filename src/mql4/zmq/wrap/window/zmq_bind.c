#include <windows.h>
#include <zmq.h>
#include <errno.h>



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
zmq_msg_t* WINAPI _zmsg_new ()
{
  void* ret =  malloc(sizeof(zmq_msg_t));  return ret;
}  

int WINAPI _zmq_msg_init_data (zmq_msg_t* msg,char *data, int size) {
  char* new_data = strdup(data);
  int ret =  zmq_msg_init_data(msg,new_data,size,my_free,NULL);  
  return ret; 
}

char* WINAPI _zmq_msg_data (zmq_msg_t *msg) {
  void* data = zmq_msg_data(msg);
  return copy(data,msg);
}


int WINAPI _zmq_msg_size (zmq_msg_t *msg) {
  int ret = zmq_msg_size(msg);  return ret;
}
int WINAPI _zmq_msg_close (zmq_msg_t *msg) {
  int ret = zmq_msg_close(msg); return ret;
}

//context
void* WINAPI _zmq_init (int io_threads) {
  void* ret = zmq_init(io_threads);  return ret;
}
int WINAPI _zmq_term (void* context) {
  int ret = zmq_term(context);  return ret;
}
//sockets
void* WINAPI _zmq_socket (void* context, int type) {
  void* ret= zmq_socket(context,type);  return ret;
}
int WINAPI _zmq_close(void* socket) {
 int ret = zmq_close(socket); return ret;
}
int WINAPI _zmq_bind(void*socket,const char* endpoint) {
  int ret = zmq_bind(socket,endpoint);   return ret;
}
int WINAPI _zmq_connect(void*socket,const char*endpoint) {
  int ret = zmq_connect(socket,endpoint);  return ret;
}
int WINAPI _zmq_send(void*socket,zmq_msg_t*msg,int flags) {
  int ret = zmq_send(socket,msg,flags); return ret;
}
int WINAPI _zmq_recv(void*socket,zmq_msg_t* msg,int flags) {
 int ret = zmq_recv(socket,msg,flags);  return ret;
}
//error handling
const char* WINAPI _zmq_strerror(int errnum) {
  return zmq_strerror(errnum);
}
int WINAPI _zmq_errno() {
 return zmq_errno();
}
 
//misc
void WINAPI _zmq_version(int *major,int*minor,int*patch) {  
  zmq_version(major,minor,patch);  
} 




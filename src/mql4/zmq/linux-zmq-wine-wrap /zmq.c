#include <windows.h>
#include <zmq.h>
#include <errno.h>

//messages
zmq_msg_t* WINAPI wine_zmsg_new ()
{
  void* ret =  malloc(sizeof(zmq_msg_t));  return ret;
} 

void WINAPI wine_zmq_version(int *major,int*minor,int*patch) {  
  zmq_version(major,minor,patch);  
} 

int WINAPI wine_zmq_msg_init_data (zmq_msg_t *msg, void *data, size_t size) {
  int ret =  zmq_msg_init_data(msg,data,size,NULL,NULL);  return ret;
}

int WINAPI wine_zmq_msg_size (zmq_msg_t *msg) {
  int ret = zmq_msg_size(msg);  return ret;
}

void* WINAPI wine_zmq_msg_data (zmq_msg_t *msg) {
  return zmq_msg_data(msg);  
}

int WINAPI wine_zmq_msg_close (zmq_msg_t *msg) {
  int ret = zmq_msg_close(msg); return ret;
}

//context
void* WINAPI wine_zmq_init (int io_threads) {
  void* ret = zmq_init(io_threads);  return ret;
}
int WINAPI wine_zmq_term (void* context) {
  int ret = zmq_term(context);  return ret;
}
//sockets
void* WINAPI wine_zmq_socket (void* context, int type) {
  void* ret= zmq_socket(context,type);  return ret;
}
int WINAPI wine_zmq_close(void* socket) {
 int ret = zmq_close(socket); return ret;
}
int WINAPI wine_zmq_bind(void*socket,const char* endpoint) {
  int ret = zmq_bind(socket,endpoint);   return ret;
}
int WINAPI wine_zmq_connect(void*socket,const char*endpoint) {
  int ret = zmq_connect(socket,endpoint);  return ret;
}
int WINAPI wine_zmq_send(void*socket,zmq_msg_t*msg,int flags) {
  int ret = zmq_send(socket,msg,flags); return ret;
}
int WINAPI wine_zmq_recv(void*socket,zmq_msg_t* msg,int flags) {
 int ret = zmq_recv(socket,msg,flags);  return ret;
}
//error handling
const char* WINAPI wine_zmq_strerror(int errnum) {
  return zmq_strerror(errnum);
}
int WINAPI wine_zmq_errno() {
 return zmq_errno();
}





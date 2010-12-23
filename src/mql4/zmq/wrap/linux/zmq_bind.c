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
zmq_msg_t* WINAPI wine_zmsg_new ()
{
  void* ret =  malloc(sizeof(zmq_msg_t));  return ret;
}  

int WINAPI wine_zmq_msg_init_data (zmq_msg_t* msg,char *data, int size) {
  char* new_data = strdup(data);
  int ret =  zmq_msg_init_data(msg,new_data,size,my_free,NULL);  
  return ret; 
}

char* WINAPI wine_zmq_msg_data (zmq_msg_t *msg) {
  void* data = zmq_msg_data(msg);
  return copy(data,msg);
}


int WINAPI wine_zmq_msg_size (zmq_msg_t *msg) {
  return zmq_msg_size(msg);
}
int WINAPI wine_zmq_msg_close (zmq_msg_t *msg) {
  return zmq_msg_close(msg); 
}

//context
void* WINAPI wine_zmq_init (int io_threads) {
  return zmq_init(io_threads); 
}
int WINAPI wine_zmq_term (void* context) {
  return zmq_term(context);
}

//polling
///used for freeing poller
void wine_free (void*ptr) {
  free(ptr);
}

//timeout in milliseconds
int WINAPI wine_zmq_poll(zmq_pollitem_t*items,int nitems,int timeout) {
  return zmq_poll(items,nitems,timeout*1000);
} 
//TODO: for some reason, if we pass any pointer other than void, it crashes. hmmm.....
void* WINAPI wine_new_poll (void* socket) {
  zmq_pollitem_t* ret =  (zmq_pollitem_t*)malloc(sizeof(zmq_pollitem_t)); 
  ret[0].socket = socket; 
  ret[0].fd = 0;
  ret[0].events = ZMQ_POLLIN; 
  return ret; 
}    
 
 
//sockets
void* WINAPI wine_zmq_socket (void* context, int type) {
  return zmq_socket(context,type); 
}
int WINAPI wine_zmq_close(void* socket) {
  return zmq_close(socket); 
}
int WINAPI wine_zmq_bind(void*socket,const char* endpoint) {
  return zmq_bind(socket,endpoint);  
}
int WINAPI wine_zmq_connect(void*socket,const char*endpoint) {
  return zmq_connect(socket,endpoint);  
}
int WINAPI wine_zmq_send(void*socket,zmq_msg_t*msg,int flags) {
  return zmq_send(socket,msg,flags);
}
int WINAPI wine_zmq_recv(void*socket,zmq_msg_t* msg,int flags) {
 return zmq_recv(socket,msg,flags);  
}

int WINAPI wine_zmq_setsockopt (void *socket, int option_name, const void *option_value, int option_len) {
  return zmq_setsockopt(socket,option_name,option_value,option_len);
}

//error handling
const char* WINAPI wine_zmq_strerror(int errnum) {
  return zmq_strerror(errnum);
}
int WINAPI wine_zmq_errno() {
 return zmq_errno();
}
 
//misc
void WINAPI wine_zmq_version(int *major,int*minor,int*patch) {  
  zmq_version(major,minor,patch);  
} 




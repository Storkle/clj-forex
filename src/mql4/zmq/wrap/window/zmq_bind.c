
#include "zmq.h"
#define EXPORT __declspec(dllexport)
#define WINAPI __stdcall

//i used zmq version 2.0.10


void * memcp(void * dst, void const * src, size_t len)
{
    char * pDst = (char *) dst;
    char const * pSrc = (char const *) src;
    int i=0;
    while (len--)
    {
        pDst[i]=pSrc[len];//*pDst++ = *pSrc++;
        ++i;
    }
    return (dst);
}



EXPORT int WINAPI _zmq_send_double_array ( char* array,int size, void*socket,int flags) {
  zmq_msg_t reply;
  zmq_msg_init_size (&reply, sizeof(double)*size);
  int i;
  memcp((char*)zmq_msg_data(&reply),array,sizeof(double)*size);
  int ret = zmq_send (socket, &reply, flags);
  zmq_msg_close (&reply);  
  return ret; 
}

EXPORT int WINAPI _zmq_send_int_array ( char* array,int size, void*socket,int flags) {
  zmq_msg_t reply;
  zmq_msg_init_size (&reply, sizeof(int)*size);
  int i;
  memcp((char*)zmq_msg_data(&reply),array,sizeof(int)*size);
  int ret = zmq_send (socket, &reply, flags); 
  zmq_msg_close (&reply);  
  return ret; 
}

EXPORT int64_t WINAPI _zmq_get_opt_more (void*  socket) {
   int64_t more;           //  Multipart detection
   size_t more_size = sizeof (more);
   zmq_getsockopt (socket, ZMQ_RCVMORE, &more, &more_size);
   return more; 
}

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
EXPORT zmq_msg_t* WINAPI _zmsg_new ()
{
  void* ret =  malloc(sizeof(zmq_msg_t));  return ret;
}  

EXPORT int WINAPI _zmq_msg_init_data (zmq_msg_t* msg,char *data, int size) {
  char* new_data = strdup(data);
  int ret =  zmq_msg_init_data(msg,new_data,size,my_free,NULL);  
  return ret; 
}

EXPORT char* WINAPI _zmq_msg_data (zmq_msg_t *msg) {
  void* data = zmq_msg_data(msg);
  return copy(data,msg);
}


EXPORT int WINAPI _zmq_msg_size (zmq_msg_t *msg) {
  return zmq_msg_size(msg);  
}
EXPORT int WINAPI _zmq_msg_close (zmq_msg_t *msg) {
  return zmq_msg_close(msg); 
}

//context
EXPORT void* WINAPI _zmq_init (int io_threads) {
  return zmq_init(io_threads);  
}
EXPORT int WINAPI _zmq_term (void* context) {
  return zmq_term(context);  
}


//polling
//timeout in milliseconds
EXPORT int WINAPI _zmq_poll(zmq_pollitem_t*items,int nitems,int timeout) {
  return zmq_poll(items,nitems,timeout*1000);
} 
//TODO: for some reason, if we pass any pointer other than void, it crashes. hmmm.....
EXPORT void* WINAPI _zmq_new_poll (void* socket) {
  zmq_pollitem_t* ret =  (zmq_pollitem_t*)malloc(sizeof(zmq_pollitem_t)); 
  ret[0].socket = socket; 
  ret[0].fd = 0;
  ret[0].events = ZMQ_POLLIN; 
  return ret; 
} 

//sockets
EXPORT void* WINAPI _zmq_socket (void* context, int type) {
  return zmq_socket(context,type);  
}
EXPORT int WINAPI _zmq_close(void* socket) {
 return zmq_close(socket); 
}
EXPORT int WINAPI _zmq_bind(void*socket,const char* endpoint) {
  return zmq_bind(socket,endpoint);   
}
EXPORT int WINAPI _zmq_connect(void*socket,const char*endpoint) {
  return  zmq_connect(socket,endpoint);
}
EXPORT int WINAPI _zmq_send(void*socket,zmq_msg_t*msg,int flags) {
  return zmq_send(socket,msg,flags); 
}
EXPORT int WINAPI _zmq_recv(void*socket,zmq_msg_t* msg,int flags) {
 return zmq_recv(socket,msg,flags);  
}

EXPORT int WINAPI _zmq_setsockopt (void *socket, int option_name, const void *option_value, int option_len) {
  return zmq_setsockopt(socket,option_name,option_value,option_len);
}

//error handling
EXPORT const char* WINAPI _zmq_strerror(int errnum) {
  return zmq_strerror(errnum);
}
EXPORT int WINAPI _zmq_errno() {
 return zmq_errno();
}
 
//misc
EXPORT void WINAPI _zmq_version(int *major,int*minor,int*patch) {  
  zmq_version(major,minor,patch);  
} 





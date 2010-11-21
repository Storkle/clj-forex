//+------------------------------------------------------------------+
//|                                                          zmq.mq4 |
//|                                                                  |
//|                                                                  |
//+------------------------------------------------------------------+
#property copyright ""
#property link      ""
#import "zmq.dll.so"
void zmq_version(int &major[],int &minor[],int &patch[]);
//messages
int zmq_msg_init_data (int msg,string data,int size);//int zmq_msg_init_data (zmq_msg_t *msg, void *data, size_t size);
int zmq_msg_size(int msg);//size_t zmq_msg_size (zmq_msg_t *msg);
int zmq_msg_data(int msg);//void *zmq_msg_data (zmq_msg_t *msg);
int zmq_msg_close (int msg);//int zmq_msg_close (zmq_msg_t *msg);
int zmsg_new (); //zmq_msg_t* zmsg_new(void)
//context
int zmq_init(int io_threads); //void *zmq_init (int io_threads);
int zmq_term(int context) ; //int zmq_term (void *context);
//sockets
int zmq_socket(int context,int type);//void *zmq_socket (void *context, int type);
int zmq_close(int socket); //int zmq_close (void *socket);
int zmq_bind(int socket,string endpoint); //int zmq_bind (void *socket, const char *endpoint);
int zmq_connect(int socket,string endpoint);//int zmq_connect (void *socket, const char *endpoint);
int zmq_send(int socket,int msg, int flags);//int zmq_send (void *socket, zmq_msg_t *msg, int flags); zmq_noblock
//todo: ZMQ_RCVMORE
int zmq_recv (int socket, int msg, int flags);//int zmq_recv (void *socket, zmq_msg_t *msg, int flags);
int zmq_errno();
string zmq_strerror(int errnum); //const char *zmq_strerror (int errnum);




#import



//+------------------------------------------------------------------+
//| expert initialization function                                   |
//+------------------------------------------------------------------+
int init()
{
  //----
   
  //----
  return(0);
}
//+------------------------------------------------------------------+
//| expert deinitialization function                                 |
//+------------------------------------------------------------------+
int deinit()
{
  //----
   
  //----
  return(0);
}
//+------------------------------------------------------------------+
//| expert start function                                            |
//+------------------------------------------------------------------+
int start()
{
  //----
  //int major[1];int minor[1];int patch[1]; Print("HI");
  //zmq_version(minor,patch);
  int major[1];int minor[1];int patch[1];
  int msg = zmsg_new();
  string data = "hi my name is seth";
  zmq_msg_init_data(msg,data,StringLen(data));
  Print("size is "+zmq_msg_size(msg)+" actual size is "+StringLen(data));
  zmq_msg_close(msg); 
  //zmq_version(major,minor,patch);
  //Print("major: "+major[0]+" minor: "+minor[0]+" patch: "+patch[0]);
  //----
  return(0);
}
//+------------------------------------------------------------------+

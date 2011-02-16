
#import "zmq_bind.dll"

int _zmq_send_double_array(double &array[],int size,int socket,int flags);
int _zmq_send_int_array(int &array[],int size,int socket,int flags);
int _zmq_get_opt_more(int socket);

int _zmq_poll(int socket,int nitems,int timeout);
int _zmq_new_poll (int socket);

void _zmq_version(int &major[],int &minor[],int &patch[]);
//messages
int _zmsg_new (); //zmq_msg_t* zmsg_new(void)
string _zmq_msg_data(int msg);//void *zmq_msg_data (zmq_msg_t *msg);
int _zmq_msg_init_data (int msg,string data,int size);//int zmq_msg_init_data (zmq_msg_t *msg, void *data, size_t size);

int _zmq_msg_size(int msg);//size_t zmq_msg_size (zmq_msg_t *msg);
int _zmq_msg_close (int msg);//int zmq_msg_close (zmq_msg_t *msg);
//context
int _zmq_init(int io_threads); //void *zmq_init (int io_threads);
int _zmq_term(int context) ; //int zmq_term (void *context);
//sockets
int _zmq_setsockopt(int socket, int option_name, string option_value, int option_len);
int _zmq_socket(int context,int type);//void *zmq_socket (void *context, int type);
int _zmq_close(int socket); //int zmq_close (void *socket);
int _zmq_bind(int socket,string endpoint); //int zmq_bind (void *socket, const char *endpoint);
int _zmq_connect(int socket,string endpoint);//int zmq_connect (void *socket, const char *endpoint);
int _zmq_send(int socket,int msg, int flags);//int zmq_send (void *socket, zmq_msg_t *msg, int flags); zmq_noblock
//todo: ZMQ_RCVMORE
int _zmq_recv (int socket, int msg, int flags);//int zmq_recv (void *socket, zmq_msg_t *msg, int flags);

string _zmq_strerror(int errnum); //const char *zmq_strerror (int errnum);
int _zmq_errno();
#import

//socket types
#define ZMQ_PAIR 0
#define ZMQ_PUB 1
#define ZMQ_SUB 2
#define ZMQ_REQ 3
#define ZMQ_REP 4
#define ZMQ_XREQ 5
#define ZMQ_XREP 6
#define ZMQ_PULL 7
#define ZMQ_PUSH 8
//socket options
#define ZMQ_HWM 1
#define ZMQ_SWAP 3
#define ZMQ_AFFINITY 4
#define ZMQ_IDENTITY 5
#define ZMQ_SUBSCRIBE 6
#define ZMQ_UNSUBSCRIBE 7
#define ZMQ_RATE 8
#define ZMQ_RECOVERY_IVL 9
#define ZMQ_MCAST_LOOP 10
#define ZMQ_SNDBUF 11
#define ZMQ_RCVBUF 12
#define ZMQ_RCVMORE 13
//send-recv options
#define ZMQ_NOBLOCK 1
#define ZMQ_SNDMORE 2
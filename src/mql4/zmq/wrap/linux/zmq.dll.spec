
@ stdcall zmq_version(ptr ptr ptr) wine_zmq_version

@ stdcall _zmsg_new() _wine_zmsg_new
@ stdcall _zmq_msg_init_data (ptr long) _wine_zmq_msg_init_data
@ stdcall _zmq_msg_data (ptr) _wine_zmq_msg_data 


@ stdcall zmq_msg_size (ptr) wine_zmq_msg_size 
@ stdcall zmq_msg_close (ptr) wine_zmq_msg_close

@ stdcall zmq_init () wine_zmq_init
@ stdcall zmq_term (ptr) wine_zmq_term

@ stdcall zmq_socket(ptr long) wine_zmq_socket
@ stdcall zmq_close(ptr) wine_zmq_close
@ stdcall zmq_bind(ptr ptr) wine_zmq_bind
@ stdcall zmq_connect(ptr ptr) wine_zmq_connect
@ stdcall zmq_send(ptr ptr long) wine_zmq_send
@ stdcall zmq_recv(ptr ptr long) wine_zmq_recv

@ stdcall zmq_strerror(long) wine_zmq_strerror
@ stdcall zmq_errno() wine_zmq_errno




@ stdcall _zmq_version(ptr ptr ptr) wine_zmq_version

@ stdcall _zmsg_new() wine_zmsg_new
@ stdcall _zmq_msg_init_data (ptr long) wine_zmq_msg_init_data
@ stdcall _zmq_msg_data (ptr) wine_zmq_msg_data 


@ stdcall _zmq_msg_size (ptr) wine_zmq_msg_size 
@ stdcall _zmq_msg_close (ptr) wine_zmq_msg_close

@ stdcall _zmq_init () wine_zmq_init
@ stdcall _zmq_term (ptr) wine_zmq_term

@ stdcall _zmq_socket(ptr long) wine_zmq_socket
@ stdcall _zmq_close(ptr) wine_zmq_close
@ stdcall _zmq_bind(ptr ptr) wine_zmq_bind
@ stdcall _zmq_connect(ptr ptr) wine_zmq_connect
@ stdcall _zmq_send(ptr ptr long) wine_zmq_send
@ stdcall _zmq_recv(ptr ptr long) wine_zmq_recv

@ stdcall _zmq_strerror(long) wine_zmq_strerror
@ stdcall _zmq_errno() wine_zmq_errno


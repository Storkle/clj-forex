project (zmq)
add_library(zmq_bind SHARED zmq_bind.c zmq_bind.def)
target_link_libraries(zmq_bind libzmq)

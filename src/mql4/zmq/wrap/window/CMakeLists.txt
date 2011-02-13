project (zmq)
SET(CMAKE_BUILD_TYPE Release)
add_library(zmq_bind SHARED zmq_bind.c zmq_bind.def)
target_link_libraries(zmq_bind libzmq)

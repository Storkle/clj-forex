#include <windows.h>
#include <zmq.h>
//i think this is the code needed! i forget how to compile in windows so i dont
//remember how to create the shared dll. someone?
zmq_msg_t* WINAPI zmsg_new ()
{malloc
  void* ret =  malloc(sizeof(zmq_msg_t));  return ret;
} 


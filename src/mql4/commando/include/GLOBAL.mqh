#include <utils.mqh>
int process_GLOBAL(string c,string command[]) {
  if (c=="NA") {
   return(0);
  } 
  else if (c=="KILL") {
   send_identity();
   return(-2);
  } 
  else if (c=="PING") {
   send_identity();
  } 
  else {
   return(-1);
  }
  return(0);
}
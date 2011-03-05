#include <utils.mqh>
#include <GUI.mqh>
#include <ACCOUNT.mqh>
#include <GLOBAL.mqh>
#include <INDICATORS_HAND.mqh>    //hand written defaults - includes 'i' functiosn, like iClose
#include <INDICATORS_DEFAULT.mqh> //automatically generated
#include <INDICATORS_CUSTOM.mqh>  //automatically generated

int process (int out,string request[]) {
    string command = request[0];
    socket = out;
    //processing  
    GetLastError();
    if(process_GUI(command,request)==-1) {
    GetLastError();
    int ret = process_GLOBAL(command,request);
    if (ret==-2) return(-1);  
    if (ret==-1) {
      GetLastError();
      ret = process_ACCOUNT(command,request);
      if (ret==-2) return(-1);
      if (ret==-1) {
        GetLastError();
        ret = process_INDICATORS_HAND(command,request);
        if (ret==-2) return(-1);
        if (ret==-1) {
          GetLastError();
          ret = process_INDICATORS_DEFAULT(command,request);
          if (ret==-2) return(-1);
          if (ret==-1) {
            GetLastError();
            ret = process_INDICATORS_CUSTOM(command,request); 
            if (ret==-2) return(-1);
            if (ret==-1) send_unknown();  
          }
        }
      }
      }
    }
    return(0);
} 
 
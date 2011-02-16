#include <utils.mqh>
void process_iClose (string request[]) {
  string symbol = request[2];
  int timeframe = StrToInteger(request[3]);
  int shift = StrToInteger(request[4]);
  int amount = StrToInteger(request[5]); 

  double array[];
  ArrayResize(array,amount);
   
  int j=0;
  for (int i=shift+amount-1;i>=shift;i--) {
    array[j] = iClose(symbol,timeframe,i); 
    j+=1;
  } 
  int err = GetLastError(); 
  if (err!=0) {
    send_error(err);
    return;
  }
  send_array(array);
 }
 
 void process_iOpen (string request[]) {
  string symbol = request[2];
  int timeframe = StrToInteger(request[3]);
  int shift = StrToInteger(request[4]);
  int amount = StrToInteger(request[5]); 

  double array[];
  ArrayResize(array,amount);
  
  int j=0;
  for (int i=shift+amount-1;i>=shift;i--) {
    array[j] = iOpen(symbol,timeframe,i); 
    j+=1;
  } 
  int err = GetLastError(); 
  if (err!=0) {
    send_error(err);
    return;
  }
  send_array(array);
 }
 
 void process_iHigh (string request[]) {
  string symbol = request[2];
  int timeframe = StrToInteger(request[3]);
  int shift = StrToInteger(request[4]);
  int amount = StrToInteger(request[5]); 

  double array[];
  ArrayResize(array,amount);
  
  int j=0;
  for (int i=shift+amount-1;i>=shift;i--) {
    array[j] = iHigh(symbol,timeframe,i); 
    j+=1;
  } 
  int err = GetLastError(); 
  if (err!=0)  {
    send_error(err);
    return;
  }
  send_array(array);
 }
 
 void process_iLow (string request[]) {
  string symbol = request[2];
  int timeframe = StrToInteger(request[3]);
  int shift = StrToInteger(request[4]);
  int amount = StrToInteger(request[5]); 

  double array[];
  ArrayResize(array,amount);
  
  int j=0;
  for (int i=shift+amount-1;i>=shift;i--) {
    array[j] = iLow(symbol,timeframe,i); 
    j+=1;
  } 
  int err = GetLastError(); 
  if (err!=0)  {
    send_error(err);
    return;
  }
  send_array(array);
 } 
 
int process_INDICATORS_HAND(string c,string command[]) {
  if (c=="iClose") 
    process_iClose(command);
  else if (c=="iLow")
    process_iLow(command);
  else if (c=="iHigh") 
    process_iHigh(command);
  else if (c=="iOpen")
    process_iOpen(command);
  else
    return(-1);
  return(0);
}


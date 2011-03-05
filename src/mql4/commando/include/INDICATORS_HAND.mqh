#include <utils.mqh>

void process_iBars (string request[]) {
 string symbol = request[1];
 int timeframe = StrToInteger(request[2]);
 
 int bars = iBars(symbol,timeframe);
 int err = GetLastError();
 if (err!=0)  {
   send_error(err);
   return;
 }
 send_long(bars);
}

void process_iTime (string request[]) {
 string symbol = request[1];
 int timeframe = StrToInteger(request[2]);
 int shift = StrToInteger(request[3]);
 int time = iTime(symbol,timeframe,shift);
 int err = GetLastError();
 if (err!=0)  {
   send_error(err);
   return;
 }
 send_long(time);
}

void process_iBarShift (string request[]) {
 string symbol = request[1];
 int timeframe = StrToInteger(request[2]);
 int time = StrToInteger(request[3]);
 
 int bar = iBarShift(symbol,timeframe,time);
 int err = GetLastError();
 if (err!=0)  {
   send_error(err);
   return;
 }
 send_long(bar);
} 


void process_iClose (string request[]) {
  string symbol = request[1];
  int timeframe = StrToInteger(request[2]);
  //mode = request[4]; 
  datetime from_date = StrToInteger(request[4]);
  datetime to_date = StrToInteger(request[5]);
  int min = StrToInteger(request[6]);
  int max = StrToInteger(request[7]); 
  //set from and to, based on min and max!!!
  int from = iBarShift(symbol,timeframe,from_date);
  //if to_date is zero, max is # of bars, 
  int to = from+max-1; 
  if (to_date!=0)
    to = iBarShift(symbol,timeframe,to_date);
    
  int err = GetLastError();
  if (err!=0) {
    send_error(err);
    return;
  }
  
  if (to_date!=0) {
    if (to-from+1>max) {
       to = from+max-1;
    }
    if (to-from+1<min) {
       to = from+min-1;
    }
  }
  //
  to = MathMin(to,iBars(symbol,timeframe)-1);
  double array[];
  ArrayResize(array,to-from+1);
  int j=0;
  for (int i=to;i>=from;i--) {
    array[j] = iClose(symbol,timeframe,i); 
    j+=1;
  } 

  err = GetLastError(); 
  if (err!=0) {
    send_error(err);
    return;
  } 
  send_array(array);
 }
 
 
 
 
 
 
 void process_iOpen (string request[]) {
  string symbol = request[1];
  int timeframe = StrToInteger(request[2]);
  //mode = request[4]; 
  int from_date = StrToInteger(request[4]);
  int to_date = StrToInteger(request[5]);
  int min = StrToInteger(request[6]);
  int max = StrToInteger(request[7]); 
  
  
  //set from and to, based on min and max!!!
  int from = iBarShift(symbol,timeframe,from_date);
  //if to_date is zero, max is # of bars, 
  int to = from+max-1;
  if (to_date!=0)
    to = iBarShift(symbol,timeframe,to_date);
    
  int err = GetLastError();
  if (err!=0) {
    send_error(err);
    return;
  }
  
  if (to_date!=0) {
    if (to-from+1>max) {
       to = from+max-1;
    }
    if (to-from+1<min) {
       to = from+min-1;
    }
  }
  //
  to = MathMin(to,iBars(symbol,timeframe)-1);
  double array[];
  ArrayResize(array,to-from+1);
   
  int j=0;
  for (int i=to;i>=from;i--) {
    array[j] = iOpen(symbol,timeframe,i); 
    j+=1;
  } 
  err = GetLastError(); 
  if (err!=0) {
    send_error(err);
    return;
  } 
  send_array(array);
 }
 
 
 
 void process_iHigh (string request[]) {
  string symbol = request[1];
  int timeframe = StrToInteger(request[2]);
  //mode = request[4]; 
  int from_date = StrToInteger(request[4]);
  int to_date = StrToInteger(request[5]);
  int min = StrToInteger(request[6]);
  int max = StrToInteger(request[7]); 
  
  
  //set from and to, based on min and max!!!
  int from = iBarShift(symbol,timeframe,from_date);
  //if to_date is zero, max is # of bars, 
  int to = from+max-1;
  if (to_date!=0)
    to = iBarShift(symbol,timeframe,to_date);
    
  int err = GetLastError();
  if (err!=0) {
    send_error(err);
    return;
  }
  
  if (to_date!=0) {
    if (to-from+1>max) {
       to = from+max-1;
    }
    if (to-from+1<min) {
       to = from+min-1;
    }
  }
  //
  to = MathMin(to,iBars(symbol,timeframe)-1);
  double array[];
  ArrayResize(array,to-from+1);
   
  int j=0;
  for (int i=to;i>=from;i--) {
    array[j] = iHigh(symbol,timeframe,i); 
    j+=1;
  } 
  err = GetLastError(); 
  if (err!=0) {
    send_error(err);
    return;
  } 
  send_array(array);
 }
 
 
 void process_iLow (string request[]) {
  string symbol = request[1];
  int timeframe = StrToInteger(request[2]);
  //mode = request[4]; 
  int from_date = StrToInteger(request[4]);
  int to_date = StrToInteger(request[5]);
  int min = StrToInteger(request[6]);
  int max = StrToInteger(request[7]); 
  
  
  //set from and to, based on min and max!!!
  int from = iBarShift(symbol,timeframe,from_date);
  //if to_date is zero, max is # of bars, 
  int to = from+max-1;
  if (to_date!=0)
    to = iBarShift(symbol,timeframe,to_date);
    
  int err = GetLastError();
  if (err!=0) {
    send_error(err);
    return;
  }
  
  if (to_date!=0) {
    if (to-from+1>max) {
       to = from+max-1;
    }
    if (to-from+1<min) {
       to = from+min-1;
    }
  }
  to = MathMin(to,iBars(symbol,timeframe)-1);
  //
  double array[];
  ArrayResize(array,to-from+1);
   
  int j=0;
  for (int i=to;i>=from;i--) {
    array[j] = iLow(symbol,timeframe,i); 
    j+=1;
  } 
  err = GetLastError(); 
  if (err!=0) {
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
  else if (c=="iBars") 
    process_iBars(command);
  else if (c=="iTime")
    process_iTime(command);
  else if (c=="iBarShift")
    process_iBarShift(command);
  else
    return(-1);
  return(0);
}


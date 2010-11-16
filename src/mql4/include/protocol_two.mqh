#include <socket.mqh>
#include <utils.mqh>

int msgsock = -1;

int start_server_loop(string id,int socket) {
  GetLastError();     
  msgsock = sock_accept(socket);
  if (errno()!=0) 
      return(-1);
  while(True) {   Print("IN LOOP"); 
      serveMe(msgsock);     
      int error = errno(); 
      if (IsStopped()==True || error!=0) {
	     return(0); 
      }     
      RefreshRates();    
      //if (error!=0) 
	   //    sock_close(msgsock);
       
    } 
  
}


//TODO: detect aall evil receive errors!
void serveMe (int msgsock) {
  GetLastError();
  string command[] ; //= new string[];
  split(command,sock_receive(msgsock)); //we assume it is 'get bars' command!
  int from = StrToInteger(command[1]);
  int to = StrToInteger(command[2]); Print("from "+from+" to "+to);
  Print("MAN");
  string request[] ;//= new string[];
  split(request,sock_receive(msgsock));
  Print("i received it"+request[0]+" "+request[1]); 
 
  string ret[];
  GetLastError();
  processBarRequest(ret,request,from,to); 
  for (int i=0;i<ArraySize(ret)/2;i++) {
     sock_send(msgsock,ret[2*i]+" "+ret[2*i+1]+"\n");
     Print("SENDT");
  }
  
  GetLastError();
  Print("DONE");
  //Print("first yummy is "+ret[0]+" "+ret[1]);
  //for (int i=0;i<results.length;i++) {
  //   sock_send(results[i]);
  //}
}


int GetSymbolBars(double& ret[],string symbol,int timeframe, int shift) {
    ret[0] = iHigh(symbol,timeframe,shift);
    ret[1] = iLow(symbol,timeframe,shift);
    ret[2]  =iOpen(symbol,timeframe,shift);
    ret[3] = iClose(symbol,timeframe,shift); 
  return(GetLastError());
}



string processBarRequest(string& ret[],string request[],int from, int to) {
  ArrayResize(ret,ArraySize(request));
  double vals[4];
  int i=0; 
  
  for(i=0;i<ArraySize(request)/2;i++) {
    string result = "";
    int j=0;
        result = "";
    for (j=from;j<to;j++) {
  
      int err = GetSymbolBars(vals,request[2*i],StrToInteger(request[2*i+1]),j);
      if (err!=0) {
	      result = "error "+err;
         ret[2*i] = "0";
	      break;
      }
      if (vals[0]==0) {
       	ret[2*i] = ""+(i+1);
      	break;
      }
      result = result+" "+ DoubleToStr(vals[0],5)+" "+DoubleToStr(vals[1],5)+" "+DoubleToStr(vals[2],5)+" "+DoubleToStr(vals[3],5);
    }
    ret[2*i] = ""+(to-from);
    ret[2*i+1]= result;
  }
  return(0);
}



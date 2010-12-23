#include <zmq_bind.mqh>
#include <utils.mqh>

string process_bars_absolute(string &req[]) {
  int i; 
  string symbol = req[2]; string ret = "";
  int timeframe = StrToInteger(req[3]);
  int fr = StrToInteger(req[4]); 
  int t = StrToInteger(req[5]); trace("from is "+fr+" and to is "+t);
  int from = iBarShift(symbol,timeframe,fr); 
  int to = iBarShift(symbol,timeframe,t);
  ret=""+iTime(symbol,timeframe,from)+" "; 
  for (i=from;i<=to;i++) {
    double high = iHigh(symbol,timeframe,i); 
    double low = iLow(symbol,timeframe,i);
    double open  =iOpen(symbol,timeframe,i);
    double close = iClose(symbol,timeframe,i); 
    int err = GetLastError();
    if (err!=0) 
      return(error(err));
    ret=ret+high+" "+low+" "+open+" "+close+" ";
  }
  return(ret);
}


string process_bars_relative(string &req[]) {
  int i; 
  string symbol = req[2]; string ret = "";
  int timeframe = StrToInteger(req[3]);
  int from = StrToInteger(req[4]);
  int to = StrToInteger(req[5]); trace("from is "+from+" and to is "+to);
  for (i=from;i<=to;i++) {
    double high = iHigh(symbol,timeframe,i); 
    double low = iLow(symbol,timeframe,i);
    double open  =iOpen(symbol,timeframe,i);
    double close = iClose(symbol,timeframe,i); 
    int err = GetLastError();
    if (err!=0) 
      return(error(err));
    ret=ret+high+" "+low+" "+open+" "+close+" ";
  }
  return(iTime(symbol,timeframe,from)+" "+ret);
}

string process_AccountBalance() {
  return(DoubleToStr(AccountBalance(),5));
}




#include <WinUser32.mqh>

void process_ChangeTimeframe (string &req[]) {
   int hwnd = WindowHandle(Symbol(),Period());
   PostMessageA(hwnd, WM_COMMAND,StrToInteger(req[2]), 0);
  // return("true");
}


//starts at index 2
string protocol(string&request[]) {
  GetLastError(); //filter out any non relative errors?
  string command = request[1];
  string ret = "";
  if (command=="bars_relative")
    {
      ret = process_bars_relative(request);
    } else if (command=="bars_absolute") {
      ret = process_bars_absolute(request);
    } else if (command=="AccountBalance") {
      ret = process_AccountBalance();
    } else if (command=="ChangeTimeframe") {
      process_ChangeTimeframe(request);
      ret = "";
    } else  {
      ret = "error unknown";
    }
  return(ret);
 // return(id+" "+ret);
}

 
int context;
int recv; //receive message
int sub,pub;

int deinit() {
 trace("deinitializing");
 z_term(context);z_close(sub);z_close(pub);
 z_msg_close(recv);
 return(0);
}
//TODO: how to handle closing of client?
int start () {
  context = z_init(1);
  sub = z_socket(context,ZMQ_SUB);
  pub = z_socket(context,ZMQ_PUB);
  recv = z_msg_empty();
    
  if(z_connect(sub,"tcp://127.0.0.1:2045")==-1) 
    return(-1);
  if(z_bind(pub,"tcp://127.0.0.1:2055")==-1) 
    return(-1); 
  z_subscribe(sub,"");
   
  int recv_poll = z_new_poll(sub);
  
  while (1==1) {
    Print("waiting for receive...");
    while(z_poll(recv_poll,100)==0) {
      if (IsStopped())
        return(0);
    }
    z_recv(sub,recv,0);  
    RefreshRates();     
  
    string r[]; string receive = z_msg(recv); 
    split(r,receive);
    trace("received "+receive);
    //process
    string reply = protocol(r);
    if (reply=="") {
      z_send(pub,r[0]+" end",0);
      return(0);
    }
    reply = r[0]+" "+reply;//string id = request[0];
   //  
    trace("sending: length "+StringLen(reply)+" ");
    z_send(pub,reply,0);
    Print("sent response..."); 
  }
  return(0);
}


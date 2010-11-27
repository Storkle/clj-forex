#include <zmq_bind.mqh>
#include <utils.mqh>

string bars_absolute(string &req[]) {
  int i; 
  string symbol = req[1]; string ret = "";
  int timeframe = StrToInteger(req[2]);
  int fr = StrToInteger(req[3]); 
  int t = StrToInteger(req[4]); trace("from is "+fr+" and to is "+t);
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


string bars_relative(string &req[]) {
  int i; 
  string symbol = req[1]; string ret = "";
  int timeframe = StrToInteger(req[2]);
  int from = StrToInteger(req[3]);
  int to = StrToInteger(req[4]); trace("from is "+from+" and to is "+to);
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

string protocol(string&request[]) {
  GetLastError(); //filter out any non relative errors?
  string command = request[0];
  if (command=="bars_relative")
    {
      return(bars_relative(request));
    } else if (command=="bars_absolute")
    {
      return(bars_absolute(request));
    } else 
    {
      return("error unknown");
    }
}


int context,server;
int recv,reply;

int deinit() {
 trace("deinitializing");
 z_term(context);z_close(server);z_msg_close(recv);//z_msg_close(reply);
 return(0);
}
//TODO: how to handle closing of client?
int start () {
  context = z_init(1);
  server = z_socket(context,ZMQ_REP);
  recv = z_msg_empty();
  if(z_bind(server,"tcp://lo:2027")==-1) 
    return(0);
  while (1==1) {
    trace("waiting for receive...");
    if (IsStopped()) 
     return(0);
    z_recv(server,recv,0); 
    RefreshRates(); 
    string r[]; string receive = z_msg(recv);
    split(r,receive);
    trace("received "+receive);
    string ret = protocol(r);
    trace("sending: length "+StringLen(ret)+" ");
    reply = z_msg_new(ret); 
    z_send(server,reply,0);
    z_msg_close(reply); reply=0;//TODO: copy them!
    trace("sent response..."); 
  }
  return(0);
}


#include <zmq_bind.mqh>
#include <utils.mqh>

string bars_relative(string &req[]) {
  int i;
  string symbol = req[1]; string ret = "";
  int timeframe = StrToInteger(req[2]);
  int from = StrToInteger(req[3]);
  int to = StrToInteger(req[4]);
  for (i=from;i<=to;i++) {
    int high = iHigh(symbol,timeframe,i);
    int low = iLow(symbol,timeframe,i);
    int open  =iOpen(symbol,timeframe,i);
    int close = iClose(symbol,timeframe,i); 
    int err = GetLastError();
    if (err!=0) 
      return(error(err));
    ret=high+" "+low+" "+open+" "+close;
  }
  return(iTime(symbol,timeframe,from)+" "+ret);
}

string protocol(string&request[]) {
  GetLastError(); //filter out any non relative errors?
  string command = request[0];
  if (command=="bars_relative")
    {
      return(bars_relative(request));
    } else
    {
      return("error unknown");
    }
}


int context,server;
int recv,reply;

int deinit() {
 z_term(context);z_close(server);z_msg_close(recv);z_msg_close(reply);
 return(0);
}

int start () {
  context = z_init(1);
  server = z_socket(context,ZMQ_REP);
  recv = z_msg_empty();
  z_bind(server,"tcp://lo:2027");
  while (1==1) {
    trace("waiting for receive...");
    z_recv(server,recv,0);
    string r[]; string receive = z_msg(recv);
    split(r,receive);
    trace("received "+receive);
    string ret = protocol(r);
    trace("sending: length "+StringLen(ret));
    reply = z_msg_new(ret);
    z_send(server,reply,0);
    z_msg_close(reply); reply=0;//TODO: copy them!
    trace("sent response...");
  }
  return(0);
}


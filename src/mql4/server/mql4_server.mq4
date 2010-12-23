#include <zmq_bind.mqh>
#include <utils.mqh>
#include <GUI.mqh>

int receive_port = 2070;
int send_port = 2065;
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
#define TD1 33134
#define TH4 33136
#define TM1 33137
#define TM5 33138
#define TM15 33139
#define TM30 33140
#define TW1 33141
#define TMN 33334
#define TH1 35400

int nums[9];
void init_protocol() {
nums[0] = 1;
nums[1] = 5;
nums[2] = 15;
nums[3] = 30;
nums[4] = 60;
nums[5] = 240;
nums[6] = 24*60;
nums[7] = 10080;
nums[8] = 43200;
}

  

string process_SetFocus(string &req[]) {
  Print("req is "+req[3]);
  int hwnd = set_focus(req[2],StrToInteger(req[3]));
  if (hwnd==0)
    return("error no_such_window");
  return("true");
} 

#define HWND_TOP 0
#define SWP_NOSIZE 1
#define SWP_NOMOVE 2
int set_focus(string symbol,int timeframe) {
  int hwnd = get_window(symbol);
  if (hwnd!=0) {
      int parent = GetParent(hwnd);
      SetWindowPos(parent,HWND_TOP,0,0,0,0,SWP_NOSIZE|SWP_NOMOVE);
      return(parent);
  }
   return(hwnd);
} 

int get_window (string symbol) {
  for (int i=0;i<ArraySize(nums);i++) {
    int handle= WindowHandle(symbol,nums[i]);
    if (handle!=0)
      return(handle);
  }
  return(0);
}


string process_Post(string &req[]) {
  string symbol = req[2];
  int timeframe = StrToInteger(req[3]);
  int command = StrToInteger(req[4]);
  int hwnd = get_window(symbol);
  if (hwnd!=0) {
    PostMessageA(hwnd, WM_COMMAND,timeframe, 0);
    return(""+hwnd);
  }
  return("error no_such_window");
}

int change_timeframe(string symbol,int timeframe) {
  int hwnd = get_window(symbol);
  if (hwnd!=0) 
      PostMessageA(hwnd, WM_COMMAND,timeframe, 0);
  else
     return(0);
  return(hwnd);
}

string process_ChangeTimeframe (string &req[]) {
   int hwnd = change_timeframe(req[3],StrToInteger(req[2]));
   if (hwnd==0)
      return("error no_such_window");
   return("true");
}

//starts at index 2
string protocol(string&request[]) {
  GetLastError(); //filter out any non relative errors?
  string command = request[1];
  string ret = "";
  
    //GET BARS
  if (command=="bars_relative")
    {
      ret = process_bars_relative(request);
    } else if (command=="bars_absolute") {
      ret = process_bars_absolute(request);
    } 
    //PROCESS ACCOUNT
      else if (command=="AccountBalance") {
      ret = process_AccountBalance();
    }  
    //CHANGE GUI
      else if (command=="ChangeTimeframe") {
      ret = process_ChangeTimeframe(request);
    } else if (command=="Post") {
      ret = process_Post(request);
    } else if (command=="SetFocus") {
      ret = process_SetFocus(request);
    } 
    //GUI
      else if (command=="ObjectCreate") {
      ret = process_ObjectCreate(request);
    } else if (command=="ObjectDelete") {
      ret = process_ObjectDelete(request);
    } else if (command=="ObjectGet") {
      ret = process_ObjectGet(request);
    } else if (command=="ObjectSet") {
      ret = process_ObjectSet(request);
    } else if (command=="ObjectsTotal") {
      ret = process_ObjectsTotal(request);
    } else if (command=="ObjectName") {
      ret = process_ObjectName(request);
    } else if (command=="DeleteAll") {
      ret = process_ObjectsDeleteAll(request); 
    } 
    //Unknown Command
    else  {
      ret = "error unknown";
    }
  return(ret);
 // return(id+" "+ret);
}



 

/////////////////////////////
/////////////////////////////
int context;
int recv; //receive message
int sub,pub;

int deinit() {
 trace("deinitializing");
 z_term(context);z_close(sub);z_close(pub);
 z_msg_close(recv);
 return(0);
}

int recv_poll;
int init () {
  Print("INIT");
  init_protocol();
  context = z_init(1);
  sub = z_socket(context,ZMQ_SUB);
  pub = z_socket(context,ZMQ_PUB);
  recv = z_msg_empty();     
  if(z_connect(sub,"tcp://127.0.0.1:"+send_port)==-1) 
    return(-1);
  if(z_bind(pub,"tcp://127.0.0.1:"+receive_port)==-1) 
    return(-1); 
  z_subscribe(sub,"");
  recv_poll = z_new_poll(sub);
  loop();
  return(0); 
}

int start () {
 Print("IN THE START");
 loop();
 return(0);
 }
 
 int loop () {
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
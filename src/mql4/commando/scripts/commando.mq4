//+------------------------------------------------------------------+
//|                                                     commando.mq4 |
//|                                                                  |
//|                                                                  |
//+------------------------------------------------------------------+
#include <zmq_bind.mqh>
#include <utils.mqh>
#include <ACCOUNT.mqh>
//#include <PROTOCOL.mqh>

//+------------------------------------------------------------------+
//| script program start function                                    |
//+------------------------------------------------------------------+
int push_port = 3000;
int pull_port = 3005;
//int sub_port = 3010;

int context;
int poll; 
int push,pull;  

//string pid = "pid";
//string group = "";

int deinit() {
 trace("deinitializing");
 z_term(context); 
 z_close(push); 
 //NOTE: if we forget to close sockets, metatrader will probably eventually crash after deinit;
 z_close(pull);
 pull=0;push=0;context=0;
 ObjectDelete("label");
 WindowRedraw();
 return(0);
}

int connect () {
  context = z_init(1);
  //pull = z_socket(context,ZMQ_PULL);
  push = z_socket(context,ZMQ_PUSH); 
  pull = z_socket(context,ZMQ_PULL);    
  Print("Attempting to connect to push port "+push_port);
  if(z_connect(push,"tcp://127.0.0.1:"+push_port)==-1)  
    return(-1); 
  Print("connected!");
  Print("Attempting to connect to pull port "+pull_port);
  if (z_connect(pull,"tcp://127.0.0.1:"+pull_port)==-1)
    return(-1);
  Print("connected!");
  return(0); 
}
 
 
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
    //CHANGE GUI
    /* else if (command=="ChangeTimeframe") {
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
    } else if (command=="ChartWindow") {
      ret = ChartWindow(request[2]);
    } */
    //ACCOUNT
    else if (command=="iHigh") {
	   ret = process_iHigh(request);
	}
	else if (command=="iLow") {
	   ret = process_iLow(request);
	}
	else if (command=="iClose") {
	   ret = process_iClose(request);
	}
	else if (command=="iOpen") {
	   ret = process_iOpen(request);
	}
	
     else if (command=="AccountProfit") {
	   ret = process_AccountProfit(request);
	}
     else if (command=="AccountBalance") {
	   ret = process_AccountBalance(request);
	} else if (command=="AccountCredit") {
	   ret = process_AccountCredit(request);
	} else if (command=="AccountCompany") {
	   ret = process_AccountCompany(request);
	} else if (command=="AccountCurrency") {
	   ret = process_AccountCurrency(request);
	} else if (command=="AccountEquity") {
	   ret = process_AccountEquity(request);
	} else if (command=="AccountFreeMargin") {
	   ret = process_AccountFreeMargin(request);
	} else if (command=="AccountLeverage") {
	   ret = process_AccountLeverage(request);
	} else if (command=="AccountMargin") {
	   ret = process_AccountMargin(request);
	} else if (command=="AccountName") {
	   ret = process_AccountName(request);
	} else if (command=="AccountNumber") {
	   ret = process_AccountNumber(request);
	} else if (command=="AccountServer") {
	   ret = process_AccountServer(request);
	} else if (command=="OrderLots") {
	   ret = process_OrderLots(request);
	} else if (command=="OrderDelete") {
	   ret = process_OrderDelete(request);
	} else if (command=="OrderCloseTime") {
	   ret = process_OrderCloseTime(request);
	} else if (command=="OrderType") {
	   ret = process_OrderType(request);
	} else if (command=="OrdersTotal") {
	   ret = process_OrdersTotal(request);
	} else if (command=="OrderSend") {
	   ret = process_OrderSend(request);
	} else if (command=="MarketInfo") {
	   ret = process_MarketInfo(request);
	} else if (command=="OrderClose") {
	   ret = process_OrderClose(request);
	} else if (command=="OrderModify") {
	   ret = process_OrderModify(request);
	}
    //Unknown Command
    else  {
      ret = "error -1";
    }
  return(ret);
}


    
int loop () { 
  Print("Entering Commando Loop");
  while(true) { 
    if (IsStopped())
      return(0);
    string request[];
    Print("waiting ...");
    split(request,z_recv(pull));
    string command = request[1];
    string id = request[0];
    
    RefreshRates();
    
    if (command=="KILL") {
      Print("KILLING node ...");
      z_send(push,"true",ZMQ_SNDMORE);
      z_send(push,id);
      return(0);
    } else {
      string ret = protocol(request);
      z_send(push,ret,ZMQ_SNDMORE);
      z_send(push,id);
    }
    Print("OK");
    GetLastError();
    /*
    z_send(push,pid,ZMQ_SNDMORE);      //send pid
    z_send(push,group,ZMQ_SNDMORE);                //send group
    z_send(push,"REQUEST");
    
    string first = z_recv(sub);
    if (first=="KILL")  {
      Print("KILLING node: group "+group+" pid "+pid);
      return(0);
    }     
    Print("I received "+z_recv(sub)); 
    Sleep(1000);
    if (IsStopped())
      return(0); 
    */
  }
  return(0);
} 

int start()
  {
  ObjectCreate("label", OBJ_LABEL, 0, 0, 0);
  ObjectSet("label", OBJPROP_XDISTANCE, 0);
  ObjectSet("label", OBJPROP_YDISTANCE, 10);
  ObjectSetText("label", "COMMANDO", 10, "Times New Roman", Blue);
  WindowRedraw();
  
   if (connect()==-1) {
      return(0); 
    }  
   loop();
   return(0);
  }
//+------------------------------------------------------------------+
#include <socket.mqh>



int start_server_loop(string id,int socket) {
    int msgsock = -1;
    if (errno()!=0)
       return(-1);     
    while(True) {
      msgsock = sock_accept(socket);
      if (errno()!=0) 
         return(-1);
      while(True) {    

         string item = sock_receive(msgsock);
         RefreshRates();   
         int error = errno();
     
         if (IsStopped()==True) {
            return(msgsock);
         }   
         
         if (error!=0) 
             break;
         string response = process(id,item);
         error = errno();
         sock_send(msgsock,response);  
          
         if (IsStopped()==True) 
            return(msgsock);
         
         if (error!=0)
             break; 
 
         
      } 
    }  
}

#include <colors.mqh>

string error (int code) {
  return("error "+code);
}

void split(string& arr[], string str, string sym) 
{
  ArrayResize(arr, 0);
  string item;
  int pos, size;
  
  int len = StringLen(str);
  for (int i=0; i < len;) {
    pos = StringFind(str, sym, i);
    if (pos == -1) pos = len;
    
    item = StringSubstr(str, i, pos-i);
    item = StringTrimLeft(item);
    item = StringTrimRight(item);
    
    size = ArraySize(arr);
    ArrayResize(arr, size+1);
    arr[size] = item;
    
    i = pos+1;
  }
}




//accounts
/*
"AccountBalance"
"AccountCredit"
"AccountCompany"
"AccountCurrency"
"AccountEquity"
"AccountFreeMargin"
"AccountLeverage"
"AccountMargin"
"AccountName"
"AccountNumber" 
"AccountProfit" 
"AccountServer"
*/

string process_AccountBalance(string command[]) {
  return(DoubleToStr(AccountBalance(),5));
}
string process_AccountCredit(string command[]) {
  return(DoubleToStr(AccountCredit(),5));
}
string process_AccountCompany(string command[]) {
  return(AccountCompany());
}
string process_AccountCurrency(string command[]) {
  return(AccountCurrency());
}
string process_AccountEquity(string command[]) {
  return(DoubleToStr(AccountEquity(),5));
}
string process_AccountFreeMargin(string command[]) {
  return(DoubleToStr(AccountFreeMargin(),5));
}
string process_AccountLeverage(string command[]) {
  return(DoubleToStr(AccountLeverage(),5));
}
string process_AccountMargin(string command[]) {
  return(DoubleToStr(AccountMargin(),5));
}
string process_AccountName(string command[]) {
  return(AccountName());
}
string process_AccountNumber(string command[]) {
  return(DoubleToStr(AccountNumber(),5));
}
string process_AccountProfit(string command[]) {
  return(DoubleToStr(AccountProfit(),5));
}
string process_AccountServer(string command[]) {
  return(AccountServer());
}


string process_OrderModify(string command[]) {
  int ticket = StrToInteger(command[2]);
  double price = StrToDouble(command[3]);
  double stoploss = StrToDouble(command[4]);
  double takeprofit = StrToDouble(command[5]);
  datetime expiration = 0;//make_datetime(command[5]);
  color color_of = make_color(command[6]);
  bool success = OrderModify(ticket,price,stoploss,takeprofit,expiration,color_of);
  if (success!=true) {
    return(error(GetLastError()));
  }
  return("true");
}









    
string process_OrderClose(string command[]) {
  int ticket = StrToInteger(command[2]);
  double lots = StrToDouble(command[3]);
  double price = StrToDouble(command[4]);
  int slippage = StrToDouble(command[5]);
  color color_of = make_color(command[6]);
  Print(ticket);
  Print(lots);
  Print(price);
  Print(slippage);
  Print(color_of);
  bool success = OrderClose(ticket,lots,price,slippage,color_of);
  if (success!=true) {
    return(error(GetLastError()));
  }
  return("true");
}


string process_MarketInfo(string command[]) {
  string symbol = command[2];
  int type = StrToInteger(command[3]);
  double result = MarketInfo(symbol,type);
  int err = GetLastError();
  if (err!=0)
    return(error(err));
  return(DoubleToStr(result,5));
}

double norm (string symbol,double d) {
  return(NormalizeDouble(d,MarketInfo(symbol,MODE_DIGITS)));
}

string process_OrderSend(string command[]) {
 string symbol = command[2];
 int cmd = StrToInteger(command[3]);
 double volume = StrToDouble(command[4]);
 double price = norm(symbol,StrToDouble(command[5]));
 int slippage = StrToInteger(command[6]);
 double stoploss = norm(symbol,StrToDouble(command[7]));
 double takeprofit = norm(symbol,StrToDouble(command[8]));
 datetime expiration = 0;//make_datetime(command[8]);
 color color_of = make_color(command[9]);
 
 int ticket = OrderSend(symbol,cmd,volume,price,slippage,stoploss,takeprofit,NULL,0,expiration,color_of);
 int err = GetLastError();
 Print("ticket is "+ticket);
 if (err!=0)
   return(error(err));
 return(DoubleToStr(ticket,15));
}



string process_OrdersTotal (string command[]) {
  return(DoubleToStr(OrdersTotal(),5));
}



string process_OrderCloseTime(string command[]) {
   int ticket= StrToInteger(command[2]);
   if (OrderSelect(ticket,SELECT_BY_TICKET,MODE_HISTORY)==true) {
     return(DoubleToStr(OrderCloseTime(),5));
    }
    return(error(GetLastError()));
}

string process_OrderType(string command[]) {
   int ticket= StrToInteger(command[2]);
   bool result = OrderSelect(ticket,SELECT_BY_TICKET);
   if (result==False) {
     int err = GetLastError();
     return(error(err));
   }
   int order_type = OrderType();
   if (result==False) {
     err = GetLastError();
     return(error(err));
   }
   return(DoubleToStr(order_type,5));
}

string process_OrderDelete(string command[]) {
   int ticket= StrToInteger(command[2]);
   bool result = OrderSelect(ticket,SELECT_BY_TICKET);
   if (result==False) {
     int err = GetLastError();
     return(error(err));
   }
   result = OrderDelete(ticket);
   if (result==False) {
     err = GetLastError();
     return(error(err));
   }
   return("true");
}


string process_OrderLots(string command[]) {
   int id = StrToInteger(command[2]);
   bool result = OrderSelect(id,SELECT_BY_TICKET);
   if (result==False) {
     int err = GetLastError();
     return(error(err));
   }
   return(DoubleToStr(OrderLots(),5));
}


//symbol,timeframe,period,shift
string process_iATR(string command[]) {
      //parse arguments
      string symbol = command[2];
      int timeframe = StrToInteger(command[3]);

      int period = StrToInteger(command[4]);
      int index = StrToInteger(command[5]);
      //body
      double atr = iATR(symbol,timeframe,period,index);
      int err = GetLastError();
      if (err!=0) 
         return(error(err));
      return(DoubleToStr(atr,5));
 }
string process_FantailVMA(string command[]) {
      //parse arguments
      string symbol = command[2];
      int timeframe = StrToInteger(command[3]);
      
      int ADX_Length = StrToInteger(command[4]);
      double Weighting = StrToDouble(command[5]);
      int MA_Length = StrToInteger(command[6]);
      int index = StrToInteger(command[7]);
      //body
      double vma = iCustom(symbol,timeframe,"FantailVMA3",ADX_Length,Weighting,MA_Length,1,0,index);
      int err = GetLastError();
      if (err!=0) 
         return(error(err));
      return(DoubleToStr(vma,5));
 }
 
 
string process_iMA(string command[]) {
      //parse arguments
      string symbol = command[2];
      int timeframe = StrToInteger(command[3]);
      int period = StrToInteger(command[4]);
      int mode = StrToInteger(command[5]);
      int price = StrToInteger(command[6]);
      int index = StrToInteger(command[7]);
      //body
      double ma = iMA(symbol,timeframe,period,0,mode,price,index);
      int err = GetLastError();
      if (err!=0) 
         return(error(err));
      return(DoubleToStr(ma,5));
 }

string process_iRSI(string commands[]) {
  string symbol = commands[2];
  
  int timeframe = StrToInteger(commands[3]);
  int period = StrToInteger(commands[4]);
  int applied_price = StrToInteger(commands[5]);
  int shift = StrToInteger(commands[6]);
  
  double result = iRSI(symbol,timeframe,period,applied_price,shift);
  int err = GetLastError();
  if (err!=0) 
    return(error(err));
  return(DoubleToStr(result,5));
}

string process_iStochastic(string commands[i]) {
  string symbol = commands[2];
  
  int timeframe = StrToInteger(commands[3]);
  int kperiod = StrToInteger(commands[4]);
  int dperiod = StrToInteger(commands[5]);
  int slowing = StrToInteger(commands[6]);
  int method = StrToInteger(commands[7]);
  int price_field = StrToInteger(commands[8]);
  int mode = StrToInteger(commands[9]);
  int shift = StrToInteger(commands[10]);
  double result = iStochastic(symbol,timeframe,kperiod,dperiod,slowing,method,price_field,mode,shift);
  int err = GetLastError();
  if (err!=0) 
    return(error(err));
  return(DoubleToStr(result,5));
}

string process(string id, string s) {
   string commands[];
   split(commands,s," ");
   string c = commands[1];
   string transaction_id = commands[0];
   string result = "error unknown";
   
   GetLastError();
   if (c =="iMA") 
     result = process_iMA(commands); 
   else if (c=="iATR")
     result = process_iATR(commands);
   else if (c=="AccountEquity")     
     result = process_AccountEquity(commands);
   else if (c=="FantailVMA") 
     result = process_FantailVMA(commands);
   else if (c=="iRSI") 
     result = process_iRSI(commands);
   else if (c=="iStochastic")
     result = process_iStochastic(commands);
   else if (c=="MarketInfo")
    result = process_MarketInfo(commands);  
   else if (c=="AccountFreeMargin")
     result = process_AccountFreeMargin(commands);
   else if (c=="OrderModify")
     result = process_OrderModify(commands);
   else if (c=="OrderDelete")
     result = process_OrderDelete(commands);
   else if (c=="OrderCloseTime")
     result = process_OrderCloseTime(commands);
   else if (c=="OrderType")
     result = process_OrderType(commands);
   else if (c=="OrderClose")
     result = process_OrderClose(commands);
   else if (c=="OrderSend")
     result = process_OrderSend(commands);
   else if (c=="OrdersTotal")
     result = process_OrdersTotal(commands); 
   else if (c=="AccountBalance")
     result = process_AccountBalance(commands);
   else if (c=="OrderLots")
     result = process_OrderLots(commands);
   else
     Print("didn't find command, which was "+c);
   return(transaction_id+" "+result+"\n");
 }
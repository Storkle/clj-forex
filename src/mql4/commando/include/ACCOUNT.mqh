string process_iHigh (string command[]) {
  string symbol = command[2];
  int timeframe = StrToInteger(command[3]);
  int shift = StrToInteger(command[4]);
  double val = iHigh(symbol,timeframe,shift);
  int err = GetLastError();
  if (err!=0)  
    return("error "+err);
  return(val);
 }
 
string process_iOpen (string command[]) {
  string symbol = command[2];
  int timeframe = StrToInteger(command[3]);
  int shift = StrToInteger(command[4]);
  double val = iOpen(symbol,timeframe,shift);
  int err = GetLastError();
  if (err!=0) 
    return("error "+err);
  return(val);
 }

string process_iClose (string command[]) {
  string symbol = command[2];
  int timeframe = StrToInteger(command[3]);
  int shift = StrToInteger(command[4]);
  double val = iClose(symbol,timeframe,shift);
  int err = GetLastError();
  if (err!=0) 
    return("error "+err);
  return(val);
 }
 
string process_iLow (string command[]) {
  string symbol = command[2];
  int timeframe = StrToInteger(command[3]);
  int shift = StrToInteger(command[4]);
  double val = iLow(symbol,timeframe,shift);
  int err = GetLastError();
  if (err!=0) 
    return("error "+err);
  return(val);
 }
 
 
string process_AccountBalance(string command[]) {
  return(AccountBalance());
}
string process_AccountCredit(string command[]) {
  return(AccountCredit());
}
string process_AccountCompany(string command[]) {
  return(AccountCompany());
}
string process_AccountCurrency(string command[]) {
  return(AccountCurrency());
}
string process_AccountEquity(string command[]) {
  return(AccountEquity());
}
string process_AccountFreeMargin(string command[]) {
  return(AccountFreeMargin());
}
string process_AccountLeverage(string command[]) {
  return(AccountLeverage());
}
string process_AccountMargin(string command[]) {
  return(AccountMargin());
}
string process_AccountName(string command[]) {
  return(AccountName());
}
string process_AccountNumber(string command[]) {
  return(AccountNumber());
}
string process_AccountProfit(string command[]) {
  return(AccountProfit());
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
  int color_of = StrToInteger(command[6]);
  bool success = OrderModify(ticket,price,stoploss,takeprofit,expiration,color_of);
  if (success!=true) {
    return("error "+GetLastError());
  }
  return("0");
}



string process_OrderClose(string command[]) {
  int ticket = StrToInteger(command[2]);
  double lots = StrToDouble(command[3]);
  double price = StrToDouble(command[4]);
  int slippage = StrToDouble(command[5]);
  int color_of =StrToInteger(command[6]);
  Print(ticket);
  Print(lots);
  Print(price);
  Print(slippage);
  Print(color_of);
  bool success = OrderClose(ticket,lots,price,slippage,color_of);
  if (success!=true) {
    return("error "+GetLastError());
  }
  return("0");
}




string process_MarketInfo(string command[]) {
  string symbol = command[2];
  int type = StrToInteger(command[3]);
  double result = MarketInfo(symbol,type);
  int err = GetLastError();
  if (err!=0)
    return("error "+err);
  return(result);
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
 int color_of = StrToInteger(command[9]);
 
 int ticket = OrderSend(symbol,cmd,volume,norm(symbol,price),slippage,stoploss,takeprofit,NULL,0,expiration,color_of);
 int err = GetLastError();
 Print("ticket is "+ticket);
 if (err!=0)
   return("error "+err);
 return(ticket);
}



string process_OrdersTotal (string command[]) {
  return(OrdersTotal());
}




string process_OrderCloseTime(string command[]) {
   int ticket= StrToInteger(command[2]);
   if (OrderSelect(ticket,SELECT_BY_TICKET,MODE_HISTORY)==true) {
     return(OrderCloseTime());
    }
    return("error "+GetLastError());
}

string process_OrderType(string command[]) {
   int ticket= StrToInteger(command[2]);
   bool result = OrderSelect(ticket,SELECT_BY_TICKET);
   if (result==False) {
     int err = GetLastError();
     return("error "+err);
   }
   int order_type = OrderType();
   if (result==False) {
     err = GetLastError();
     return("error " + err);
   }
   return(order_type);
}

string process_OrderDelete(string command[]) {
   int ticket= StrToInteger(command[2]);
   bool result = OrderSelect(ticket,SELECT_BY_TICKET);
   if (result==False) {
     int err = GetLastError();
     return("error "+err);
   }
   result = OrderDelete(ticket);
   if (result==False) {
     err = GetLastError();
     return("error "+err);
   }
   return("0");
}


string process_OrderLots(string command[]) {
   int id = StrToInteger(command[2]);
   bool result = OrderSelect(id,SELECT_BY_TICKET);
   if (result==False) {
     int err = GetLastError();
     return("error " +err);
   }
   return(OrderLots());
}



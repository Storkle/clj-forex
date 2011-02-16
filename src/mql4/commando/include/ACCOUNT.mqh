#include <utils.mqh>

int process_ACCOUNT (string c,string command[]) {
  if (c=="AccountBalance") {
    process_AccountBalance(command);
  } else if (c=="AccountCredit") {
    process_AccountCredit(command);
  } else if (c=="AccountCompany") {
    process_AccountCompany(command);
  } else if (c=="AccountCurrency") {
    process_AccountCurrency(command);
  } else if (c=="AccountEquity") {
    process_AccountEquity(command);
  } else if (c=="AccountFreeMargin") {
    process_AccountFreeMargin(command);
  } else if (c=="AccountLeverage") {
    process_AccountLeverage(command);
  } else if (c=="AccountMargin") {
    process_AccountMargin(command);
  } else if (c=="AccountName") {
    process_AccountName(command);
  } else if (c=="AccountNumber") {
    process_AccountNumber(command);
  } else if (c=="AccountProfit") {
    process_AccountProfit(command);
  } else if (c=="AccountServer") {
    process_AccountServer(command);
  } else {
    return(-1);
  }
  return(0);
}

void process_AccountBalance(string command[]) {
  send_double(AccountBalance());
}
void process_AccountCredit(string command[]) {
  send_double(AccountCredit());
}
void process_AccountCompany(string command[]) {
  send_string(AccountCompany());
}
void process_AccountCurrency(string command[]) {
  send_string(AccountCurrency());
}
void process_AccountEquity(string command[]) {
  send_double(AccountEquity());
}
void process_AccountFreeMargin(string command[]) {
  send_double(AccountFreeMargin());
}
void process_AccountLeverage(string command[]) {
  send_int(AccountLeverage());
}
void process_AccountMargin(string command[]) {
  send_double(AccountMargin());
}
void process_AccountName(string command[]) {
  send_string(AccountName());
}
void process_AccountNumber(string command[]) {
  send_string(AccountNumber());
}
void process_AccountProfit(string command[]) {
  send_double(AccountProfit());
}
void process_AccountServer(string command[]) {
  send_string(AccountServer());
}
///
///

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









#include <utils.mqh>

int process_ACCOUNT (string c,string command[]) {
  if (c=="TimeCurrent") {
    process_TimeCurrent(command);
  }
  else if (c=="TimeLocal") {
    process_TimeLocal(command);
  }
  else if (c=="AccountBalance") {
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
  } else if (c=="OrderModify") {
    process_OrderModify(command);
  } else if (c=="OrderClose") {
    process_OrderClose(command);
  } else if (c=="MarketInfo") {
    process_MarketInfo(command);
  } else if (c=="OrderSend") {
    process_OrderSend(command);
  } else if (c=="OrdersTotal") {
    process_OrdersTotal(command);
  } else if (c=="OrderCloseTime") {
    process_OrderCloseTime(command);
  } else if (c=="OrderType") {
    process_OrderType(command);
  } else if (c=="OrderDelete") {
    process_OrderDelete(command);
  } else if (c=="OrderLots") {
    process_OrderLots(command);
  } else if (c=="IsConnected") {
    process_IsConnected(command);
  } else if (c=="IsDemo") {
    process_IsDemo(command);
  } else if (c=="IsTradeAllowed") {
    process_IsTradeAllowed(command);
  } else {
    return(-1);
  }
  return(0);
}


void process_TimeCurrent(string command[]) {
 send_long(TimeCurrent());
}

void process_TimeLocal(string command[]) {
 send_long(TimeLocal());
}

void process_IsConnected(string command[]) {
 send_boolean(IsConnected());

}
void process_IsDemo(string command[]) {
 send_boolean(IsDemo());

}
void process_IsTradeAllowed(string command[]) {
 send_boolean(IsTradeAllowed());
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


void process_OrderModify(string command[]) {
  int ticket = StrToInteger(command[1]);
  double price = StrToDouble(command[2]);
  double stoploss = StrToDouble(command[3]);
  double takeprofit = StrToDouble(command[4]);
  datetime expiration = 0;//make_datetime(command[5]);
  int color_of = StrToInteger(command[5]);
  bool success = OrderModify(ticket,price,stoploss,takeprofit,expiration,color_of);
  if (success!=true) {
    send_error(GetLastError());
    return;
  }
  send_boolean(true);
}
void process_OrderClose(string command[]) {
  int ticket = StrToInteger(command[1]);
  double lots = StrToDouble(command[2]);
  double price = StrToDouble(command[3]);
  int slippage = StrToDouble(command[4]);
  int color_of =StrToInteger(command[5]);
  
  OrderSelect(ticket,SELECT_BY_TICKET);
  double old_lots = OrderLots();
  
  bool success = OrderClose(ticket,lots,price,slippage,color_of);
  if (success!=true) {
    send_error(GetLastError());
    return;
  }
  
  if (old_lots!=lots && OrderSelect(OrdersTotal()-1, SELECT_BY_POS, MODE_TRADES)==true) {
    send_string(OrderTicket()); 
  } else { 
    send_boolean (true);
  }
}

string process_MarketInfo(string command[]) {
  string symbol = command[1];
  int type = StrToInteger(command[2]);
  double result = MarketInfo(symbol,type);
  int err = GetLastError();
  if (err!=0) {
    send_error(GetLastError());
    return;
  }
  send_double(result);
}

double norm (string symbol,double d) {
  return(NormalizeDouble(d,MarketInfo(symbol,MODE_DIGITS)));
}

void process_OrderSend(string command[]) {
 string symbol = command[1];
 int cmd = StrToInteger(command[2]);
 double volume = StrToDouble(command[3]);
 double price = norm(symbol,StrToDouble(command[4]));
 int slippage = StrToInteger(command[5]);
 double stoploss = norm(symbol,StrToDouble(command[6]));
 double takeprofit = norm(symbol,StrToDouble(command[7]));
 datetime expiration = 0;//make_datetime(command[8]);
 int color_of = StrToInteger(command[8]);
 
 int ticket = OrderSend(symbol,cmd,volume,norm(symbol,price),slippage,stoploss,takeprofit,NULL,0,expiration,color_of);
 int err = GetLastError();
 if (err!=0) {
   send_error(err);
   return;
 }
 send_string(ticket);
}

void process_OrdersTotal (string command[]) {
  send_long(""+OrdersTotal());
}

void process_OrderCloseTime(string command[]) {
   int ticket= StrToInteger(command[1]);
   if (OrderSelect(ticket,SELECT_BY_TICKET,MODE_HISTORY)==true) {
     send_long(OrderCloseTime());
     return;
    } 
    send_error(GetLastError());
}

void process_OrderType(string command[]) {
   int ticket= StrToInteger(command[1]);
   if (OrderSelect(ticket,SELECT_BY_TICKET)==true) {
     send_int(OrderType());
     return;
   }
   send_error(GetLastError());
}

string process_OrderDelete(string command[]) {
   int ticket= StrToInteger(command[1]);
   if (OrderSelect(ticket,SELECT_BY_TICKET)==False) {
     send_error(GetLastError());
     return;
   }
   if (OrderDelete(ticket)==False) {
     send_error(GetLastError());
     return;
   }
   send_string("true");
}

string process_OrderLots(string command[]) {
   int id = StrToInteger(command[1]);
   if (OrderSelect(id,SELECT_BY_TICKET)==False) {
     send_error(GetLastError());
     return;
   }
   send_double(OrderLots());
}









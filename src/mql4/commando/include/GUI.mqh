#include <utils.mqh>

void process_WindowRedraw(string &req[]) {
  WindowRedraw();
}

void process_ObjectCreate(string &req[]) {
  string name = req[8];
  int type = StrToInteger(req[1]);
  int window = 0;
  int time1 = StrToInteger(req[2]);
  double price1 = StrToDouble(req[3]);
  int time2 = StrToInteger(req[4]);
  double price2 = StrToDouble(req[5]);
  int time3 = StrToInteger(req[6]);
  double price3 = StrToDouble(req[7]);
  bool result = ObjectCreate(name,type,window,time1,price1,time2,price2,time3,price3);
  if (result==false) {
    send_error(GetLastError());
    return;
  }
  send_boolean(true);
}

void process_ObjectDelete(string &req[]) {
  string name = req[1];
  if(!ObjectDelete(name)) {
    send_boolean(false);
    return;
  }
  send_boolean(true);
}

void process_ObjectGet(string &req[]) {
  double ret = ObjectGet(req[2],StrToInteger(req[1]));
  int err = GetLastError();
  if (err!=0) {
    send_error(err); 
    return;
  }
  send_double(ret);
}



void process_ObjectSet(string &req[]) {
 string name = req[3];
 int index = StrToInteger(req[1]);
 double value = StrToDouble(req[2]); 
 if(!ObjectSet(name,index,value)) {
   send_error(GetLastError());
   return;
 }
 send_boolean(true);
}

void process_ObjectsTotal(string &req[]) {
 send_int(ObjectsTotal());
}

void process_ObjectName(string &req[]) {
 string name = ObjectName(StrToInteger(req[1]));
 int err = GetLastError();
 if (err!=0) {
  send_string("");
  return;
 }
 send_string(name);
}

void process_ObjectsDeleteAll(string &req[]) {
 ObjectsDeleteAll(); 
 send_boolean(true);
}


void process_ObjectDescription(string &req[]) {
 string description = ObjectDescription(req[1]);
 int err = GetLastError();
 if (err!=0) {
   send_string("");
   return;
 }
 send_string(description);
}

int process_GUI(string c,string command[]) {
  if (c=="WindowRedraw") {
    process_WindowRedraw(command);
  }
  else if (c=="ObjectDescription") 
    process_ObjectDescription(command);
  else if (c=="ObjectsDeleteAll")
    process_ObjectsDeleteAll(command);
  else if (c=="ObjectName")
    process_ObjectName(command);
  else if (c=="ObjectsTotal")
    process_ObjectsTotal(command);
  else if (c=="ObjectSet")
    process_ObjectSet(command);
  else if (c=="ObjectGet")
    process_ObjectGet(command);
  else if (c=="ObjectDelete")
    process_ObjectDelete(command);
  else if (c=="ObjectCreate")
    process_ObjectCreate(command);
  else
    return(-1);
  return(0);
}
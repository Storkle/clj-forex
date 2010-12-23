string process_ObjectCreate(string &req[]) {
  string name = req[2];
  int type = StrToInteger(req[3]);
  int window = 0;
  int time1 = StrToInteger(req[4]);
  double price1 = StrToDouble(req[5]);
  int time2 = StrToInteger(req[6]);
  double price2 = StrToDouble(req[7]);
  int time3 = StrToInteger(req[8]);
  double price3 = StrToDouble(req[9]);
  bool result = ObjectCreate(name,type,window,time1,price1,time2,price2,time3,price3);
  if (result==false) {
    return("error "+GetLastError());
  }
  return("0");
}

string process_ObjectDelete(string &req[]) {
  string name = req[2];
  if(!ObjectDelete(name))
    return("error "+GetLastError());
  return("0");
}

string process_ObjectGet(string &req[]) {
  double ret = ObjectGet(req[2],StrToInteger(req[3]));
  int err = GetLastError();
  if (err!=0) 
    return("error "+err);
  return(ret);
}

string process_ObjectSet(string &req[]) {
 string name = req[2];
 int index = StrToInteger(req[3]);
 double value = StrToDouble(req[4]);
 if(!ObjectSet(name,index,value))
   return("error "+GetLastError());
 return("0");
}

string process_ObjectsTotal(string &req[]) {
 return (ObjectsTotal());
}

string process_ObjectName(string &req[]) {
 string name = ObjectName(StrToInteger(req[2]));
 int err = GetLastError();
 if (err!=0)
  return("error "+err);
 return(name);
}

string process_ObjectsDeleteAll(string &req[]) {
 ObjectsDeleteAll(); 
 return("0");
}


 
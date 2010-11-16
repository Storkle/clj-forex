
string error (int code) {
  return("error "+code);
}

void split(string& arr[], string str) 
{
  string sym = " ";
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
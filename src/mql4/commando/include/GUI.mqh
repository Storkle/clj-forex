
#import "user32.dll"
   int GetAncestor(int hWnd, int gaFlags);
   int GetDlgItem(int hDlg, int nIDDlgItem);
   int PostMessageA(int hWnd, int Msg, int wParam, int lParam);
#import

#define WM_COMMAND   0x0111
#define WM_KEYDOWN   0x0100
#define VK_HOME      0x0024
#define VK_DOWN      0x0028

//+------------------------------------------------------------------+
//| script program start function                                    |
//+------------------------------------------------------------------+

//+------------------------------------------------------------------+
//| Open a new chart                                                 |
//+------------------------------------------------------------------+
int ChartWindow(string SymbolName)
{
   int hFile, SymbolsTotal, hTerminal, hWnd;

   hFile = FileOpenHistory("symbols.sel", FILE_BIN|FILE_READ);
   if(hFile < 0) return(-1);

   SymbolsTotal = (FileSize(hFile) - 4) / 128;
   FileSeek(hFile, 4, SEEK_SET);

   hTerminal = GetAncestor(WindowHandle(Symbol(), Period()), 2);

   hWnd = GetDlgItem(hTerminal, 0xE81C);
   hWnd = GetDlgItem(hWnd, 0x50);
   hWnd = GetDlgItem(hWnd, 0x8A71);

   PostMessageA(hWnd, WM_KEYDOWN, VK_HOME, 0);

   for(int i = 0; i < SymbolsTotal; i++)
   {
      if(FileReadString(hFile, 12) == SymbolName)
      {
         PostMessageA(hTerminal, WM_COMMAND, 33160, 0);
         return(0);
      }
      PostMessageA(hWnd, WM_KEYDOWN, VK_DOWN, 0);
      FileSeek(hFile, 116, SEEK_CUR);
   }

   FileClose(hFile);

   return(-1);
}






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


 
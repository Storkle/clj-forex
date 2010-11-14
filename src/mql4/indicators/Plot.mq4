//NOTE: this is for debugging purposes. the file out.txt is created in 
//the file directory of metatrader (see Test.java in forex.indicators.test)
//this is especially useful when translating metatrader indicators!
//it is really basic right now and only does one indicator in one window \
//with a line connection. you would have to change it for others
#property indicator_color1    LightBlue
#property indicator_chart_window
extern string file = "out.txt";
double Indicator[];
//+------------------------------------------------------------------+
//| Custom indicator initialization function                         |
//+------------------------------------------------------------------+
int init()
  {
//---- indicators
//----
   IndicatorBuffers(1);
   SetIndexStyle(0,DRAW_LINE);
   SetIndexBuffer(0,Indicator);
   return(0);
  }
//+------------------------------------------------------------------+
//| Custom indicator deinitialization function                       |
//+------------------------------------------------------------------+
int deinit()
  {
   return(0);
  
  }
  
//+------------------------------------------------------------------+
//| Custom indicator iteration function                              |
//+------------------------------------------------------------------+
int start()
  {
   
      //read file and plot!
      int handle = FileOpen(file,FILE_READ|FILE_CSV,' ');
      if (handle<0) {
         int error = GetLastError();
         Print("error: "+error);
         return(-1);
      }
      int amount = FileReadNumber(handle);
      Print("amount is "+amount);
      for (int i=0;i<amount;i++) 
        Indicator[i] = FileReadNumber(handle);
      Print("achieved it "+Indicator[0]);
      FileClose(handle);

   return(0);
  }
//+------------------------------------------------------------------+

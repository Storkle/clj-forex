bool protocol_found = false;
string Custom_Momentum (string command[]) { 
string symbol = command[2];
int timeframe = StrToInteger(command[3]);
int mode = StrToInteger(command[4]);
int shift = StrToInteger(command[5]);
int i_MomPeriod = StrToInteger(command[6]); double val = iCustom(symbol,timeframe,"Momentum",i_MomPeriod,mode,shift); 
int err = GetLastError();
if(err!=0)
  return("error " + err);
return(val); 
}

string Custom_Parabolic (string command[]) { 
string symbol = command[2];
int timeframe = StrToInteger(command[3]);
int mode = StrToInteger(command[4]);
int shift = StrToInteger(command[5]);
double i_Step = StrToDouble(command[6]);
double i_Maximum = StrToDouble(command[7]); double val = iCustom(symbol,timeframe,"Parabolic",i_Step,i_Maximum,mode,shift); 
int err = GetLastError();
if(err!=0)
  return("error " + err);
return(val); 
}

string Custom_Heiken_Ashi (string command[]) { 
string symbol = command[2];
int timeframe = StrToInteger(command[3]);
int mode = StrToInteger(command[4]);
int shift = StrToInteger(command[5]);
color i_color1 = StrToInteger(command[6]);
color i_color2 = StrToInteger(command[7]);
color i_color3 = StrToInteger(command[8]);
color i_color4 = StrToInteger(command[9]); double val = iCustom(symbol,timeframe,"Heiken Ashi",i_color1,i_color2,i_color3,i_color4,mode,shift); 
int err = GetLastError();
if(err!=0)
  return("error " + err);
return(val); 
}

string Custom_iExposure (string command[]) { 
string symbol = command[2];
int timeframe = StrToInteger(command[3]);
int mode = StrToInteger(command[4]);
int shift = StrToInteger(command[5]);
color i_ExtColor = StrToInteger(command[6]); double val = iCustom(symbol,timeframe,"iExposure",i_ExtColor,mode,shift); 
int err = GetLastError();
if(err!=0)
  return("error " + err);
return(val); 
}

string Custom_Awesome (string command[]) { 
string symbol = command[2];
int timeframe = StrToInteger(command[3]);
int mode = StrToInteger(command[4]);
int shift = StrToInteger(command[5]);
 double val = iCustom(symbol,timeframe,"Awesome",mode,shift); 
int err = GetLastError();
if(err!=0)
  return("error " + err);
return(val); 
}

string Custom_Stochastic (string command[]) { 
string symbol = command[2];
int timeframe = StrToInteger(command[3]);
int mode = StrToInteger(command[4]);
int shift = StrToInteger(command[5]);
int i_KPeriod = StrToInteger(command[6]);
int i_DPeriod = StrToInteger(command[7]);
int i_Slowing = StrToInteger(command[8]); double val = iCustom(symbol,timeframe,"Stochastic",i_KPeriod,i_DPeriod,i_Slowing,mode,shift); 
int err = GetLastError();
if(err!=0)
  return("error " + err);
return(val); 
}

string Custom_Bands (string command[]) { 
string symbol = command[2];
int timeframe = StrToInteger(command[3]);
int mode = StrToInteger(command[4]);
int shift = StrToInteger(command[5]);
int i_BandsPeriod = StrToInteger(command[6]);
int i_BandsShift = StrToInteger(command[7]);
double i_BandsDeviations = StrToDouble(command[8]); double val = iCustom(symbol,timeframe,"Bands",i_BandsPeriod,i_BandsShift,i_BandsDeviations,mode,shift); 
int err = GetLastError();
if(err!=0)
  return("error " + err);
return(val); 
}

string Custom_ZigZag (string command[]) { 
string symbol = command[2];
int timeframe = StrToInteger(command[3]);
int mode = StrToInteger(command[4]);
int shift = StrToInteger(command[5]);
int i_ExtDepth = StrToInteger(command[6]);
int i_ExtDeviation = StrToInteger(command[7]);
int i_ExtBackstep = StrToInteger(command[8]); double val = iCustom(symbol,timeframe,"ZigZag",i_ExtDepth,i_ExtDeviation,i_ExtBackstep,mode,shift); 
int err = GetLastError();
if(err!=0)
  return("error " + err);
return(val); 
}

string Custom_RSI (string command[]) { 
string symbol = command[2];
int timeframe = StrToInteger(command[3]);
int mode = StrToInteger(command[4]);
int shift = StrToInteger(command[5]);
int i_RSIPeriod = StrToInteger(command[6]); double val = iCustom(symbol,timeframe,"RSI",i_RSIPeriod,mode,shift); 
int err = GetLastError();
if(err!=0)
  return("error " + err);
return(val); 
}

string Custom_ATR (string command[]) { 
string symbol = command[2];
int timeframe = StrToInteger(command[3]);
int mode = StrToInteger(command[4]);
int shift = StrToInteger(command[5]);
int i_AtrPeriod = StrToInteger(command[6]); double val = iCustom(symbol,timeframe,"ATR",i_AtrPeriod,mode,shift); 
int err = GetLastError();
if(err!=0)
  return("error " + err);
return(val); 
}

string Custom_Bears (string command[]) { 
string symbol = command[2];
int timeframe = StrToInteger(command[3]);
int mode = StrToInteger(command[4]);
int shift = StrToInteger(command[5]);
int i_BearsPeriod = StrToInteger(command[6]); double val = iCustom(symbol,timeframe,"Bears",i_BearsPeriod,mode,shift); 
int err = GetLastError();
if(err!=0)
  return("error " + err);
return(val); 
}

string Custom_Bulls (string command[]) { 
string symbol = command[2];
int timeframe = StrToInteger(command[3]);
int mode = StrToInteger(command[4]);
int shift = StrToInteger(command[5]);
int i_BullsPeriod = StrToInteger(command[6]); double val = iCustom(symbol,timeframe,"Bulls",i_BullsPeriod,mode,shift); 
int err = GetLastError();
if(err!=0)
  return("error " + err);
return(val); 
}

string Custom_OsMA (string command[]) { 
string symbol = command[2];
int timeframe = StrToInteger(command[3]);
int mode = StrToInteger(command[4]);
int shift = StrToInteger(command[5]);
int i_FastEMA = StrToInteger(command[6]);
int i_SlowEMA = StrToInteger(command[7]);
int i_SignalSMA = StrToInteger(command[8]); double val = iCustom(symbol,timeframe,"OsMA",i_FastEMA,i_SlowEMA,i_SignalSMA,mode,shift); 
int err = GetLastError();
if(err!=0)
  return("error " + err);
return(val); 
}

string Custom_MACD (string command[]) { 
string symbol = command[2];
int timeframe = StrToInteger(command[3]);
int mode = StrToInteger(command[4]);
int shift = StrToInteger(command[5]);
int i_FastEMA = StrToInteger(command[6]);
int i_SlowEMA = StrToInteger(command[7]);
int i_SignalSMA = StrToInteger(command[8]); double val = iCustom(symbol,timeframe,"MACD",i_FastEMA,i_SlowEMA,i_SignalSMA,mode,shift); 
int err = GetLastError();
if(err!=0)
  return("error " + err);
return(val); 
}

string Custom_Alligator (string command[]) { 
string symbol = command[2];
int timeframe = StrToInteger(command[3]);
int mode = StrToInteger(command[4]);
int shift = StrToInteger(command[5]);
int i_JawsPeriod = StrToInteger(command[6]);
int i_JawsShift = StrToInteger(command[7]);
int i_TeethPeriod = StrToInteger(command[8]);
int i_TeethShift = StrToInteger(command[9]);
int i_LipsPeriod = StrToInteger(command[10]);
int i_LipsShift = StrToInteger(command[11]); double val = iCustom(symbol,timeframe,"Alligator",i_JawsPeriod,i_JawsShift,i_TeethPeriod,i_TeethShift,i_LipsPeriod,i_LipsShift,mode,shift); 
int err = GetLastError();
if(err!=0)
  return("error " + err);
return(val); 
}

string Custom_FantailVMA3 (string command[]) { 
string symbol = command[2];
int timeframe = StrToInteger(command[3]);
int mode = StrToInteger(command[4]);
int shift = StrToInteger(command[5]);
int i_ADX_Length = StrToInteger(command[6]);
double i_Weighting = StrToDouble(command[7]);
int i_MA_Length = StrToInteger(command[8]);
int i_MA_Mode = StrToInteger(command[9]); double val = iCustom(symbol,timeframe,"FantailVMA3",i_ADX_Length,i_Weighting,i_MA_Length,i_MA_Mode,mode,shift); 
int err = GetLastError();
if(err!=0)
  return("error " + err);
return(val); 
}

string Custom_CCI (string command[]) { 
string symbol = command[2];
int timeframe = StrToInteger(command[3]);
int mode = StrToInteger(command[4]);
int shift = StrToInteger(command[5]);
int i_CCIPeriod = StrToInteger(command[6]); double val = iCustom(symbol,timeframe,"CCI",i_CCIPeriod,mode,shift); 
int err = GetLastError();
if(err!=0)
  return("error " + err);
return(val); 
}

string Custom_Accelerator (string command[]) { 
string symbol = command[2];
int timeframe = StrToInteger(command[3]);
int mode = StrToInteger(command[4]);
int shift = StrToInteger(command[5]);
 double val = iCustom(symbol,timeframe,"Accelerator",mode,shift); 
int err = GetLastError();
if(err!=0)
  return("error " + err);
return(val); 
}

string Custom_Blazan_Dynamic_Stop (string command[]) { 
string symbol = command[2];
int timeframe = StrToInteger(command[3]);
int mode = StrToInteger(command[4]);
int shift = StrToInteger(command[5]);
int i_Contract_Step = StrToInteger(command[6]);
int i_Precision = StrToInteger(command[7]);
int i_Shift_Bars = StrToInteger(command[8]);
int i_Bars_Count = StrToInteger(command[9]); double val = iCustom(symbol,timeframe,"Blazan Dynamic Stop",i_Contract_Step,i_Precision,i_Shift_Bars,i_Bars_Count,mode,shift); 
int err = GetLastError();
if(err!=0)
  return("error " + err);
return(val); 
}

string Custom_Ichimoku (string command[]) { 
string symbol = command[2];
int timeframe = StrToInteger(command[3]);
int mode = StrToInteger(command[4]);
int shift = StrToInteger(command[5]);
int i_Tenkan = StrToInteger(command[6]);
int i_Kijun = StrToInteger(command[7]);
int i_Senkou = StrToInteger(command[8]); double val = iCustom(symbol,timeframe,"Ichimoku",i_Tenkan,i_Kijun,i_Senkou,mode,shift); 
int err = GetLastError();
if(err!=0)
  return("error " + err);
return(val); 
}

string Custom_Accumulation (string command[]) { 
string symbol = command[2];
int timeframe = StrToInteger(command[3]);
int mode = StrToInteger(command[4]);
int shift = StrToInteger(command[5]);
 double val = iCustom(symbol,timeframe,"Accumulation",mode,shift); 
int err = GetLastError();
if(err!=0)
  return("error " + err);
return(val); 
}

string Custom_Moving_Averages (string command[]) { 
string symbol = command[2];
int timeframe = StrToInteger(command[3]);
int mode = StrToInteger(command[4]);
int shift = StrToInteger(command[5]);
int i_MA_Period = StrToInteger(command[6]);
int i_MA_Shift = StrToInteger(command[7]);
int i_MA_Method = StrToInteger(command[8]); double val = iCustom(symbol,timeframe,"Moving Averages",i_MA_Period,i_MA_Shift,i_MA_Method,mode,shift); 
int err = GetLastError();
if(err!=0)
  return("error " + err);
return(val); 
}

string process_INDICATORS(string commands[]) {
string ret = "";string command=commands[1];
if (command=="0") {
}
else if (command=="Custom_Momentum") {
protocol_found=true;
ret = Custom_Momentum(commands);
}
else if (command=="Custom_Parabolic") {
protocol_found=true;
ret = Custom_Parabolic(commands);
}
else if (command=="Custom_Heiken_Ashi") {
protocol_found=true;
ret = Custom_Heiken_Ashi(commands);
}
else if (command=="Custom_iExposure") {
protocol_found=true;
ret = Custom_iExposure(commands);
}
else if (command=="Custom_Awesome") {
protocol_found=true;
ret = Custom_Awesome(commands);
}
else if (command=="Custom_Stochastic") {
protocol_found=true;
ret = Custom_Stochastic(commands);
}
else if (command=="Custom_Bands") {
protocol_found=true;
ret = Custom_Bands(commands);
}
else if (command=="Custom_ZigZag") {
protocol_found=true;
ret = Custom_ZigZag(commands);
}
else if (command=="Custom_RSI") {
protocol_found=true;
ret = Custom_RSI(commands);
}
else if (command=="Custom_ATR") {
protocol_found=true;
ret = Custom_ATR(commands);
}
else if (command=="Custom_Bears") {
protocol_found=true;
ret = Custom_Bears(commands);
}
else if (command=="Custom_Bulls") {
protocol_found=true;
ret = Custom_Bulls(commands);
}
else if (command=="Custom_OsMA") {
protocol_found=true;
ret = Custom_OsMA(commands);
}
else if (command=="Custom_MACD") {
protocol_found=true;
ret = Custom_MACD(commands);
}
else if (command=="Custom_Alligator") {
protocol_found=true;
ret = Custom_Alligator(commands);
}
else if (command=="Custom_FantailVMA3") {
protocol_found=true;
ret = Custom_FantailVMA3(commands);
}
else if (command=="Custom_CCI") {
protocol_found=true;
ret = Custom_CCI(commands);
}
else if (command=="Custom_Accelerator") {
protocol_found=true;
ret = Custom_Accelerator(commands);
}
else if (command=="Custom_Blazan_Dynamic_Stop") {
protocol_found=true;
ret = Custom_Blazan_Dynamic_Stop(commands);
}
else if (command=="Custom_Ichimoku") {
protocol_found=true;
ret = Custom_Ichimoku(commands);
}
else if (command=="Custom_Accumulation") {
protocol_found=true;
ret = Custom_Accumulation(commands);
}
else if (command=="Custom_Moving_Averages") {
protocol_found=true;
ret = Custom_Moving_Averages(commands);
}

return(ret);}
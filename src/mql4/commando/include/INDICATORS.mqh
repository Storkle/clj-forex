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

string Custom_swing_zz (string command[]) { 
string symbol = command[2];
int timeframe = StrToInteger(command[3]);
int mode = StrToInteger(command[4]);
int shift = StrToInteger(command[5]);
int i_minBars = StrToInteger(command[6]); double val = iCustom(symbol,timeframe,"swing_zz",i_minBars,mode,shift); 
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

string Custom_Rsi_BlackFeet_modded (string command[]) { 
string symbol = command[2];
int timeframe = StrToInteger(command[3]);
int mode = StrToInteger(command[4]);
int shift = StrToInteger(command[5]);
int i_rsipds = StrToInteger(command[6]);
int i_ema = StrToInteger(command[7]);
int i_rsiapprice = StrToInteger(command[8]);
int i_Offset = StrToInteger(command[9]);
int i_price_offset = StrToInteger(command[10]); double val = iCustom(symbol,timeframe,"Rsi BlackFeet modded",i_rsipds,i_ema,i_rsiapprice,i_Offset,i_price_offset,mode,shift); 
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

string Custom_New_Candle_Alarm (string command[]) { 
string symbol = command[2];
int timeframe = StrToInteger(command[3]);
int mode = StrToInteger(command[4]);
int shift = StrToInteger(command[5]);
 double val = iCustom(symbol,timeframe,"New_Candle_Alarm",mode,shift); 
int err = GetLastError();
if(err!=0)
  return("error " + err);
return(val); 
}

string Custom_IINWMARROWS (string command[]) { 
string symbol = command[2];
int timeframe = StrToInteger(command[3]);
int mode = StrToInteger(command[4]);
int shift = StrToInteger(command[5]);
int i_FasterMode = StrToInteger(command[6]);
int i_FasterMA = StrToInteger(command[7]);
int i_SlowerMode = StrToInteger(command[8]);
int i_SlowerMA = StrToInteger(command[9]); double val = iCustom(symbol,timeframe,"IINWMARROWS",i_FasterMode,i_FasterMA,i_SlowerMode,i_SlowerMA,mode,shift); 
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

string Custom_Wick_O_Gram (string command[]) { 
string symbol = command[2];
int timeframe = StrToInteger(command[3]);
int mode = StrToInteger(command[4]);
int shift = StrToInteger(command[5]);
 double val = iCustom(symbol,timeframe,"Wick-O-Gram",mode,shift); 
int err = GetLastError();
if(err!=0)
  return("error " + err);
return(val); 
}

string Custom_CandleTime_THV (string command[]) { 
string symbol = command[2];
int timeframe = StrToInteger(command[3]);
int mode = StrToInteger(command[4]);
int shift = StrToInteger(command[5]);
color i_timeColor = StrToInteger(command[6]); double val = iCustom(symbol,timeframe,"CandleTime THV",i_timeColor,mode,shift); 
int err = GetLastError();
if(err!=0)
  return("error " + err);
return(val); 
}

string Custom_Tick_Value (string command[]) { 
string symbol = command[2];
int timeframe = StrToInteger(command[3]);
int mode = StrToInteger(command[4]);
int shift = StrToInteger(command[5]);
 double val = iCustom(symbol,timeframe,"Tick-Value",mode,shift); 
int err = GetLastError();
if(err!=0)
  return("error " + err);
return(val); 
}

string Custom_qqe_adv (string command[]) { 
string symbol = command[2];
int timeframe = StrToInteger(command[3]);
int mode = StrToInteger(command[4]);
int shift = StrToInteger(command[5]);
int i_SF = StrToInteger(command[6]);
int i_RSI_Period = StrToInteger(command[7]);
int i_DARFACTOR = StrToInteger(command[8]); double val = iCustom(symbol,timeframe,"qqe_adv",i_SF,i_RSI_Period,i_DARFACTOR,mode,shift); 
int err = GetLastError();
if(err!=0)
  return("error " + err);
return(val); 
}

string Custom_Bollinger_Squeeze_v8 (string command[]) { 
string symbol = command[2];
int timeframe = StrToInteger(command[3]);
int mode = StrToInteger(command[4]);
int shift = StrToInteger(command[5]);
string i_note1 = command[6];
string i_note2 = command[7];
int i_triggerType = StrToInteger(command[8]);
int i_stochPeriod_trigger1 = StrToInteger(command[9]);
int i_cciPeriod_trigger2 = StrToInteger(command[10]);
int i_rsiPeriod_trigger3 = StrToInteger(command[11]); double val = iCustom(symbol,timeframe,"Bollinger Squeeze v8",i_note1,i_note2,i_triggerType,i_stochPeriod_trigger1,i_cciPeriod_trigger2,i_rsiPeriod_trigger3,mode,shift); 
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

string Custom_MMPrice_THV (string command[]) { 
string symbol = command[2];
int timeframe = StrToInteger(command[3]);
int mode = StrToInteger(command[4]);
int shift = StrToInteger(command[5]);
string i_note1 = command[6];
bool i_Bid_Ask_Colors = StrToInteger(command[7]);
string i_note2 = command[8];
color i_FontColor = StrToInteger(command[9]);
string i_note3 = command[10];
int i_FontSize = StrToInteger(command[11]);
string i_note4 = command[12];
string i_FontType = command[13];
string i_note5 = command[14];
string i_note6 = command[15];
string i_note7 = command[16];
int i_WhatCorner = StrToInteger(command[17]); double val = iCustom(symbol,timeframe,"MMPrice THV",i_note1,i_Bid_Ask_Colors,i_note2,i_FontColor,i_note3,i_FontSize,i_note4,i_FontType,i_note5,i_note6,i_note7,i_WhatCorner,mode,shift); 
int err = GetLastError();
if(err!=0)
  return("error " + err);
return(val); 
}

string Custom_SweetSpots (string command[]) { 
string symbol = command[2];
int timeframe = StrToInteger(command[3]);
int mode = StrToInteger(command[4]);
int shift = StrToInteger(command[5]);
int i_NumLinesAboveBelow = StrToInteger(command[6]);
int i_SweetSpotMainLevels = StrToInteger(command[7]);
color i_LineColorMain = StrToInteger(command[8]);
int i_LineStyleMain = StrToInteger(command[9]);
bool i_ShowSubLevels = StrToInteger(command[10]);
int i_sublevels = StrToInteger(command[11]);
color i_LineColorSub = StrToInteger(command[12]);
int i_LineStyleSub = StrToInteger(command[13]); double val = iCustom(symbol,timeframe,"SweetSpots",i_NumLinesAboveBelow,i_SweetSpotMainLevels,i_LineColorMain,i_LineStyleMain,i_ShowSubLevels,i_sublevels,i_LineColorSub,i_LineStyleSub,mode,shift); 
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
else if (command=="Custom_swing_zz") {
protocol_found=true;
ret = Custom_swing_zz(commands);
}
else if (command=="Custom_Heiken_Ashi") {
protocol_found=true;
ret = Custom_Heiken_Ashi(commands);
}
else if (command=="Custom_Rsi_BlackFeet_modded") {
protocol_found=true;
ret = Custom_Rsi_BlackFeet_modded(commands);
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
else if (command=="Custom_New_Candle_Alarm") {
protocol_found=true;
ret = Custom_New_Candle_Alarm(commands);
}
else if (command=="Custom_IINWMARROWS") {
protocol_found=true;
ret = Custom_IINWMARROWS(commands);
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
else if (command=="Custom_Wick_O_Gram") {
protocol_found=true;
ret = Custom_Wick_O_Gram(commands);
}
else if (command=="Custom_CandleTime_THV") {
protocol_found=true;
ret = Custom_CandleTime_THV(commands);
}
else if (command=="Custom_Tick_Value") {
protocol_found=true;
ret = Custom_Tick_Value(commands);
}
else if (command=="Custom_qqe_adv") {
protocol_found=true;
ret = Custom_qqe_adv(commands);
}
else if (command=="Custom_Bollinger_Squeeze_v8") {
protocol_found=true;
ret = Custom_Bollinger_Squeeze_v8(commands);
}
else if (command=="Custom_FantailVMA3") {
protocol_found=true;
ret = Custom_FantailVMA3(commands);
}
else if (command=="Custom_CCI") {
protocol_found=true;
ret = Custom_CCI(commands);
}
else if (command=="Custom_MMPrice_THV") {
protocol_found=true;
ret = Custom_MMPrice_THV(commands);
}
else if (command=="Custom_SweetSpots") {
protocol_found=true;
ret = Custom_SweetSpots(commands);
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
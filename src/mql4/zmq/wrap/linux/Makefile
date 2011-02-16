SRCDIR                = .
SUBDIRS               =
DLLS                  = zmq_bind.dll
EXES                  =



### Common settings

CEXTRA                = -m32
CXXEXTRA              = -m32
RCEXTRA               =
DEFINES               = 
INCLUDE_PATH          = 
DLL_PATH              =
DLL_IMPORTS           =
LIBRARY_PATH          = 
LIBRARIES             = -lzmq 


### cudart.dll sources and settings

cudart_dll_MODULE     = zmq_bind.dll
cudart_dll_C_SRCS     = zmq_bind.c
cudart_dll_CXX_SRCS   =
cudart_dll_RC_SRCS    =
cudart_dll_LDFLAGS    = -shared \
			$(cudart_dll_MODULE:%=%.spec) \
			-mno-cygwin
cudart_dll_DLL_PATH   =
cudart_dll_DLLS       = 
cudart_dll_LIBRARY_PATH= .
cudart_dll_LIBRARIES  = zmq

cudart_dll_OBJS       = $(cudart_dll_C_SRCS:.c=.o) \
			$(cudart_dll_CXX_SRCS:.cpp=.o) \
			$(cudart_dll_RC_SRCS:.rc=.res)



### Global source lists

C_SRCS                = $(cudart_dll_C_SRCS)
CXX_SRCS              = $(cudart_dll_CXX_SRCS)
RC_SRCS               = $(cudart_dll_RC_SRCS)


### Tools

CC = winegcc -m32
CXX = wineg++ -m32
RC = wrc


### Generic targets

all: $(SUBDIRS) $(DLLS:%=%.so) $(EXES:%=%.so)

### Build rules

.PHONY: all clean dummy

$(SUBDIRS): dummy
	@cd $@ && $(MAKE)

# Implicit rules

.SUFFIXES: .cpp .rc .res
DEFINCL = $(INCLUDE_PATH) $(DEFINES) $(OPTIONS)

.c.o:
	$(CC) -c $(CFLAGS) $(CEXTRA) $(DEFINCL) -o $@ $<

.cpp.o:
	$(CXX) -c $(CXXFLAGS) $(CXXEXTRA) $(DEFINCL) -o $@ $<

.cxx.o:
	$(CXX) -c $(CXXFLAGS) $(CXXEXTRA) $(DEFINCL) -o $@ $<

.rc.res:
	$(RC) $(RCFLAGS) $(RCEXTRA) $(DEFINCL) -fo$@ $<

# Rules for cleaning

CLEAN_FILES     = y.tab.c y.tab.h lex.yy.c core *.orig *.rej \
                  \\\#*\\\# *~ *% .\\\#*

clean:: $(SUBDIRS:%=%/__clean__) $(EXTRASUBDIRS:%=%/__clean__)
	$(RM) $(CLEAN_FILES) $(RC_SRCS:.rc=.res) $(C_SRCS:.c=.o) $(CXX_SRCS:.cpp=.o)
	$(RM) $(DLLS:%=%.so) $(EXES:%=%.so) $(EXES:%.exe=%)

$(SUBDIRS:%=%/__clean__): dummy
	cd `dirname $@` && $(MAKE) clean

$(EXTRASUBDIRS:%=%/__clean__): dummy
	-cd `dirname $@` && $(RM) $(CLEAN_FILES)

### Target specific build rules
DEFLIB = $(LIBRARY_PATH) $(LIBRARIES) $(DLL_PATH) $(DLL_IMPORTS:%=-l%)

$(cudart_dll_MODULE).so: $(cudart_dll_OBJS)
	$(CC) $(cudart_dll_LDFLAGS) -o $@ $(cudart_dll_OBJS) $(cudart_dll_LIBRARY_PATH:%=-L%) $(DEFLIB) $(cudart_dll_DLLS:%=-l%) $(cudart_dll_LIBRARIES:%=-l%)




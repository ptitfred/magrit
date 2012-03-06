MAGRIT_TARGET=.
MAGRIT_VERSION=unknown
SOURCES=$(wildcard *.cpp)
OBJECTS=$(SOURCES:.cpp=.o)

# Linux
LDFLAGS=-lpthread -lboost_program_options -lboost_filesystem -lboost_system -Wl,-rpath,'$$ORIGIN'
CXXFLAGS=-std=c++0x -I. -pedantic -Wall -Werror -Wconversion -Wextra -Wno-unused -s -fPIC -DMAGRIT_VERSION=$(MAGRIT_VERSION)
CC=g++
EXT=so

# Win32 (Fedora 15 host)
#CXXFLAGS=-std=c++0x -I. -pedantic -Wall -Werror -Wconversion -Wextra -Wno-unused -s -D__CYGWIN__ -DMAGRIT_VERSION=$(MAGRIT_VERSION)
#LDFLAGS=-enable-auto-import -lpthread -L/usr/i686-pc-mingw32/sys-root/mingw/bin/ -lboost_program_options-gcc45-1_46_1 -lboost_filesystem-gcc45-1_46_1 -lboost_system-gcc45-1_46_1 -Wl,-rpath,'$$ORIGIN'
#CC=/usr/bin/i686-pc-mingw32-g++
#EXT=dll

all: magrit-build-log magrit-wait-for
	
magrit-build-log: libmagrit.$(EXT) 
	mkdir -p $(MAGRIT_TARGET)
	$(CC) -DMAGRIT_BUILD_LOG $(CXXFLAGS) main.cpp -L$(MAGRIT_TARGET) -lmagrit $(LDFLAGS) -o $(MAGRIT_TARGET)/$@

magrit-wait-for: libmagrit.$(EXT) 
	mkdir -p $(MAGRIT_TARGET)
	$(CC) -DMAGRIT_WAIT $(CXXFLAGS) main.cpp -L$(MAGRIT_TARGET) -lmagrit $(LDFLAGS) -o $(MAGRIT_TARGET)/$@

libmagrit.$(EXT): generic_command.o utils.o build_log.o wait.o
	mkdir -p $(MAGRIT_TARGET)
	$(CC) -shared $(LDFLAGS) $^ -o $(MAGRIT_TARGET)/$@ 

.cpp.o:
	$(CC) $(CXXFLAGS) -c $<

magrit: $(OBJECTS)
	mkdir -p $(MAGRIT_TARGET)
	$(CC) $(CXXFLAGS) $(LDFLAGS) $(OBJECTS) -o $@ 

clean:
	rm magrit $(MAGRIT_TARGET)/magrit-build-log $(MAGRIT_TARGET)/magrit-wait-for $(MAGRIT_TARGET)/*.so $(MAGRIT_TARGET)/*.dll *.o -f

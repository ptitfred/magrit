MAGRIT_TARGET=.
SOURCES=$(wildcard *.cpp)
OBJECTS=$(SOURCES:.cpp=.o)

# Linux
DEPS=-lpthread -lboost_program_options -lboost_filesystem -lboost_system -Wl,-rpath,'$$ORIGIN'
CPP_FLAGS=-std=c++0x -I. -pedantic -Wall -Werror -Wconversion -Wextra -Wno-unused -s
CC=g++
EXT=so

# Win32 (Fedora 15 host)
#CPP_FLAGS=-std=c++0x -I. -pedantic -Wall -Werror -Wconversion -Wextra -Wno-unused -s -D__CYGWIN__
#DEPS=-enable-auto-import -lpthread -L/usr/i686-pc-mingw32/sys-root/mingw/bin/ -lboost_program_options-gcc45-1_46_1 -lboost_filesystem-gcc45-1_46_1 -lboost_system-gcc45-1_46_1 -Wl,-rpath,'$$ORIGIN'
#CC=/usr/bin/i686-pc-mingw32-g++
#EXT=dll

all: magrit-build-log magrit-wait-for
	
magrit-build-log: libmagrit.$(EXT) 
	mkdir -p $(MAGRIT_TARGET)
	$(CC) -DMAGRIT_BUILD_LOG $(CPP_FLAGS) main.cpp $(DEPS) -L$(MAGRIT_TARGET) -lmagrit -o $(MAGRIT_TARGET)/$@

magrit-wait-for: libmagrit.$(EXT) 
	mkdir -p $(MAGRIT_TARGET)
	$(CC) -DMAGRIT_WAIT $(CPP_FLAGS) main.cpp $(DEPS) -L$(MAGRIT_TARGET) -lmagrit -o $(MAGRIT_TARGET)/$@

libmagrit.$(EXT): generic_command.o utils.o build_log.o wait.o
	mkdir -p $(MAGRIT_TARGET)
	$(CC) -shared $(DEPS) $^ -o $(MAGRIT_TARGET)/$@ 

.cpp.o:
	$(CC) $(CPP_FLAGS) -c $<

magrit: $(OBJECTS)
	mkdir -p $(MAGRIT_TARGET)
	$(CC) $(CPP_FLAGS) $(DEPS) $(OBJECTS) -o $@ 

clean:
	rm magrit $(MAGRIT_TARGET)/magrit-build-log $(MAGRIT_TARGET)/magrit-wait-for $(MAGRIT_TARGET)/*.so $(MAGRIT_TARGET)/*.dll *.o -f

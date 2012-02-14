MAGRIT_TARGET=.
SOURCES=$(wildcard *.cpp)
OBJECTS=$(SOURCES:.cpp=.o)
CPP_FLAGS=-std=c++0x -I. -pedantic -Wall -Werror -Wconversion -Wextra -Wno-unused -s 
DEPS=-lpthread -lboost_program_options -lboost_filesystem -lboost_system -Wl,-rpath,'$$ORIGIN'
CC=g++

all: magrit-build-log magrit-wait-for
	
magrit-build-log: libmagrit.so 
	mkdir -p $(MAGRIT_TARGET)
	$(CC) -DMAGRIT_BUILD_LOG $(CPP_FLAGS) main.cpp $(DEPS) -L$(MAGRIT_TARGET) -lmagrit -o $(MAGRIT_TARGET)/$@

magrit-wait-for: libmagrit.so 
	mkdir -p $(MAGRIT_TARGET)
	$(CC) -DMAGRIT_WAIT $(CPP_FLAGS) main.cpp $(DEPS) -L$(MAGRIT_TARGET) -lmagrit -o $(MAGRIT_TARGET)/$@

libmagrit.so: generic_command.o utils.o build_log.o wait.o
	mkdir -p $(MAGRIT_TARGET)
	$(CC) -shared $^ -o $(MAGRIT_TARGET)/$@

.cpp.o:
	$(CC) $(CPP_FLAGS) -c $<

magrit: $(OBJECTS)
	mkdir -p $(MAGRIT_TARGET)
	$(CC) $(CPP_FLAGS) $(DEPS) $(OBJECTS) -o $@ 

clean:
	rm magrit $(MAGRIT_TARGET)/magrit-build-log $(MAGRIT_TARGET)/magrit-wait-for $(MAGRIT_TARGET)/*.so *.o -f

MAGRIT_TARGET=.
SOURCES=$(wildcard *.cpp)
OBJECTS=$(SOURCES:.cpp=.o)
CPP_FLAGS=-s -Wall -Werror -std=c++0x -I.
DEPS=-lpthread -lboost_program_options -lboost_filesystem -lboost_system

all: magrit-build-log
	
magrit: $(OBJECTS)
	mkdir -p $(MAGRIT_TARGET)
	g++ $(CPP_FLAGS) $(DEPS) $(OBJECTS) -o $@ 

magrit-build-log: generic_command.o utils.o build_log.o
	mkdir -p $(MAGRIT_TARGET)
	g++ -DMAGRIT_BUILD_LOG $(CPP_FLAGS) $(DEPS) $^ main.cpp  -o $(MAGRIT_TARGET)/$@

.cpp.o:
	g++ $(CPP_FLAGS) -c $<

clean:
	rm magrit $(MAGRIT_TARGET)/magrit-build-log *.o -f

MAGRIT_TARGET=.
SOURCES=$(wildcard *.cpp)
OBJECTS=$(SOURCES:.cpp=.o)
EXECUTABLE=magrit
CPP_FLAGS=-O3 -s -Wall -Werror -std=c++0x
DEPS=-lpthread -lboost_program_options -lboost_filesystem -lboost_system

all: magrit-build-log
	
$(EXECUTABLE): $(OBJECTS)
	mkdir -p $(MAGRIT_TARGET)
	# TODO: remove -I ./ needed by Boost Process. Avoid boost::process propagating till main.cpp. 
	g++ $(CPP_FLAGS) $(DEPS) $(OBJECTS) -I./ -o $@ 

magrit-build-log: generic_command.o utils.o build_log.o
	# TODO: remove -I ./ needed by Boost Process. Avoid boost::process propagating till main.cpp. 
	g++ -DMAGRIT_BUILD_LOG $(CPP_FLAGS) $(DEPS) $^ main.cpp -I./ -o $(MAGRIT_TARGET)/$@

.cpp.o:
	# TODO: remove ./ (needed by Boost Process)
	g++ $(CPP_FLAGS) -c -I./ $<

clean:
	rm magrit magrit-build-log *.o -f

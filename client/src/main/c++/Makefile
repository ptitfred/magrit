SOURCES=$(wildcard *.cpp)
OBJECTS=$(SOURCES:.cpp=.o)
EXECUTABLE=magrit

all: $(SOURCES) $(EXECUTABLE)
	
$(EXECUTABLE): $(OBJECTS) 
	g++ -lboost_program_options $(OBJECTS) -o $@ 

.cpp.o:
	g++ -Wall -Werror -std=c++0x -g -c $<

clean:
	rm magrit *.o -f

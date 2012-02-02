SOURCES=$(wildcard *.cpp)
OBJECTS=$(SOURCES:.cpp=.o)
EXECUTABLE=magrit

all: $(SOURCES) $(EXECUTABLE)
	
$(EXECUTABLE): $(OBJECTS) 
	g++ -lboost_program_options $(OBJECTS) -o $@
	g++ -DMAGRIT_BUILD_LOG -Wall -Werror -std=c++0x -g -lboost_program_options $(OBJECTS:main.o=) main.cpp -o magrit-build-log

.cpp.o:
	g++ -Wall -Werror -std=c++0x -g -c $<

clean:
	rm magrit *.o -f

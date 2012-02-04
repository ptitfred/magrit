SOURCES=$(wildcard *.cpp)
OBJECTS=$(SOURCES:.cpp=.o)
EXECUTABLE=magrit

all: $(SOURCES) $(EXECUTABLE)
	
$(EXECUTABLE): $(OBJECTS) 
	# TODO: remove ./ (needed by Boost Process. In this case we have to
  # avoid boost::process propagating till main.cpp ).
	g++ -lpthread -lboost_program_options -lboost_filesystem -lboost_system $(OBJECTS) -I./ -o $@ 
	# TODO: remove ./ (needed by Boost Process. In this case we have to
  # avoid boost::process propagating till main.cpp ).
	g++ -DMAGRIT_BUILD_LOG -Wall -Werror -std=c++0x -g -lpthread -lboost_system -lboost_filesystem -lboost_program_options $(OBJECTS:main.o=) main.cpp -I./ -o magrit-build-log 

.cpp.o:
	# TODO: remove ./ (needed by Boost Process)
	g++ -Wall -Werror -std=c++0x -g -c -I./ $<

clean:
	rm magrit *.o -f

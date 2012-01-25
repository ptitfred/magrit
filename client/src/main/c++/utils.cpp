/**
 * Copyright 2011 Frederic Menou
 *
 * This file is part of Magrit.
 *
 * Magrit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Magrit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with Magrit.
 * If not, see <http://www.gnu.org/licenses/>.
 */
/////////////////////////////////////////////////////////////////////////
// MAGRIT 
#include "utils.hpp"
/////////////////////////////////////////////////////////////////////////
// STD
#include <stdexcept> 
#include <string.h>
#include <sstream>
/////////////////////////////////////////////////////////////////////////
// POPEN 
#include <stdio.h>
//#include <sys/types.h>
//#include <sys/wait.h>
//#include <unistd.h>
/////////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////////////////////
void clear_screen ()
{
  std::cout << "\x1B[2J\x1B[H";
}

/////////////////////////////////////////////////////////////////////////
std::string sanitize ( const std::string& str )
{
  // TODO: implement
  return str;
}

/////////////////////////////////////////////////////////////////////////
std::string sanitize_ssh_cmd ( const std::string& str )
{
  // TODO: implement
  return str;
}

/////////////////////////////////////////////////////////////////////////
std::string get_repo_name ()
{
  return sanitize ( "magrit" );
}

/////////////////////////////////////////////////////////////////////////
std::string get_magrit_host ()
{
  return sanitize ( "localhost" );
}

/////////////////////////////////////////////////////////////////////////
int get_magrit_port ()
{
  return 2022;
}

/////////////////////////////////////////////////////////////////////////
std::string get_magrit_user ()
{
  return sanitize ( "git" );
}

/////////////////////////////////////////////////////////////////////////
void send_ssh_command ( const std::string& cmd )
{
  std::stringstream ssh_cmd;

  ssh_cmd << "ssh -x -p " << get_magrit_port() << " "
          << get_magrit_user() << "@" << get_magrit_host()
          << " " << cmd ;

  std::cout << "Sending [" << ssh_cmd.str() << "]" << std::endl;

  FILE* output = popen ( cmd.c_str(), "r" );
  
  if ( output == NULL )
  {
    // error
    throw std::runtime_error ( strerror( errno ) );
  }
  else 
  {
    char line[256];

    while ( fgets ( line, sizeof line, output ) )
    {
      printf("-- %s", line);
    }

    int status = pclose ( output ) ;

    if ( status != 0 )
    {
      std::runtime_error ( strerror ( errno ) );
    }
  }
}

/////////////////////////////////////////////////////////////////////////
void send_ssh_command_bg ( const std::string& cmd )
{
  send_ssh_command ( cmd );
}


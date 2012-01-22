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

/////////////////////////////////////////////////////////////////////////
void clear_screen ()
{
  std::cout << "\x1B[2J\x1B[H";
}

/////////////////////////////////////////////////////////////////////////
const std::string& sanitize ( const std::string& str )
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
std::string get_repo_ssh_url ()
{
  return sanitize ( "git@github.com" );
}

/////////////////////////////////////////////////////////////////////////
std::string get_repo_host ()
{
  return sanitize ( "github.com" );
}

/////////////////////////////////////////////////////////////////////////
std::string get_repo_user ()
{
  return sanitize ( "git" );
}

/////////////////////////////////////////////////////////////////////////
std::string get_repo_port ()
{
  // TODO: according to the bash's regex, it's
  //       taking the user name??
  return sanitize ( "daniperez" );
}

/////////////////////////////////////////////////////////////////////////
void send_ssh_command ( const std::string& cmd )
{
  std::cout << "Sending [" << cmd << "]" << std::endl;
}

/////////////////////////////////////////////////////////////////////////
void send_ssh_command_bg ( int in_fd, int out_fd, const std::string& cmd )
{
  send_ssh_command ( cmd );
}


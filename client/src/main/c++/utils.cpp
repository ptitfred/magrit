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
#include <iterator>
//////////////////////////////////////////////////////////////////////////
// BOOST
#include <boost/lexical_cast.hpp> 
////////////////////////////////////////////////////////////////////////
// FORK 
#include <stdio.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <unistd.h>
/////////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////////////////////
void clear_screen ()
{
  std::cout << "\x1B[2J\x1B[H";
}

/////////////////////////////////////////////////////////////////////////
std::vector< std::string > split ( const std::string& input, char delimiter )
{
  std::vector< std::string > output;

  std::istringstream iss ( input );

  std::copy
  (
    std::istream_iterator<std::string>(iss),
    std::istream_iterator<std::string>(),
    std::back_inserter ( output )
  );

  return output;
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
boost::process::pipeline_entry send_ssh_command
( 
  const std::string& cmd, 
  boost::process::stream_behavior& _stdin,
  boost::process::stream_behavior& _stdout,
  boost::process::stream_behavior& _stderr,
  bool background
)
{
  std::string port
    = boost::lexical_cast<std::string>( get_magrit_port() ).c_str();

  std::string conn_str
    = ( get_magrit_user() + std::string("@") + get_magrit_host() ).c_str();

  std::vector < std::string > cmd_line = 
  {
    "ssh",
    "-x",
    "-p",
    port.c_str(),
    conn_str.c_str(),
    cmd.c_str()
  };

  boost::process::pipeline_entry prog
    = create_program ( cmd_line, _stdin, _stdout, _stderr );

  if ( background )
  {
    return prog;
  }
  else
  {
    std::vector < boost::process::pipeline_entry > entries;
     
    entries.push_back ( prog );

    boost::process::children child_prog
      = boost::process::launch_pipeline ( entries ); 

    boost::process::wait_children ( child_prog );

    return prog;
  }
}

/////////////////////////////////////////////////////////////////////////
void wait_children ( boost::process::child& child )
{
  boost::process::status stat = child.wait();

  if ( ! stat.exited() )
  {
    throw std::runtime_error
    (
      std::string ("Process ") +
      boost::lexical_cast < std::string > ( child.get_id() ) +
      std::string (" abruptly terminated.")
    );
  }
}

/////////////////////////////////////////////////////////////////////////
std::vector< std::string > get_git_commits ( const std::vector< std::string >& arguments )
{

  std::vector< std::string > cmd;
 
  cmd.insert ( cmd.end(), "git" );
  cmd.insert ( cmd.end(), "log" );
  cmd.insert ( cmd.end(), "--format=%H" );
  cmd.insert ( cmd.end(), arguments.begin(), arguments.end() );

  boost::process::pipeline_entry git_log
    = create_program 
      ( 
        cmd,
        boost::process::inherit_stream(),
        boost::process::capture_stream(),
        boost::process::inherit_stream()
      ); 

  std::vector < boost::process::pipeline_entry > entries;
  entries.push_back ( git_log ); 

  boost::process::children child_git_log
    = boost::process::launch_pipeline ( entries ); 
  
  std::stringstream hashes;

  boost::process::pistream& _stdout
    = child_git_log.front().get_stdout();

  std::string line;

  while ( std::getline ( _stdout, line ) )
  {
    hashes << line << std::endl; 
  }

  boost::process::wait_children ( child_git_log );

  return split ( hashes.str(), '\n' );
}

/////////////////////////////////////////////////////////////////////////
int get_num_exec_args ( const std::vector < std::string >& args )
{
  return args.size() + 1 /* ending NULL pointer */ ;
}

/////////////////////////////////////////////////////////////////////////
void get_exec_args ( const std::vector < std::string >& args, char** output )
{
  for ( uint i = 0 ; i < args.size() ; ++i )
  {
    // exec expects a non const despite it doesn't
    // touch its arguments.
    output[i] = const_cast<char*>( args[i].c_str() );
  }

  output[args.size()] = NULL;
}

/////////////////////////////////////////////////////////////////////////
boost::process::pipeline_entry create_program
(
  const std::vector< std::string >& arguments,
  boost::process::stream_behavior _stdin,
  boost::process::stream_behavior _stdout,
  boost::process::stream_behavior _stderr
)
{
  if ( arguments.size() < 1 )
  {
    throw std::logic_error ( "execute_program needs at least 1 argument" );
  }

  char * c_arguments [ get_num_exec_args ( arguments ) ];
  
  get_exec_args ( arguments, c_arguments );

  boost::process::context context;

  context.stdin_behavior = _stdin;
  context.stdout_behavior = _stdout;
  context.stderr_behavior = _stderr;

  return boost::process::pipeline_entry ( arguments[0], arguments, context );  
}


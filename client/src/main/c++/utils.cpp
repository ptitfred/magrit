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
boost::process::pipeline_entry ssh_command
( 
  const std::string& cmd
)
{
  std::string port
    = boost::lexical_cast<std::string>( get_magrit_port() ).c_str();

  std::string conn_str
    = ( get_magrit_user() + std::string("@") + get_magrit_host() ).c_str();

  std::vector < std::string > cmd_line = 
  {
    "-x",
    "-p",
    port.c_str(),
    conn_str.c_str(),
    cmd.c_str()
  };

  return start_pipeline_process
      (
        "ssh",
        cmd_line,
        boost::process::close_stream(),
        boost::process::inherit_stream(),
        boost::process::inherit_stream()
      );
}

/////////////////////////////////////////////////////////////////////////
boost::process::pipeline_entry
get_git_commits_cmd ( const std::vector< std::string >& git_args )
{
  std::vector< std::string > args;
 
  args.insert ( args.end(), "log" );
  args.insert ( args.end(), "--format=%H" );
  args.insert ( args.end(), git_args.begin(), git_args.end() );

  return start_pipeline_process 
  ( 
    "git",
    args,
    boost::process::close_stream(),
    boost::process::close_stream(),
    boost::process::inherit_stream()
  ); 
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
boost::process::child start_process
(
  const std::string& program,
  const std::vector< std::string >& arguments,
  boost::process::stream_behavior _stdin,
  boost::process::stream_behavior _stdout,
  boost::process::stream_behavior _stderr
)
{
  if ( arguments.size() < 1 )
  {
    throw std::logic_error ( "start_process needs at least 1 argument" );
  }

  boost::process::context context;

  context.stdin_behavior = _stdin;
  context.stdout_behavior = _stdout;
  context.stderr_behavior = _stderr;

  std::vector < std::string > boost_process_workaround_args;

  // TODO: bug in boost::process: doc says arguments to be passed
  //       but execve used by boost::process needs of the whole
  //       command line.
  boost_process_workaround_args.push_back ( program );
  boost_process_workaround_args.insert
    ( boost_process_workaround_args.end(), arguments.begin(), arguments.end() );

  return boost::process::launch
  (
    boost::process::find_executable_in_path ( program ),
    boost_process_workaround_args,
    context
  );
}

/////////////////////////////////////////////////////////////////////////
boost::process::pipeline_entry start_pipeline_process
(
  const std::string& program,
  const std::vector< std::string >& arguments,
  boost::process::stream_behavior _stdin,
  boost::process::stream_behavior _stdout,
  boost::process::stream_behavior _stderr
)
{
  if ( arguments.size() < 1 )
  {
    throw std::logic_error ( "start_process needs at least 1 argument" );
  }

  boost::process::context context;

  context.stdin_behavior = _stdin;
  context.stdout_behavior = _stdout;
  context.stderr_behavior = _stderr;

  std::vector < std::string > boost_process_workaround_args;

  // TODO: bug in boost::process: doc says arguments to be passed
  //       but execve used by boost::process needs of the whole
  //       command line.
  boost_process_workaround_args.push_back ( program );
  boost_process_workaround_args.insert
    ( boost_process_workaround_args.end(), arguments.begin(), arguments.end() );

  return boost::process::pipeline_entry
  (
    boost::process::find_executable_in_path ( program ),
    boost_process_workaround_args,
    context
  );
}


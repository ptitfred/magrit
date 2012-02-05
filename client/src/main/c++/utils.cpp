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
#include <istream>
//////////////////////////////////////////////////////////////////////////
// BOOST
#include <boost/lexical_cast.hpp> 
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
std::string magrit::read_one_output_line
( const char* cmd, const std::vector < std::string >& args )
{
  std::string output;

  start_process
  (
    cmd,
    args,
    boost::process::inherit_stream(),
    boost::process::capture_stream(),
    boost::process::inherit_stream(),
    [&output]( const std::string& line ) { output = line; }
  );

  return sanitize ( output );
}

/////////////////////////////////////////////////////////////////////////
std::string magrit::get_repo_remote_name ()
{
  try
  {
    return read_one_output_line
      (
        "git", 
        std::vector < std::string > { "config", "--get", "magrit.remote" }
      );
  }
  catch (...)
  {
    return "magrit";
  }
}

/////////////////////////////////////////////////////////////////////////
std::string magrit::get_repo_url ()
{
  std::string var
    = std::string ( "remote." ) + get_repo_remote_name () + std::string ( ".url" );

  std::string url 
    = read_one_output_line 
      ( 
        "git",
        std::vector < std::string > 
        { 
          "config", 
          "--local",
          var
        }
      );

  if ( url.empty () )
  {
    throw std::runtime_error
    ( 
      var + " variable not set. Did you use 'magrit config add <name>' ?"
    );
  }
  else
  {
    return url;
  }
}

/////////////////////////////////////////////////////////////////////////
std::string magrit::get_repo_name ()
{
  std::string url = get_repo_url ();

  size_t pos = url.find_last_of ( '/' );

  if ( pos == std::string::npos || pos == url.size() - 1 )
  {
    throw std::runtime_error
    (
      url + ": malformed url, no repo name supplied"
    );
  }

  return std::string ("/") + url.substr ( pos + 1 ) + std::string ("/");
}

/////////////////////////////////////////////////////////////////////////
std::string magrit::get_repo_host ()
{
  std::string url = get_repo_url ();

  size_t pos_at = url.find_first_of ( '@' );

  if ( pos_at == std::string::npos )
  {
    throw std::runtime_error ( url + ": malformed url, no user supplied" );
  }

  size_t pos_colon = url.find_first_of ( ':', pos_at );
  size_t pos_slash = url.find_first_of ( '/', pos_at );

  size_t end = -1;

  if ( pos_slash == std::string::npos )
  {
    throw std::runtime_error ( url + ": malformed url, no repo name supplied" );
  }
  else if ( pos_colon != std::string::npos )
  {
    end = std::min ( pos_colon, pos_slash );
  }
  else
  {
    end = pos_slash;
  }

  if ( end - pos_at <= 1 )
  {
    throw std::runtime_error ( url + ": malformed url, no host supplied" );
  }

  return url.substr ( pos_at + 1, end - pos_at - 1 );
}

/////////////////////////////////////////////////////////////////////////
int magrit::get_magrit_port ()
{
  return 2022;
}

/////////////////////////////////////////////////////////////////////////
std::string magrit::get_repo_user ()
{
  return sanitize ( "git" );
}

/////////////////////////////////////////////////////////////////////////
void magrit::start_process
(
  const std::string& program,
  const std::vector< std::string >& arguments,
  boost::process::stream_behavior _stdin,
  boost::process::stream_behavior _stdout,
  boost::process::stream_behavior _stderr,
  std::function<void (const std::string&)> line_processor,
  size_t limit_num_lines
)
{
  // std::cout << "Executing [" << program << ", "
  //           << join ( " ", arguments.begin(), arguments.end() )
  //           << "]" << std::endl;

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

  boost::process::child ch
    = boost::process::launch
      (
        boost::process::find_executable_in_path ( program ),
        boost_process_workaround_args,
        context
      );

  boost::process::pistream& is = ch.get_stdout();

  std::string line; 

  while ( std::getline ( is, line ) )
  {
    line_processor ( line );
  }

  if ( !is.eof() )
  {
    throw std::runtime_error
          ( "An error occurred before reading all the stdout" );
  }

  auto status = ch.wait();

  if ( !status.exited() )
  {
    // Warning: errno is not guaranteed to be wait's one.
    throw std::runtime_error ( strerror ( errno ) );
  }
  else if ( status.exit_status () != 0 )
  {
    throw std::runtime_error
    ( 
      std::string ( "[" ) + program + " " +
      join ( " ",  arguments.begin(), arguments.end() ) +
      std::string ( "] returned " ) +
      boost::lexical_cast < std::string > ( status.exit_status() )
    );
  }

  // std::cout << "Executed." << std::endl;
}

/////////////////////////////////////////////////////////////////////////
boost::process::children magrit::start_pipeline
(
  const std::vector < boost::process::pipeline_entry >& pipeline
)
{
  // std::cout << "Executing [" << program << ", "
  //           << join ( " ", arguments.begin(), arguments.end() )
  //           << "]" << std::endl;

  boost::process::children ch 
    = boost::process::launch_pipeline ( pipeline );

  boost::process::status exit_status
    = boost::process::wait_children ( ch ); 

  if ( !exit_status.exited () || exit_status.exit_status() != 0 )
  {
    std::stringstream pipe_str;

    for ( uint i = 0 ; i < pipeline.size() ; ++i )
    {
      if ( i > 0 )
      {
        pipe_str << " | ";
      }

      pipe_str
        << pipeline[i].executable << " " 
        << join 
           (
             " ",
             pipeline[i].arguments.begin(),
             pipeline[i].arguments.end()
           );
    }

    throw std::runtime_error
    (
      std::string ( "An error occurred in pipeline '" ) +
      pipe_str.str() +
      std::string ( "'" )
    );
  }

  // std::cout << "Executed." << std::endl;

  return ch; 
}

/////////////////////////////////////////////////////////////////////////
boost::process::pipeline_entry magrit::create_pipeline_process
(
  const std::string& program,
  const std::vector< std::string >& arguments,
  boost::process::stream_behavior _stdin,
  boost::process::stream_behavior _stdout,
  boost::process::stream_behavior _stderr
)
{
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


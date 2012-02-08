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
#include "build_log.hpp"
#include "utils.hpp"
/////////////////////////////////////////////////////////////////////////
// STD 
#include <iomanip>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
/////////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////////////////////
magrit::log::log ( generic_command* previous_subcommand )
  : generic_command ( previous_subcommand ),
    _log_options ("Log options"),
    _positional_parameters_desc
    ("Positional options (can be added to the end of argument list without the dashed string)")
{
  _log_options.add_options()
    ( "watch,w","activates the automatic refresh" );

  get_options().add ( _log_options );

  _positional_parameters.add("git-args", -1);

  _positional_parameters_desc.add_options()
    ("git-args", boost::program_options::value<std::vector<std::string>>(),
     "'git log' options and commit filters");

  get_options().add ( _positional_parameters_desc );
}

/////////////////////////////////////////////////////////////////////////
const char*
magrit::log::get_name() const
{
  if ( _previous_subcommand == nullptr )
  {
    return "magrit-build-log";
  }
  else
  {
    return "log"; 
  }
} 

/////////////////////////////////////////////////////////////////////////
const char* magrit::log::get_description() const
{
  return "Shows the status of the commits sent to the server";
}

/////////////////////////////////////////////////////////////////////////
const boost::program_options::positional_options_description&
magrit::log::get_positional_options () const
{
  return _positional_parameters;
}

/////////////////////////////////////////////////////////////////////////
void
magrit::log::process_parsed_options
(
  const std::vector<std::string>& arguments,
  const boost::program_options::variables_map& vm,
  bool allow_zero_arguments
)
const
{
  generic_command::process_parsed_options ( arguments, vm, true );

  std::vector< std::string > git_args;

  if ( vm.count ( "watch" ) )
  {
    clear_screen();
  }

  if ( vm.count ( "git-args" ) )
  {
    git_args = vm["git-args"].as< std::vector< std::string > >();
  }

  print_status ( git_args );
}

/////////////////////////////////////////////////////////////////////////
void
magrit::log::print_status ( const std::vector < std::string >& git_args )
const
{
  std::vector < boost::process::pipeline_entry > pipeline;

  add_process_to_pipeline
  (
    "git",
    std::vector < std::string >
    {
      "log",
      "--format=%H",
      join ( " ", git_args.begin(), git_args.end() )
    },
    boost::process::close_stream(),
    boost::process::close_stream(),
    boost::process::inherit_stream(),
    pipeline
  );
  
  add_process_to_pipeline
  ( 
    "ssh",
    std::vector < std::string >
    {
      "-x",
      "-p",
      boost::lexical_cast < std::string > ( get_magrit_port() ),
      get_repo_user() + std::string("@") + get_repo_host(),
      std::string("magrit status /") + get_repo_name() + std::string("/ -")
    },
    boost::process::close_stream(),
    boost::process::capture_stream(),
    boost::process::inherit_stream(),
    pipeline
  );

  boost::process::children statuses = start_pipeline ( pipeline );

  // We issue again a git log with color and the commit message.
  // For every line, we print the status previously fetched from
  // server. Note: it's theoretically possible that the previous
  // git log had less lines than the following one if a commit 
  // was pushed in between, but in practice the odds are very low
  // and the impact is very small.
  start_process
  (
    "git",
     std::vector < std::string >
     {
       "log",
       "--color=always",
       "--oneline",
       "-z",
       join ( " ", git_args.begin(), git_args.end() )
     },
     boost::process::inherit_stream(),
     boost::process::capture_stream(),
     boost::process::inherit_stream(),
     [&]( const std::string& line )
     { 
       std::string status;
       std::getline( statuses.back().get_stdout(), status );
       std::cout 
         << std::left << std::setw (77)
         << line << " | " << colorize_linux ( status ) << std::endl;
     }
  );
}

/////////////////////////////////////////////////////////////////////////
std::string
magrit::log::colorize_linux ( const std::string& status )
{
  std::stringstream output;

  for ( uint i = 0 ; i < status.size() ; ++i )
  {
    switch ( status[i] )
    {
      case 'O':
        output << cool ( status[i] );
        break;
      case 'E':
        output << error ( status[i] );
        break;
      case 'R':
        output << running ( status[i] );
        break;
      case 'P':
        output << pending ( status[i] );
        break;
      case '?':
        output << warning ( status[i] );
        break;
      default:
        output << status[i];
        break;
    }
  }

  return output.str();
}


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
// BOOST
#include "boost/process.hpp" 
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
  : generic_command ( previous_subcommand, true ),
    _log_options ("Log options")
{
  _log_options.add_options()
    ( "watch,w","activates the automatic refresh" );

  get_options().add ( _log_options );
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
void
magrit::log::process_parsed_options
(
  const std::vector<std::string>& arguments,
  const boost::program_options::variables_map& vm,
  const std::vector<std::string>& unrecognized_arguments,
  bool allow_zero_arguments
)
const
{
  generic_command::process_parsed_options
    ( arguments, vm, unrecognized_arguments, true );

  if ( vm.count ( "watch" ) )
  {
    throw std::runtime_error ( "not implemented yet" );
    watch_status ( unrecognized_arguments );
  }
  else
  {
    print_status ( unrecognized_arguments );
  }
}

/////////////////////////////////////////////////////////////////////////
void
magrit::log::watch_status ( const std::vector < std::string >& git_args )
const
{
  clear_screen();
}

/////////////////////////////////////////////////////////////////////////
void
magrit::log::print_status ( const std::vector < std::string >& git_args )
const
{
  std::vector < boost::process::pipeline_entry > pipeline;

  auto msg_width
    = color? get_message_max_width () + 8 : get_message_max_width();

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
       color?"--color=always":"--color=never",
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
         << std::left << std::setw ( msg_width )
         << cut_message ( line, msg_width ) << " | "
         << colorize_linux ( status , color )
         << std::endl;
     }
  );
}

/////////////////////////////////////////////////////////////////////////
std::string
magrit::log::colorize_linux ( const std::string& status, bool color )
{
  std::stringstream output;

  for ( uint i = 0 ; i < status.size() ; ++i )
  {
    switch ( status[i] )
    {
      case 'O':
        output << cool ( status[i], color );
        break;
      case 'E':
        output << error ( status[i], color );
        break;
      case 'R':
        output << running ( status[i], color );
        break;
      case 'P':
        output << pending ( status[i], color );
        break;
      case '?':
        output << warning ( status[i], color );
        break;
      default:
        output << status[i];
        break;
    }
  }

  return output.str();
}


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
#include "wait.hpp"
#include "utils.hpp"
/////////////////////////////////////////////////////////////////////////
// STD 
#include <algorithm>
/////////////////////////////////////////////////////////////////////////

static std::string accepted_events = "SEP";

/////////////////////////////////////////////////////////////////////////
magrit::wait::wait ( generic_command* previous_subcommand )
  : generic_command ( previous_subcommand, true ),
    _wait_options ("Wait options")
{
  _wait_options.add_options()
    ( "event-mask,e", boost::program_options::value<std::string>()->required(),
      "Event to wait for: S(tart), E(nd) and P(ending)" )
    ( "timeout,t",boost::program_options::value<size_t>(),
      "Timeout in milliseconds" );

  get_options().add ( _wait_options );
}

/////////////////////////////////////////////////////////////////////////
const char*
magrit::wait::get_name() const
{
  if ( _previous_subcommand == nullptr )
  {
    return "magrit-wait-for";
  }
  else
  {
    return "wait"; 
  }
} 

/////////////////////////////////////////////////////////////////////////
const char* magrit::wait::get_description() const
{
  return "Waits for commit status change";
}

/////////////////////////////////////////////////////////////////////////
void
magrit::wait::process_parsed_options
(
  const std::vector<std::string>& arguments,
  const boost::program_options::variables_map& vm,
  const std::vector<std::string>& unrecognized_arguments,
  bool allow_zero_arguments
)
const
{
  size_t timeout = 0;
  std::string events = vm["event-mask"].as<std::string>();

  generic_command::process_parsed_options
    ( arguments, vm, unrecognized_arguments, true );

  if ( vm.count ( "timeout" ) )
  {
    timeout = vm["timeout"].as<size_t>();
  }

  if ( events.find_first_not_of ( accepted_events ) != std::string::npos )
  {
    throw magrit::option_not_recognized
    (
      std::string("Event '") +
      events[events.find_first_not_of ( accepted_events )] +
      std::string("' not recognized. Accepted values are: ") +
      join (" or ",accepted_events.begin(),accepted_events.end(), true )
    );
  }
  else if ( events.size() > accepted_events.size() )
  {
    throw magrit::invalid_argument
    (
      "Only " + boost::lexical_cast<std::string> ( accepted_events.size() ) +
      " events are allowed"
    );
  }

  if ( unrecognized_arguments.size() == 0 )
  {
    wait_for
    (
      events, timeout,
      magrit::get_commits ( std::vector<std::string>{ "HEAD" } )
    );  
  }
  else
  {
    wait_for ( events, timeout, get_commits ( unrecognized_arguments ) );  
  }
}

/////////////////////////////////////////////////////////////////////////
std::string to_textual_events ( const std::string& input )
{
  std::vector<std::string> output;

  std::transform
  (
    input.begin(), input.end(), std::back_inserter ( output ),
    [] ( char event ) -> std::string
    {
      switch ( event )
      {
			  case 'S':
          return "to start"; 
			  case 'E':
          return "to end";
			  case 'P':
          return "to be scheduled";
        default:
          throw std::logic_error 
          (
            std::string("Event '") +
            event +
            std::string("' not recognized. Accepted events: ") +
            join ( " or ", accepted_events.begin(), accepted_events.end() )
          );
      }
    }
  );

  return join ( " or ", output.begin(), output.end(), true );
}

/////////////////////////////////////////////////////////////////////////
void
magrit::wait::wait_for
( 
  const std::string& events,
  size_t timeout,
  const std::vector<std::string>& sha1s,
  bool silent
)
{
  if ( !silent )
  {
    std::cout
      << "Waiting the following commit(s) " 
      << "build " << to_textual_events ( events ) << ": " << std::endl
      << " " << join ( "\n ", sha1s.begin(), sha1s.end() ) << std::endl;
  }

  start_process
  (
    "ssh",
    std::vector < std::string >
    {
      "-x",
      "-p",
      boost::lexical_cast < std::string > ( get_magrit_port() ),
      get_magrit_connection_info(),
      "--raw",
      "magrit",
      "wait-for",
      std::string("--event-mask=") + events,
      "--timeout=" + ( timeout == 0 ? 
                       std::string("-1") :
                       boost::lexical_cast<std::string> ( timeout ) ),
      get_repo_name(),
      join ( " ", sha1s.begin(), sha1s.end() )
    },
    boost::process::close_stream(),
    boost::process::silence_stream(),
    boost::process::inherit_stream(),
    [] ( const std::string& line )
    {
      std::cout << line << std::endl; 
    }
  );
}

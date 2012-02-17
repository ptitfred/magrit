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
#include <iomanip>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
/////////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////////////////////
magrit::wait::wait ( generic_command* previous_subcommand )
  : generic_command ( previous_subcommand, true ),
    _wait_options ("Wait options")
{
  _wait_options.add_options()
    ( "event-mask,e", boost::program_options::value<char>()->required(),
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
std::vector<std::string> get_commits ( const std::vector<std::string>& git_args )
{
  return std::vector<std::string>();
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
  static std::string accepted_events = "SEP";

  generic_command::process_parsed_options
    ( arguments, vm, unrecognized_arguments, true );

  size_t timeout = 0;
  char event = vm["event-mask"].as<char>();

  if ( vm.count ( "timeout" ) )
  {
    timeout = vm["timeout"].as<size_t>();
  }

  if ( std::string(accepted_events).find(event) == std::string::npos )
  {
    throw magrit::option_not_recognized
    (
      std::string("Event '") +
      event +
      std::string("' not recognized. Accepted values are: ") +
      join (",",accepted_events.begin(),accepted_events.end() )
    );
  }

  if ( unrecognized_arguments.size() == 0 )
  {
    wait_for
    (
      event, timeout,
      get_commits ( std::vector<std::string>{ "HEAD" } )
    );  
  }
  else
  {
    wait_for ( event, timeout, get_commits ( unrecognized_arguments ) );  
  }
}

/////////////////////////////////////////////////////////////////////////
void
magrit::wait::wait_for
( char event, size_t timeout, const std::vector<std::string>& git_options )
const
{
}

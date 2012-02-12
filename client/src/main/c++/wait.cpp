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
magrit::wait::wait ( generic_command* previous_subcommand )
  : generic_command ( previous_subcommand, true ),
    _wait_options ("Wait options")
{
  /*
  _wait_options.add_options()
    ( "watch,w","activates the automatic refresh" );

  get_options().add ( _wait_options );
  */
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
  generic_command::process_parsed_options
    ( arguments, vm, unrecognized_arguments, true );

  if ( vm.count ( "watch" ) )
  {
  }
  else
  {
  }
}


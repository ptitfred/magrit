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
     "git options");

  get_options().add ( _positional_parameters_desc );
}

/////////////////////////////////////////////////////////////////////////
const char*
magrit::log::get_name() const
{
  return "log"; 
} 

/////////////////////////////////////////////////////////////////////////
const char* magrit::log::get_description() const
{
  return "<description to be written>";
}

/////////////////////////////////////////////////////////////////////////
boost::program_options::command_line_parser&
magrit::log::positional
  ( boost::program_options::command_line_parser& parser ) const
{
  return parser.positional( _positional_parameters );
}

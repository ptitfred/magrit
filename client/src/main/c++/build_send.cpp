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
#include "build_send.hpp"
/////////////////////////////////////////////////////////////////////////

magrit::send::send() : options ( "" )
{
  namespace bpo = boost::program_options;

  options.add ( generic_command::get_options() );

  bpo::options_description
    send_options_desc ( "Send options" );

  send_options_desc.add_options()
    ("--force,f", "<description to be written>")
    ("--command,c", bpo::value<std::string>()->required()
                  , "<description to be written>");

  options.add ( send_options_desc );
}

/////////////////////////////////////////////////////////////////////////
const char*
magrit::send::get_name() const
{
  return "send"; 
} 

/////////////////////////////////////////////////////////////////////////
const char* magrit::send::get_description() const
{
  return "<description to be written>";
}

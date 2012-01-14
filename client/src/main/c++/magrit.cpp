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
#include "magrit.hpp"
/////////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////////////////////
magrit::magrit ()
{
  _subcommands.push_back ( sh_ptr<generic_command>( new build() ) );
}

/////////////////////////////////////////////////////////////////////////
const char*
magrit::get_name() const
{
  return "magrit"; 
} 

/////////////////////////////////////////////////////////////////////////
const char* magrit::get_description() const
{
  return "Command-line client for Magrit build system";
}

/////////////////////////////////////////////////////////////////////////
std::vector<std::string>
magrit::get_subcommands_desc() const override
{
  std::vector<std::string> commands;

  commands.push_back ( std::string("Description to be written.") );

  return commands;
}

/////////////////////////////////////////////////////////////////////////
void magrit::run ( const std::vector<std::string>& arguments ) const
{
  if ( arguments.size() == 0 )
  {
    print_help ();
  }
  else
  {
    generic_command::run ( arguments );
  }
}

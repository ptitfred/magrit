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
#include "build.hpp"
#include "config.hpp"
#include "monitor.hpp"
#include "share.hpp"
#include "status.hpp"
/////////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////////////////////
magrit::magrit::magrit () : generic_command ( nullptr )
{
  _subcommands.push_back ( sh_ptr<generic_command>( new build ( this ) ) );
  _subcommands.push_back ( sh_ptr<generic_command>( new config ( this ) ) );
  _subcommands.push_back ( sh_ptr<generic_command>( new monitor ( this ) ) );
  _subcommands.push_back ( sh_ptr<generic_command>( new share ( this ) ) );
  _subcommands.push_back ( sh_ptr<generic_command>( new status ( this ) ) );
}

/////////////////////////////////////////////////////////////////////////
const char*
magrit::magrit::get_name() const
{
  return "magrit"; 
} 

/////////////////////////////////////////////////////////////////////////
const char* magrit::magrit::get_description() const
{
  return "Command-line client for Magrit build system";
}

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
#include "config.hpp"
#include "config_add.hpp"
#include "config_use.hpp"
#include "config_remove.hpp"
/////////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////////////////////
magrit::config::config ( generic_command* previous_subcommand )
  : generic_command ( previous_subcommand )
{
  _subcommands.push_back(sh_ptr<generic_command>(new config_add(this)));
  _subcommands.push_back(sh_ptr<generic_command>(new config_use(this)));
  _subcommands.push_back(sh_ptr<generic_command>(new config_remove(this)));
}

/////////////////////////////////////////////////////////////////////////
const char*
magrit::config::get_name() const
{
  return "config"; 
} 

/////////////////////////////////////////////////////////////////////////
const char* magrit::config::get_description() const
{
  return "<description to be written>";
}




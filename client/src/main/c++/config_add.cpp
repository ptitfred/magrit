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
#include "config_add.hpp"
/////////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////////////////////
magrit::config_add::config_add ( generic_command* previous_subcommand )
  : generic_command ( previous_subcommand ),
     _positional_parameters_desc
    ("Positional options (can be added to the end of argument list without the dashed string)")

{
  get_options().add_options()
  ( 
    "alias",
    boost::program_options::value<std::string>()->default_value("magrit"),
    "name to identify the config"
  );
  
  _positional_parameters.add("address", 1);
  _positional_parameters.add("name", 1);

  _positional_parameters_desc.add_options()
    ("address",
       boost::program_options::value<std::string>()
         ->default_value("localhost:2022"),
       "host:port where magrit listens to")
    ("name", boost::program_options::value<std::string>(),
     "repository name");

  get_options().add ( _positional_parameters_desc );
}

/////////////////////////////////////////////////////////////////////////
const char*
magrit::config_add::get_name() const
{
  return "add"; 
} 

/////////////////////////////////////////////////////////////////////////
const char* magrit::config_add::get_description() const
{
  return "Adds a new repository";
}

/////////////////////////////////////////////////////////////////////////
const boost::program_options::positional_options_description&
magrit::config_add::get_positional_options () const override
{
  return _positional_parameters;
}




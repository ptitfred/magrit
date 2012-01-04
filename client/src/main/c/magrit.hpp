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
#include "utils.hpp"
/////////////////////////////////////////////////////////////////////////
// STD
#include <forward_list>
#include <vector>
#include <algorithm>
/////////////////////////////////////////////////////////////////////////
// BOOST
#include <boost/program_options.hpp>
#include <boost/foreach.hpp>
/////////////////////////////////////////////////////////////////////////

struct magrit : public generic_command
{
  const char* get_name() const
  {
    return "magrit"; 
  } 

  void
  process_parsed_options
  ( int argc, const char* const* argv, const boost::program_options::variables_map& vm )
  throw ( DoNotContinue )
  {
    if ( argc == 1 )
    {
      help();

      throw DoNotContinue();
    }
    else
    {
      generic_command::process_parsed_options ( argc, argv, vm );

      const std::string command = vm["command"].as < std::string > ();

      std::vector <std::string> command_args
        = vm["command-arguments"].as < std::vector< std::string> > ();

      std::vector<generic_command> commands
        = get_commands ();

      BOOST_FOREACH ( generic_command& command_obj, commands )
      {
        if ( command_obj.get_name() == command )
        {
          size_t length = command_args.size() + 1 ;

          const char* command_line[length];

          join ( command, command_args, command_line );

          command_obj.run ( length , command_line );
        }
      }
    }
  }

  boost::program_options::positional_options_description
    create_positional_options ()
  {
    // Command to execute
    boost::program_options::positional_options_description
      positional_options_desc;

    positional_options_desc.add("command",1).add("command-arguments",-1);

    return positional_options_desc; 
  }

  std::vector<generic_command> get_commands ()
  {
    std::vector<generic_command> commands;

    // TODO:
    //commands.add ();

    return commands;   
  }
};

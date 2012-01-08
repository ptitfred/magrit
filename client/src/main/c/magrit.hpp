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
#include "magrit-build.hpp"
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
  const throw ( DoNotContinue )
  {
    if ( argc == 1 )
    {
      help();

      throw DoNotContinue();
    }

    generic_command::process_parsed_options ( argc, argv, vm );

    const std::string command = vm["command"].as < std::string > ();

    std::vector <std::string> command_args;

    if ( vm.count("command-arguments") )
    {
      command_args = vm["command-arguments"]
        .as < std::vector< std::string> > ();
    }

    std::forward_list< sh_ptr<generic_command> > commands
      = get_commands ();

    BOOST_FOREACH ( const sh_ptr<generic_command> command_obj, commands )
    {
      std::cout << "[build::process_parsed_options2 get_name='" << command_obj->get_name() << "' command='" << command  << "']" << std::endl;
      if ( command_obj->get_name() == command )
      {
        size_t length = command_args.size() + 1 ;

        const char* command_line[length];

        join ( command, command_args, command_line );

        command_obj->run ( length , command_line );

        return;
      }
    }

    std::cerr << "Command '" << command << "' not found" << std::endl;
  }

  std::forward_list< sh_ptr<generic_command> > get_commands () const
  {
    std::forward_list< sh_ptr<generic_command> > commands;

    commands.push_front ( sh_ptr<generic_command>( new build() ) );

    return commands;
  }
};

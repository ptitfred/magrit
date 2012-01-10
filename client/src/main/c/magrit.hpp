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
  ( int argc, char** argv, const boost::program_options::variables_map& vm )
  const throw ( DoNotContinue )
  {
    if ( argc == 1 )
    {
      help();

      throw DoNotContinue();
    }

    generic_command::process_parsed_options ( argc, argv, vm );
    
    std::forward_list< sh_ptr<generic_command> > commands
      = get_commands ();

    char* command = argv[1];

    BOOST_FOREACH ( const sh_ptr<generic_command> command_obj, commands )
    {
      if ( command_obj->get_name() == std::string(command) )
      {
        // TODO: if other command line options are added (other than
        //       --help and --version that will be catched by process_parsed_options),
        //       argv[0] might not hold the command (e.g: ./magrit --debug build send ).
        //       process_parsed_options must return a pointer to unprocessed options.
        char** command_args = argc > 2? &argv[2]: NULL;
        char* new_command_line[argc - 1];

        join ( command, command_args, std::max(argc - 2,0), new_command_line );

        command_obj->run ( argc - 1, new_command_line );

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

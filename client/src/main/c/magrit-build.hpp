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
///////////////////////////////////////////////////////////////////////////
// MAGRIT 
//#include "magrit-send-build.hpp"
//#include "magrit-cat-build.hpp"
///////////////////////////////////////////////////////////////////////////

struct build : public generic_command
{
  const char* get_name() const
  {
    return "build"; 
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

    char* command = argv[0];

    uint command_args_length = argc - 1;

    char** command_args = &argv[1];

    char* command_line[command_args_length+1];

    join ( command, command_args, command_args_length, command_line );      

    if ( std::string(command) == "send" )
    {
      // magrit_send_build cmd;
      // cmd.run ( length, command_line ); 
      std::cout << "[build send]" << std::endl;
    }
    else if ( std::string(command) == "cat-log" )
    {
      // magrit_cat_build cmd;
      // cmd.run ( length, command_line ); 
      std::cout << "[build cat-log]" << std::endl;
    }
    else
    {
      help();

      throw DoNotContinue();
    }
  }
};

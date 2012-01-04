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

struct build
{
  const char* get_name() const
  {
    return "magrit-build"; 
  } 

  void
  process_parsed_options
  ( int argc, const char* const* argv, const boost::program_options::variables_map& vm )
  throw ( DoNotContinue )
  {
    generic_command::process_parsed_options ( argc, argv, vm );

    const std::string command = vm["command"].as < std::string > ();

    std::vector <std::string> command_args
      = vm["command-arguments"].as < std::vector< std::string> > ();

    size_t length = command_args.size() + 1 ;

    const char* command_line[length];

    join ( command, command_args, command_line );      

    if ( command == "send" )
    {
      // magrit_send_build cmd;
      // cmd.run ( length, command_line ); 
    }
    else if ( command == "cat-log" )
    {
      // magrit_cat_build cmd;
      // cmd.run ( length, command_line ); 
    }
    else
    {
      help();

      throw DoNotContinue();
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
};

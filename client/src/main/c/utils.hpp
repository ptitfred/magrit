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
// STD
#include <stdexcept>
/////////////////////////////////////////////////////////////////////////
// BOOST
#include <boost/program_options.hpp>
/////////////////////////////////////////////////////////////////////////

struct DoNotContinue
{
};

/**
 * Base class for all the magrit's commands.
 */
struct generic_command
{
  /**
   * Runs the command. The default behavior is to parse the command line
   * supplied and pass the parsed command line to
   * generic_command::process_parsed_options.
   *
   * @throws boost::program_options::unknown_option if one of the
   *         given command line switches is not allowed.
   */
  virtual void run ( int argc, const char* const * argv )
  {
    boost::program_options::variables_map vm;

    boost::program_options::store
    (
      boost::program_options::command_line_parser( argc, argv )
        .options ( create_options() )
        .positional( create_positional_options() )
        .run (),
      vm
    );

    boost::program_options::notify ( vm );

    process_parsed_options ( argc, argv, vm );
  }

  /**
   * The supplied variables_map contains correctly parsed
   * variables. You will probably want to redefine this method,
   * by default it only processes generic_command::create_options
   * options.
   *
   * @throw DoNotContinue If a switch parsed doesn't require
   *        further action (e.g.: --help, --version ). 
   */
  virtual void
  process_parsed_options
  ( int argc, const char* const* arg, const boost::program_options::variables_map& vm )
  throw ( DoNotContinue )
  {
    if ( vm.count("help") )
    {
      help();
    }
    else if ( vm.count("version") )
    {
      static const char* LICENSE =
      "Copyright 2011 Frederic Menou                                    \n"
      "                                                                 \n"
      "Magrit is free software: you can redistribute it and/or modify   \n"
      "it under the terms of the GNU Affero General Public License as   \n"
      "published by the Free Software Foundation, either version 3 of   \n"
      "the License, or (at your option) any later version.              \n"
      "                                                                 \n"
      "Magrit is distributed in the hope that it will be useful,        \n"
      "but WITHOUT ANY WARRANTY; without even the implied warranty of   \n"
      "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the    \n"
      "GNU Affero General Public License for more details.              \n"
      "                                                                 \n"
      "You should have received a copy of the GNU Affero General Public \n"
      "License along with Magrit.                                       \n"
      "If not, see <http://www.gnu.org/licenses/>.                      \n";

      std::cout << LICENSE << std::endl;
      std::cout << "Version version.num.not.implemented.yet" << std::endl;
      throw DoNotContinue();
    }
  }

  /**
   * Defines the command line options used by this command. Redefine
   * in the subclass to tailor to your need. Call this method in your
   * subclass if you want to have access to --help and --version switches.
   *
   * @return boost::program_options::options_description
   */
  virtual boost::program_options::options_description create_options ()
  {
    boost::program_options::options_description
      generic_options_desc ( "Main options" );

    generic_options_desc.add_options()
      ("help,h", "produces this help message")
      ("version,v", "version of the application");

    return generic_options_desc;    
  }

  /**
   * Redefine this method if you have to parse positional parameters
   * at the end of the command line.
   *
   * @return boost::program_options::positional_options_description
   */ 
  virtual boost::program_options::positional_options_description
    create_positional_options ()
  {
    // Command to execute
    boost::program_options::positional_options_description
      null_positional_options_desc;

    return null_positional_options_desc; 
  }

  virtual void help () throw ( DoNotContinue )
  {
    std::cout << create_options();

    throw DoNotContinue();
  }
}; 

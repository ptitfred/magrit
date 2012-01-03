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
// BOOST
#include <boost/program_options.hpp>
/////////////////////////////////////////////////////////////////////////

/**
 * Base class for all the magrit's commands.
 */
struct generic_command
{
  /**
   * Runs the command. 
   */
  virtual void run () = 0;

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

}; 

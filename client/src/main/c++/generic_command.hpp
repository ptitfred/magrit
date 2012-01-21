/**
 * Copyright 2012 Frederic Menou
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
#ifndef __MAGRIT_GENERIC__
#define __MAGRIT_GENERIC__
/////////////////////////////////////////////////////////////////////////
// MAGRIT 
#include "utils.hpp"
/////////////////////////////////////////////////////////////////////////
// STD
#include <stdexcept>
#include <iostream>
#include <iterator>
#include <string>
#include <vector>
/////////////////////////////////////////////////////////////////////////
// BOOST
#include <boost/program_options.hpp>
#include <iterator>
/////////////////////////////////////////////////////////////////////////

namespace magrit
{

  struct do_not_continue 
  {
  };

  struct option_not_recognized : public std::runtime_error
  {
    option_not_recognized (const std::string& what)
      : std::runtime_error ( what )
    {
    }
  };

  /**
   * Base class for all magrit commands.
   */
  class generic_command
  {
    public:

      generic_command( generic_command* previous_subcommand );

      /**
       * @name Main methods you will have to redefine.
       */
      ///@{
      /**
       * Name of the command as it appears in the command line.
       *
       * @return Name of the command.
       */
      virtual const char* get_name() const = 0;

      /**
       * Returns the description of the command.
       */
      virtual const char* get_description() const = 0;

      /**
       * @name These ones are not mandatory but reasonable
       *       defaults are provided. 
       */
      ///@{
      /**
       * Defines the command line options used by this command. Redefine
       * in the subclass to tailor to your need. Call this method in your
       * subclass if you want to have access to --help and --version switches.
       *
       * @return bpo::options_description
       */
      virtual const boost::program_options::options_description&
      get_options () const;

    protected:

      virtual boost::program_options::command_line_parser& 
      positional ( boost::program_options::command_line_parser& parser )
      const;

      ///@}
      ///@}

    public:

      /**
       * Runs the command. The default behavior is to parse the command line
       * supplied and pass the parsed command line to
       * generic_command::process_parsed_options and
       * generic_command::process_subcommands. You won't probably want
       * to redefine this.
       *
       * @throws bpo::unknown_option if one of the
       *         given command line switches is not allowed.
       */
      virtual void run ( const std::vector<std::string>& arguments ) const;

      /**
       * Implementation of run().
       */
      virtual bool run_impl
      ( 
        const std::vector<std::string>& arguments, boost::program_options::variables_map& vm
      ) const;

    protected:

      /**
       * @name You can redefine these methods if you want to do
       *       something useful. generic_command::run() is the top-most
       *       method and it calls generic_command::process_parsed_options
       *       and generic_command::process_subcommands.
       *       You'll probably want to redefine the process_parsed_options
       *       methods instead of ::run and ::process_subcommands.
       */
      ///@{
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
      (
        const std::vector<std::string>& arguments,
        const boost::program_options::variables_map& vm
      )
      const;
      ///@}
   
    protected:

      /**
       * Subcommands implemented by the command. Empty vector by default.
       */
      virtual const std::vector< sh_ptr<generic_command>>&
      get_subcommands() const;

      /**
       * Prints the help notice.
       */
      virtual void print_help () const;

    private:

      /**
       * Returns an iterator to the first command in the arguments
       * vector or end() if none.
       *
       * @param arguments Vector of command line arguments.
       * @return iterator to the first command or end() if none.
       */
      std::vector<std::string>::const_iterator
      first_command ( const std::vector<std::string>& arguments ) const;

      /**
       * Removes the given argument from the list of arguments. Returns
       * the result as a new vector.
       */
      std::vector<std::string> remove_subcommand_first
        ( const std::vector<std::string>& arguments, const std::string& arg )
      const;

      /**
       * Returns the subcommand with the given name or
       * end() if none exists.
       */
      std::vector< sh_ptr<generic_command>>::const_iterator
      get_subcommand ( const std::string& name ) const;

      /**
       * Returns if the given arguments matches the options
       * of the current command. vm is written with the
       * parsed options.
       */
      bool
      matches
      ( 
        const std::vector<std::string>& arguments,
        boost::program_options::variables_map& vm
      ) const;

      /**
       * Prints the help notice.
       */
      virtual void print_help_subcommands_description () const;

      virtual void print_help_command () const;

    protected:

      boost::program_options::options_description _options;

      boost::program_options::positional_options_description
                                                  _no_positional_options;

      std::vector<sh_ptr<generic_command>>        _subcommands;

      generic_command*                            _previous_subcommand;
  };
};
#endif

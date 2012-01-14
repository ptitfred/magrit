/**
 * Copyright 2012 Frederic Menou
 * Copyright 2012 Daniel Perez
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

struct DoNotContinue
{
};

struct OptionNotRecognized : public std::runtime_error
{
  OptionNotRecognized (const std::string& what)
    : std::runtime_error ( what )
  {
  }
};

/**
 * Base class for all magrit commands.
 */
struct generic_command
{
  /**
   * Name of the command as it appears in the command line.
   *
   * @return Name of the command.
   */
  virtual const char* get_name() const = 0;

  /**
   * Runs the command. The default behavior is to parse the command line
   * supplied and pass the parsed command line to
   * generic_command::process_parsed_options.
   *
   * @throws bpo::unknown_option if one of the
   *         given command line switches is not allowed.
   */
  virtual void run ( const std::vector<std::string>& arguments ) const;

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
   * Processes positional options. By default dispatches the
   * subcommands. 
   */
  virtual void
  process_unregistered_options
  (
    const std::vector<std::string>& arguments,
    const std::vector< boost::program_options::basic_option<char> >&
      options,
    const boost::program_options::variables_map& vm
  ) const;

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

  /**
   * Defines the command line options used by this command. Redefine
   * in the subclass to tailor to your need. Call this method in your
   * subclass if you want to have access to --help and --version switches.
   *
   * @return bpo::options_description
   */
  virtual boost::program_options::options_description
  create_options () const;

  /**
   * Subcommands implemented by the command. None by default.
   */
  virtual std::vector< sh_ptr<generic_command> > get_subcommands() const;

  /**
   * Subcommand description. Same order as get_subcommands. None by default.
   */
  virtual std::vector<std::string> get_subcommands_desc() const;

  /**
   * Prints the help notice.
   */
  virtual void print_help () const;
};
#endif

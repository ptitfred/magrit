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
#ifndef __MAGRIT_LOG__
#define __MAGRIT_LOG__
///////////////////////////////////////////////////////////////////////////
// MAGRIT 
#include "generic_command.hpp"
///////////////////////////////////////////////////////////////////////////

namespace magrit
{
  class log : public generic_command
  {
    public:

      log ( generic_command* previous_subcommand );

      /**
       * @see generic_command::get_name
       */
      const char* get_name() const override;

      /**
       * @see generic_command::get_description
       */
      const char* get_description() const override;

      void print_status ( const std::vector < std::string >& git_args ) const;

      /**
       * Given a status code, returns the colorized version.
       */
      static std::string
      colorize_linux ( const std::string& status, bool color );

    protected:

      void
      process_parsed_options
      (
        const std::vector<std::string>& arguments,
        const boost::program_options::variables_map& vm,
        const std::vector<std::string>& unrecognized_arguments,
        bool allow_zero_arguments
      )
      const;

    protected:

      boost::program_options::options_description _log_options;

      boost::program_options::positional_options_description
                                                  _positional_parameters;

      boost::program_options::options_description _positional_parameters_desc;
  };
};
#endif


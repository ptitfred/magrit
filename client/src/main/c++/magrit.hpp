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
#ifndef __MAGRIT_MAIN__
#define __MAGRIT_MAIN__
/////////////////////////////////////////////////////////////////////////
// MAGRIT 
#include "generic_command.hpp"
/////////////////////////////////////////////////////////////////////////
// STD
#include <vector>
/////////////////////////////////////////////////////////////////////////

struct magrit : public generic_command
{

  magrit ();

  /**
   * @see generic_command::get_name
   */
  const char* get_name() const override;

  /**
   * @see generic_command::get_description
   */
  const char* get_description() const override;

  /**
   * @see generic_command::process_parsed_options
   */
  void
  process_parsed_options
  (
    const std::vector<std::string>& arguments,
    const boost::program_options::variables_map& vm
  )
  const override;
};
#endif

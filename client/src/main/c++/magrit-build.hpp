/**
 * Copyright 2011 Frederic Menou
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
///////////////////////////////////////////////////////////////////////////
// MAGRIT 
#include "generic_command.hpp"
///////////////////////////////////////////////////////////////////////////

struct build : public generic_command
{
  /**
   * @see generic_command::get_name
   */
  const char* get_name() const
  {
    return "build"; 
  } 
};

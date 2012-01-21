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
#include "magrit.hpp"
/////////////////////////////////////////////////////////////////////////
// STD 
#include <algorithm>
/////////////////////////////////////////////////////////////////////////

int main ( int argc, char** argv )
{
  magrit::magrit ma;

  try
  {
    std::vector<std::string> arguments ( argv, argv+argc );

    ma.run ( arguments );

    return 0;
  }
  catch ( const magrit::do_not_continue& e )
  {
    return 0;
  }
  catch ( const magrit::option_not_recognized& e )
  {
    std::cerr << "Error: unknown option: " << e.what() << std::endl;
  }
  catch ( boost::program_options::error& e )
  {
    std::cerr << "Error: " << e.what() << std::endl;
  }
  catch ( std::exception& e )
  {
    std::cerr << "Error: '" << e.what() << "'" << std::endl;
  }

  return -1;
}

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
  magrit ma;

  try
  {
    std::vector<std::string> arguments;

    std::copy ( argv+1, argv+argc, std::back_inserter(arguments) );
   
    ma.run ( arguments );
  }
  catch ( const DoNotContinue& e )
  {
  }
  catch ( const OptionNotRecognized& e )
  {
    std::cerr << "Unknown option '" << e.what() << "'" << std::endl;
  }
  catch ( boost::program_options::unknown_option& e )
  {
    std::cerr << "Unknown option (boost::program_options::unknown_option) '" << e.get_option_name() << "'" << std::endl;
  }
  catch ( std::exception& e )
  {
    std::cerr << "Error: " << e.what() << "'" << std::endl;
  }
}

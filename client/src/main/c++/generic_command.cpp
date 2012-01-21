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
#include "generic_command.hpp"
/////////////////////////////////////////////////////////////////////////
// STD 
#include <iomanip>
/////////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////////////////////
magrit::generic_command::generic_command() : options ( "" )
{
  namespace bpo = boost::program_options;

  options.add_options()
    ("help,h", "produces this help message")
    ("version,v", "version of the application");
}

/////////////////////////////////////////////////////////////////////////
void
magrit::generic_command::run
  ( const std::vector<std::string>& arguments ) const
{
  namespace bpo = boost::program_options;

  bpo::variables_map vm;

  const std::vector<std::string>
    tail_arguments ( ++arguments.begin(), arguments.end() );

  if ( ! run_impl ( tail_arguments, vm ) )
  {
    print_help();

    throw option_not_recognized
      ( join<std::string> ( " ", arguments.begin(), arguments.end() ) );
  }
}


/////////////////////////////////////////////////////////////////////////
bool
magrit::generic_command::run_impl
(
  const std::vector<std::string>& arguments,
  boost::program_options::variables_map& vm
) const
{
  namespace bpo = boost::program_options;

  auto subcommand_str = first_command ( arguments );

  if ( subcommand_str != arguments.end() )
  {
    // Subcommand passed
    auto subcommand = get_subcommand(*subcommand_str);

    if ( subcommand != get_subcommands().end() )
    {
      return (*subcommand)->run_impl
             ( 
               remove_subcommand_first ( arguments, *subcommand_str ),
               vm 
             );
    }
    else
    {
      std::cout << std::string("Command '")
                << get_name()
                << std::string("' doesn't accept subcommand '")
                << *subcommand_str
                << std::string("'")
                << std::endl;
      return false;
    }
  }
  else
  {
    // No more subcommands, we try a match.
    if ( matches ( arguments, vm ) )
    {
      process_parsed_options ( arguments, vm );
      return true;
    }
    else
    {
      return false;
    }
  }
}


/////////////////////////////////////////////////////////////////////////
bool
magrit::generic_command::matches
( 
  const std::vector<std::string>& arguments,
  boost::program_options::variables_map& vm
) const
{
  namespace bpo = boost::program_options;

  if ( arguments.size() == 0 ) return true;

  try
  {
    bpo::parsed_options parsed =
      bpo::command_line_parser( arguments )
        .options ( get_options() )
        .run ();

    bpo::store ( parsed, vm );

    bpo::notify ( vm );

    return true;
  }
  catch ( boost::program_options::unknown_option& e )
  {
    return false;
  }
}

/////////////////////////////////////////////////////////////////////////
void
magrit::generic_command::process_parsed_options
(
  const std::vector<std::string>& arguments,
  const boost::program_options::variables_map& vm
)
const
{
  if ( vm.count("help") )
  {
    print_help ();

    throw do_not_continue();
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
    std::cout << "Version 0.0.1" << std::endl;
    throw do_not_continue();
  }
}

/////////////////////////////////////////////////////////////////////////
std::vector<sh_ptr<magrit::generic_command>>::const_iterator
magrit::generic_command::get_subcommand ( const std::string& name ) const
{
  return std::find_if
  (
    get_subcommands().begin(),
    get_subcommands().end(),
    [&] ( sh_ptr<generic_command> cmd )
    {
      return cmd->get_name() == name;
    }
  );
}

/////////////////////////////////////////////////////////////////////////
const boost::program_options::options_description&
magrit::generic_command::get_options () const 
{
  return options; 
}

/////////////////////////////////////////////////////////////////////////
const std::vector< sh_ptr<magrit::generic_command>>&
magrit::generic_command::get_subcommands() const
{
  return _subcommands;
}

/////////////////////////////////////////////////////////////////////////
void magrit::generic_command::print_help () const
{
  using namespace std;

  cout << "Use: " << get_name() << " <options> ";

  if ( get_subcommands().size() > 0 )
  {
    cout << (
              (get_subcommands().size() > 0)?
                "[command] <subcommand arguments>"
                :
                ""
            )
         << endl << endl;

    cout << "Commands:" << endl << endl;

    print_help_subcommands_description ();

    cout << endl;

    cout << "For subcommand's arguments help, ";
    cout << "call the desired subcommand with --help" << endl << endl;
  }
  else
  {
    cout << endl << endl;
  }

  cout <<  get_options() ;
}

/////////////////////////////////////////////////////////////////////////
void magrit::generic_command::print_help_subcommands_description () const
{
  std::for_each
  (
    get_subcommands().begin(),
    get_subcommands().end(),
    [] ( sh_ptr<generic_command> cmd )
    {
      std::cout << "  " << std::setw (10) << cmd->get_name() 
                << "\t" << cmd->get_description() << std::endl;
    }
  );
}

/////////////////////////////////////////////////////////////////////////
std::vector<std::string> magrit::generic_command::remove_subcommand_first
  ( const std::vector<std::string>& arguments, const std::string& arg ) const
{
  std::vector<std::string> output;

  auto to_remove =
    std::find_if (
                   arguments.begin(), arguments.end(),
                   [&arg] (const std::string& current)
                   {
                     return current == arg; 
                   }
                 );

  for ( auto it  = arguments.begin();
             it != arguments.end();
             ++it ) 
  {
    if ( it != to_remove )
    {
      output.push_back (*it);
    }
  }

  return output;
}

/////////////////////////////////////////////////////////////////////////
std::vector<std::string>::const_iterator
magrit::generic_command::first_command ( const std::vector<std::string>& arguments )
const
{
  return find_if ( 
                   arguments.begin(), arguments.end(),
                   [](const std::string& elem)
                   {
                     if ( elem[0] == '-' )
                     {
                       return false;
                     }
                     else
                     {
                       return true;
                     }
                   }
                 );
}


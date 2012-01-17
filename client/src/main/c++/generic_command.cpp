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

/////////////////////////////////////////////////////////////////////////
void
generic_command::run
  ( const std::vector<std::string>& arguments ) const
{
  namespace bpo = boost::program_options;

  bpo::variables_map vm;

  bpo::options_description
    parent_options_desc ("");

  parent_options_desc
    .add ( create_options() );

  bpo::parsed_options parsed =
    bpo::command_line_parser( arguments )
      .options ( parent_options_desc )
      .allow_unregistered()
      .run ();

  bpo::store ( parsed, vm );

  bpo::notify ( vm );

  // The logic of commands is implemented from here on:

  if ( !process_subcommands ( arguments, parsed.options, vm))
  {
    process_parsed_options ( arguments, vm );
  }
}

/////////////////////////////////////////////////////////////////////////
bool
generic_command::process_subcommands
(
  const std::vector<std::string>& arguments,
  const std::vector< boost::program_options::basic_option<char> >&
    options,
  const boost::program_options::variables_map& vm
)
const
{
  namespace bpo = boost::program_options;

  auto subcommand = first_command ( arguments );

  if ( get_subcommands().size() == 0 && subcommand == arguments.end() )
  {
    // No unprocessed arguments. We stop rambling.
  }
  else if ( get_subcommands().size() != 0 && subcommand != arguments.end() )
  {
    // Still arguments to be processed by a subcommand
    auto subcmd_it = get_subcommand ( *subcommand );

    if ( subcmd_it != get_subcommands().end() )
    {
      (*subcmd_it)->run ( remove_argument ( arguments, *subcommand ) );
    }
  } 
  else if ( get_subcommands().size() != 0 && subcommand == arguments.end() )
  {
    // Expected a subcommand and no extra arguments passed:
    // up to the specific command to print help or do any
    // action if the command can be called without subcommands too.
  }
  else
  {
    // Extra arguments passed but none was expected
    // ( get_subcommands().size() == 0 && subcommand != arguments.end() )
    throw OptionNotRecognized
      ( join<std::string> ( " ", arguments.begin(), arguments.end() ) );
  }

  // Only if we processed any command
  return get_subcommands().size() != 0 && subcommand != arguments.end();
}

/////////////////////////////////////////////////////////////////////////
void
generic_command::process_parsed_options
(
  const std::vector<std::string>& arguments,
  const boost::program_options::variables_map& vm
)
const
{
  if ( vm.count("help") )
  {
    print_help ();

    throw DoNotContinue();
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
    throw DoNotContinue();
  }
}

/////////////////////////////////////////////////////////////////////////
std::vector<sh_ptr<generic_command>>::const_iterator
generic_command::get_subcommand ( const std::string& name ) const
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
boost::program_options::options_description
generic_command::create_options () const 
{
  namespace bpo = boost::program_options;

  bpo::options_description
    generic_options_desc ( "Main options" );

  generic_options_desc.add_options()
    ("help,h", "produces this help message")
    ("version,v", "version of the application");

  return generic_options_desc;
}

/////////////////////////////////////////////////////////////////////////
const std::vector< sh_ptr<generic_command>>&
generic_command::get_subcommands() const
{
  return _subcommands;
}

/////////////////////////////////////////////////////////////////////////
void generic_command::print_help () const
{
  using namespace std;

  cout << "Use: " << get_name() << " <options> ";

  print_help_subcommands ();

  cout << ((get_subcommands().size() > 0)? " <subcommand arguments>":"") << endl << endl;

  cout << "Commands:" << endl << endl;

  print_help_subcommands_description ();

  cout << endl;

  cout << "For subcommand's arguments help, ";
  cout << "call the desired subcommand with --help" << endl << endl;

  cout <<  create_options() ;
}

/////////////////////////////////////////////////////////////////////////
void generic_command::print_help_subcommands () const
{
  // join template capture list was not liking the "::" ?
  using namespace std;

  join<string,vector<sh_ptr<generic_command> > >
  (
    "|",
    get_subcommands(),
    ostream_iterator<string>( cout ),
    []( sh_ptr<generic_command> cmd ) -> string
    {
      return cmd->get_name(); 
    }
  );
}

/////////////////////////////////////////////////////////////////////////
void generic_command::print_help_subcommands_description () const
{
  std::for_each
  (
    get_subcommands().begin(),
    get_subcommands().end(),
    [] ( sh_ptr<generic_command> cmd )
    {
      std::cout << "  " << cmd->get_name() << "  \t "
                << cmd->get_description() << std::endl;
    }
  );
}

/////////////////////////////////////////////////////////////////////////
std::vector<std::string> generic_command::remove_argument
  ( const std::vector<std::string>& arguments, const std::string& arg ) const
{
  std::vector<std::string> output;

  std::remove_copy_if
  (
    arguments.begin(), arguments.end(), std::back_inserter(output),
    [&arg](const std::string& current) { return current == arg; }
  );

  return output;
}

/////////////////////////////////////////////////////////////////////////
std::vector<std::string>::const_iterator
generic_command::first_command ( const std::vector<std::string>& arguments )
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


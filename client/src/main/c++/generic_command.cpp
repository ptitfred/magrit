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
generic_command::run ( const std::vector<std::string>& arguments ) const
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

  process_unregistered_options ( arguments, parsed.options, vm );

  process_parsed_options ( arguments, vm );
}

/////////////////////////////////////////////////////////////////////////
void
generic_command::process_unregistered_options
(
  const std::vector<std::string>& arguments,
  const std::vector< boost::program_options::basic_option<char> >&
    options,
  const boost::program_options::variables_map& vm
)
const
{
  namespace bpo = boost::program_options;

  std::vector<std::string> unregistered
    = bpo::collect_unrecognized ( options, bpo::include_positional );

  auto command = first_command ( unregistered );

  if ( get_subcommands().size() == 0 && command == unregistered.end() )
  {
    // No unprocessed arguments. Everything ok!
  }
  else if ( get_subcommands().size() > 0 && command != unregistered.end() )
  {
    // Still arguments to be processed by a subcommand
    for ( uint i = 0; i < get_subcommands().size(); ++i )
    {
      sh_ptr<generic_command> cmd = get_subcommands()[i];

      if ( cmd->get_name() == *command )
      {
        cmd->run ( arguments );
      }
    }
  }
  else
  {
    // Expected a subcommand and no extra arguments passed, or
    // the opposite.
    throw OptionNotRecognized
          ( 
            join<std::string>
            (
              " ",
              unregistered.begin(),
              unregistered.end()
            ) 
          ); 
  }
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
    print_help();

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
std::vector< sh_ptr<generic_command>>
generic_command::get_subcommands() const
{
  return std::vector< sh_ptr<generic_command> >();
}

/////////////////////////////////////////////////////////////////////////
std::vector<std::string> generic_command::get_subcommands_desc() const
{
  return std::vector<std::string>();
}

/////////////////////////////////////////////////////////////////////////
void generic_command::print_help () const
{
  // Template method was not liking the "::" ?
  using namespace std;

  auto cmds      = get_subcommands();
  auto cmds_desc = get_subcommands_desc();

  cout << "Use: " << get_name() << " <options> ";

  join<string,vector<sh_ptr<generic_command> > >
  (
    " | ",
    cmds,
    ostream_iterator<string>( cout ),
    []( sh_ptr<generic_command> cmd ) -> string
    {
      return cmd->get_name(); 
    }
  );

  cout << ((cmds.size() > 0)? " <subcommand>":"") << endl << endl;

  cout << "For subcommand's arguments help, ";
  cout << "call the desired command with --help" << endl << endl;

  /*
  cout << ((cmds.size() > 0)? "Commands:":"") << endl;

  for (uint i = 0; i < cmds.size(); ++i )
  {
    cout << "  " << cmds[i]->get_name() << ":  " << cmds_desc[i] << endl;
  }

  cout << endl;
  */

  cout <<  create_options() ;
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

